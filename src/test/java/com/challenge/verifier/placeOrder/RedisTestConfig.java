package com.challenge.verifier.placeOrder;

import com.challenge.verifier.matchOrder.MatchOrderCommandHandler;
import com.challenge.verifier.placeOrder.stream.RedisOrderPlacedQueueListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redis.testcontainers.RedisContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@Configuration
@Profile("redis-test")
public class RedisTestConfig {

    @Value("${REDIS_HOST}")
    private String host;

    @MockBean
    private MatchOrderCommandHandler matchOrderCommandHandler;

    @Bean
    public RedisTemplate redisTemplate() {
        RedisTemplate template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        ObjectMapper om = new ObjectMapper();
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(om));
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(om));
        return template;
    }

    @Bean
    MessageListener listener() {
        return new RedisOrderPlacedQueueListener(matchOrderCommandHandler, redisContainer(), topic());
    }

    @Container
    static RedisContainer REDIS_CONTAINER =
            new RedisContainer(DockerImageName.parse("redis:7")).withExposedPorts(6379);

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        REDIS_CONTAINER.start();
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, REDIS_CONTAINER.getFirstMappedPort());
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    RedisMessageListenerContainer redisContainer() {
        RedisMessageListenerContainer container
                = new RedisMessageListenerContainer();
        container.setConnectionFactory(jedisConnectionFactory());
        return container;
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic("orders-placed");
    }
}
