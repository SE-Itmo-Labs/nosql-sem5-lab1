package org.seitmolabs.modules.consistency.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.consistency.dto.ExperimentRequest;
import org.seitmolabs.modules.consistency.dto.ExperimentResponse;
import org.seitmolabs.modules.consistency.dto.ModeRequest;
import org.seitmolabs.modules.consistency.dto.NodeInfoResponse;
import org.seitmolabs.modules.consistency.dto.NodesResponse;
import org.seitmolabs.modules.consistency.dto.ReadResponse;
import org.seitmolabs.modules.consistency.dto.WriteRequest;
import org.seitmolabs.modules.consistency.dto.WriteResponse;
import org.seitmolabs.modules.consistency.topology.RedisNodeClient;
import org.seitmolabs.modules.consistency.topology.RedisNodeId;
import org.seitmolabs.modules.consistency.topology.RedisNodeRegistry;
import org.seitmolabs.modules.consistency.topology.ReplicaSelectionStrategy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class ConsistencyServiceTest {

    private StringRedisTemplate primaryTemplate;
    private ValueOperations<String, String> values;
    private NodeProbeService nodeProbeService;
    private RedisNodeRegistry nodeRegistry;
    private ReplicaSelectionStrategy replicaSelectionStrategy;
    private ReplicationTopologyService topologyService;
    private RedisNodeClient primaryNode;
    private RedisNodeClient replicaOne;
    private RedisNodeClient replicaTwo;
    private ConsistencyService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        primaryTemplate = mock(StringRedisTemplate.class);
        values = mock(ValueOperations.class);
        nodeProbeService = mock(NodeProbeService.class);
        nodeRegistry = mock(RedisNodeRegistry.class);
        replicaSelectionStrategy = mock(ReplicaSelectionStrategy.class);
        topologyService = mock(ReplicationTopologyService.class);
        primaryNode = mock(RedisNodeClient.class);
        replicaOne = mock(RedisNodeClient.class);
        replicaTwo = mock(RedisNodeClient.class);
        when(primaryTemplate.opsForValue()).thenReturn(values);
        when(primaryNode.id()).thenReturn(new RedisNodeId("primary"));
        when(primaryNode.template()).thenReturn(primaryTemplate);
        when(replicaOne.id()).thenReturn(new RedisNodeId("replica-1"));
        when(replicaTwo.id()).thenReturn(new RedisNodeId("replica-2"));
        when(nodeRegistry.primary()).thenReturn(primaryNode);
        service = new ConsistencyService(
                nodeRegistry,
                nodeProbeService,
                replicaSelectionStrategy,
                topologyService,
                500,
                2
        );
    }

    @Test
    void usesPrimaryAndAsyncModesByDefault() {
        assertThat(service.getState().readMode()).isEqualTo("PRIMARY");
        assertThat(service.getState().writeMode()).isEqualTo("ASYNC");
        assertThat(service.getState().waitTimeoutMs()).isEqualTo(500);
        assertThat(service.getState().waitReplicas()).isEqualTo(2);
    }

    @Test
    void writesToPrimaryInAsyncMode() {
        WriteResponse response = service.write(new WriteRequest("demo", "version-1"));

        verify(values).set("consistency:demo", "version-1");
        assertThat(response.writeMode()).isEqualTo("ASYNC");
        assertThat(response.replicasAcked()).isNull();
    }

    @Test
    void readsFromPrimaryInMasterMode() {
        when(values.get("consistency:demo")).thenReturn("version-1");

        ReadResponse response = service.read("demo");

        assertThat(response.value()).isEqualTo("version-1");
        assertThat(response.sourceNode()).isEqualTo("primary");
    }

    @Test
    void alternatesBetweenReplicas() {
        service.changeMode(new ModeRequest("REPLICA_PREFERRED", null));
        when(replicaSelectionStrategy.select()).thenReturn(replicaOne, replicaTwo);
        when(nodeProbeService.read("replica-1", "consistency:demo")).thenReturn("old");
        when(nodeProbeService.read("replica-2", "consistency:demo")).thenReturn("new");

        ReadResponse first = service.read("demo");
        ReadResponse second = service.read("demo");

        assertThat(first.sourceNode()).isEqualTo("replica-1");
        assertThat(first.value()).isEqualTo("old");
        assertThat(second.sourceNode()).isEqualTo("replica-2");
        assertThat(second.value()).isEqualTo("new");
    }

    @Test
    void fallsBackToPrimaryWhenReplicaIsUnavailable() {
        service.changeMode(new ModeRequest("REPLICA_PREFERRED", null));
        when(replicaSelectionStrategy.select()).thenReturn(replicaOne);
        when(nodeProbeService.read("replica-1", "consistency:demo"))
                .thenThrow(new IllegalStateException("Replica is unavailable"));
        when(values.get("consistency:demo")).thenReturn("primary-value");

        ReadResponse response = service.read("demo");

        assertThat(response.sourceNode()).isEqualTo("primary");
        assertThat(response.value()).isEqualTo("primary-value");
    }

    @Test
    void invalidModeDoesNotPartiallyChangeState() {
        assertThatThrownBy(() -> service.changeMode(
                new ModeRequest("REPLICA_PREFERRED", "unknown")
        )).hasMessageContaining("Недопустимое значение writeMode");

        assertThat(service.getState().readMode()).isEqualTo("PRIMARY");
        assertThat(service.getState().writeMode()).isEqualTo("ASYNC");
    }

    @Test
    void keepsExternalKeyInNodesResponse() {
        List<NodeInfoResponse> nodes = List.of(
                new NodeInfoResponse("primary", "value", -1L, "master", null)
        );
        when(nodeProbeService.probeAll("consistency:demo"))
                .thenReturn(new NodesResponse("consistency:demo", nodes));

        NodesResponse response = service.getNodes("demo");

        assertThat(response.key()).isEqualTo("demo");
        assertThat(response.nodes()).isEqualTo(nodes);
    }

    @Test
    void runsEventualExperimentAgainstPrimary() {
        when(values.get("consistency:demo")).thenReturn("version-1");
        ExperimentRequest request = new ExperimentRequest(
                "demo",
                "version-1",
                "PRIMARY",
                "EVENTUAL",
                500
        );

        ExperimentResponse response = service.runExperiment(request);

        verify(values).set("consistency:demo", "version-1");
        assertThat(response.sourceNode()).isEqualTo("primary");
        assertThat(response.replicasAcked()).isNull();
        assertThat(response.consistent()).isTrue();
    }

    @Test
    void detectsStaleValueOnReplica() {
        when(replicaSelectionStrategy.select()).thenReturn(replicaOne);
        when(nodeProbeService.read("replica-1", "consistency:demo"))
                .thenReturn("version-1");
        ExperimentRequest request = new ExperimentRequest(
                "demo",
                "version-2",
                "REPLICA",
                "EVENTUAL",
                500
        );

        ExperimentResponse response = service.runExperiment(request);

        assertThat(response.readValue()).isEqualTo("version-1");
        assertThat(response.consistent()).isFalse();
    }
}
