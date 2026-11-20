package org.seitmolabs.modules.consistency.topology;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisNodeClientFactory {

    private final LettuceClientConfiguration clientConfiguration;

    // Задаем настройки ожидания для всех узлов, чтобы система была +- детерминированной
    public RedisNodeClientFactory(
            @Value("${spring.data.redis.connect-timeout}") Duration connectTimeout,
            @Value("${spring.data.redis.timeout}") Duration commandTimeout
    ) {

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(connectTimeout)
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .build();

        clientConfiguration = LettuceClientConfiguration.builder()
                .commandTimeout(commandTimeout)
                .clientOptions(clientOptions)
                .build();
    }

    public RedisNodeClient create(RedisTopologyProperties.Node definition) {

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
                definition.getHost(),
                definition.getPort()
        );

        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration, clientConfiguration);

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
