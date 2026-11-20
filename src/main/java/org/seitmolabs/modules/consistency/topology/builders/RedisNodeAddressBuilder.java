package org.seitmolabs.modules.consistency.topology.builders;


import org.seitmolabs.modules.consistency.topology.RedisTopologyProperties;

public final class RedisNodeAddressBuilder {
    public static String build(RedisTopologyProperties.Node node) {
        return "redis://" + node.getHost() + ":" + node.getPort();
    }
}