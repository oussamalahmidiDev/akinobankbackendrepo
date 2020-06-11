package com.akinobank.app.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
@Log4j2
public class Redis {

    @Value("${spring.redis.host}")
    private String hostName;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.client-name}")
    private String clientName;

    @Value("${spring.redis.port}")
    private int port;


    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConFactory
            = new JedisConnectionFactory();
        jedisConFactory.setHostName(hostName);
        jedisConFactory.setPassword(password);
        jedisConFactory.setClientName(clientName);
        jedisConFactory.setDatabase(0);
        jedisConFactory.setPort(port);
        return jedisConFactory;

    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        log.info("Redis connection : {}", jedisConnectionFactory().getHostName());
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        return template;
    }
}
