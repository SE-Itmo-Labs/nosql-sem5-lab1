package org.seitmolabs.modules.consistency.topology;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.redis-topology")
public class RedisTopologyProperties {

    private List<Node> nodes = new ArrayList<>();

    @Getter
    @Setter
    public static class Node {

        private String id;
        private RedisNodeRole role;
        private String host;
        private int port;
    }
}
