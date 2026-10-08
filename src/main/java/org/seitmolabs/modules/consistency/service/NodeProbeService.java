package org.seitmolabs.modules.consistency.service;

import java.util.List;
import java.util.Properties;

import org.seitmolabs.common.exceptions.BadRequestException;
import org.seitmolabs.modules.consistency.dto.NodeInfoResponse;
import org.seitmolabs.modules.consistency.dto.NodesResponse;
import org.seitmolabs.modules.consistency.dto.ReplicaActionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class NodeProbeService {

    public static final String PRIMARY = "primary";
    public static final String REPLICA_1 = "replica-1";
    public static final String REPLICA_2 = "replica-2";

    private final StringRedisTemplate primaryTemplate;

    private final String replica1Host;
    private final int replica1Port;
    private final String replica2Host;
    private final int replica2Port;

    private final String masterHost;
    private final int masterPort;

    private LettuceConnectionFactory replica1Factory;
    private LettuceConnectionFactory replica2Factory;
    private StringRedisTemplate replica1Template;
    private StringRedisTemplate replica2Template;

    public NodeProbeService(
            StringRedisTemplate primaryTemplate,
            @Value("${app.replication.replica1-host}") String replica1Host,
            @Value("${app.replication.replica1-port}") int replica1Port,
            @Value("${app.replication.replica2-host}") String replica2Host,
            @Value("${app.replication.replica2-port}") int replica2Port,
            @Value("${app.replication.master-host}") String masterHost,
            @Value("${app.replication.master-port}") int masterPort
    ) {
        this.primaryTemplate = primaryTemplate;
        this.replica1Host = replica1Host;
        this.replica1Port = replica1Port;
        this.replica2Host = replica2Host;
        this.replica2Port = replica2Port;
        this.masterHost = masterHost;
        this.masterPort = masterPort;
    }

    @PostConstruct
    void connect() {
        replica1Factory = newConnectionFactory(replica1Host, replica1Port);
        replica1Template = new StringRedisTemplate(replica1Factory);
        replica1Template.afterPropertiesSet();

        replica2Factory = newConnectionFactory(replica2Host, replica2Port);
        replica2Template = new StringRedisTemplate(replica2Factory);
        replica2Template.afterPropertiesSet();
    }

    @PreDestroy
    void disconnect() {
        if (replica1Factory != null) {
            replica1Factory.destroy();
        }
        if (replica2Factory != null) {
            replica2Factory.destroy();
        }
    }

    public NodeInfoResponse probe(String node, String key) {
        StringRedisTemplate template = templateFor(node);

        String value = template.opsForValue().get(key);
        Long ttlSeconds = template.getExpire(key);

        Properties replication = info(template, "replication");
        String role = replication.getProperty("role", "unknown");
        String linkUp = replication.getProperty("master_link_status");

        return new NodeInfoResponse(node, value, ttlSeconds, role, linkUp);
    }

    public NodesResponse probeAll(String key) {
        List<NodeInfoResponse> nodes = List.of(
                probe(PRIMARY, key),
                probe(REPLICA_1, key),
                probe(REPLICA_2, key)
        );
        return new NodesResponse(key, nodes);
    }

    public String read(String node, String key) {
        return templateFor(node).opsForValue().get(key);
    }

    public ReplicaActionResponse detach(int replicaNumber) {
        executeOnReplica(replicaNumber, connection -> connection.serverCommands().replicaOfNoOne());
        return replicaState(replicaNumber, "detach");
    }

    public ReplicaActionResponse attach(int replicaNumber) {
        executeOnReplica(
                replicaNumber,
                connection -> connection.serverCommands().replicaOf(masterHost, masterPort)
        );

        ReplicaActionResponse state = replicaState(replicaNumber, "attach");
        for (int attempt = 0; attempt < 32 && !"up".equals(state.linkUp()); attempt++) {
            waitBeforeProbe();
            state = replicaState(replicaNumber, "attach");
        }

        return state;
    }

    private ReplicaActionResponse replicaState(int replicaNumber, String action) {
        Properties replication = info(replicaTemplate(replicaNumber), "replication");
        return new ReplicaActionResponse(
                replicaNumber,
                action,
                replication.getProperty("role", "unknown"),
                replication.getProperty("master_link_status")
        );
    }

    private void executeOnReplica(int replicaNumber, java.util.function.Consumer<RedisConnection> action) {
        RedisConnection connection = replicaTemplate(replicaNumber).getConnectionFactory().getConnection();
        try {
            action.accept(connection);
        } finally {
            connection.close();
        }
    }

    private StringRedisTemplate templateFor(String node) {
        return switch (node) {
            case PRIMARY -> primaryTemplate;
            case REPLICA_1 -> replica1Template;
            case REPLICA_2 -> replica2Template;
            default -> throw new BadRequestException("Неизвестная нода: " + node);
        };
    }

    private StringRedisTemplate replicaTemplate(int replicaNumber) {
        return switch (replicaNumber) {
            case 1 -> replica1Template;
            case 2 -> replica2Template;
            default -> throw new BadRequestException("Реплика должна быть 1 или 2, получено: " + replicaNumber);
        };
    }

    private LettuceConnectionFactory newConnectionFactory(String host, int port) {
        LettuceConnectionFactory factory =
                new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
        factory.afterPropertiesSet();
        return factory;
    }

    private void waitBeforeProbe() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Ожидание подключения реплики прервано", e);
        }
    }

    private static Properties info(StringRedisTemplate template, String section) {
        RedisConnection connection = template.getConnectionFactory().getConnection();
        try {
            return connection.info(section);
        } finally {
            connection.close();
        }
    }
}
