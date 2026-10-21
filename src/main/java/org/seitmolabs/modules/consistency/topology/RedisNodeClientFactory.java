package org.seitmolabs.modules.consistency.topology;

import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisNodeClientFactory {

    public RedisNodeClient create(RedisTopologyProperties.Node definition) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
                definition.getHost(),
                definition.getPort()
        );
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration);
        connectionFactory.afterPropertiesSet();

        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        template.afterPropertiesSet();

        return new RedisNodeClient(
                new RedisNodeId(definition.getId()),
                definition.getRole(),
                definition.getHost(),
                definition.getPort(),
                connectionFactory,
                template
        );
    }
}
