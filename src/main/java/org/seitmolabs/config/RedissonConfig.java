package org.seitmolabs.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReplicatedServersConfig;
import org.redisson.config.SentinelServersConfig;
import org.seitmolabs.modules.consistency.topology.builders.RedisNodeAddressBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.seitmolabs.modules.consistency.topology.RedisTopologyProperties;
import org.seitmolabs.modules.consistency.topology.RedisTopologyProperties.Node;
import org.redisson.config.MasterSlaveServersConfig;

@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisTopologyProperties topology) {
        Config config = new Config();

        // config.useSingleServer() 
                // .setAddress("redis://" + host + ":" + port);

//        SentinelServersConfig servers = config.useSentinelServers().setMasterName(topology.getNodes().get(0).getId());

        SentinelServersConfig servers = config.useSentinelServers().setMasterName(topology.getNodes().getFirst().getId());

        for (Node topologyNode : topology.getNodes()) {

            String address = RedisNodeAddressBuilder.build(topologyNode);

            switch (topologyNode.getRole()) {
                case REPLICA -> servers.addSentinelAddress(address);
            }
        }

        return Redisson.create(config);
    }
}