package org.seitmolabs.modules.consistency.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.consistency.dto.ModeRequest;
import org.seitmolabs.modules.consistency.dto.NodeInfoResponse;
import org.seitmolabs.modules.consistency.dto.NodesResponse;
import org.seitmolabs.modules.consistency.dto.ReadResponse;
import org.seitmolabs.modules.consistency.dto.WriteRequest;
import org.seitmolabs.modules.consistency.dto.WriteResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class ConsistencyServiceTest {

    private StringRedisTemplate primaryTemplate;
    private ValueOperations<String, String> values;
    private NodeProbeService nodeProbeService;
    private ConsistencyService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        primaryTemplate = mock(StringRedisTemplate.class);
        values = mock(ValueOperations.class);
        nodeProbeService = mock(NodeProbeService.class);
        when(primaryTemplate.opsForValue()).thenReturn(values);
        service = new ConsistencyService(primaryTemplate, nodeProbeService, 500, 2);
    }

    @Test
    void usesMasterAndAsyncModesByDefault() {
        assertThat(service.getState().readMode()).isEqualTo("MASTER");
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

        assertThat(service.getState().readMode()).isEqualTo("MASTER");
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
}
