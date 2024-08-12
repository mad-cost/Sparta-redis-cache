package com.example.redis.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Configuration
@EnableCaching // 캐싱 기능을 활성화하기 위해 사용되는 어노테이션
public class CacheConfig {
  // CacheManager의 구현체를 만들어서 빈으로 공급해줘야지 스프링 부트가 빈 객체를 사용하여 캐싱을 구성

  @Bean // Redis를 사용하는 CacheManager
  public RedisCacheManager cacheManager(
          RedisConnectionFactory redisConnectionFactory
  ) {
    // 설정 구성을 먼저 진행한다
    // Redis를 이용해서 Spring Cache를 사용할 때, Redis 관련 설정을 모아두는 클래스
    RedisCacheConfiguration configuration = RedisCacheConfiguration
            // Redis를 사용하여 캐시를 설정할 때 기본 설정을 반환하는 메서드
            .defaultCacheConfig()
            // 결과가 null이면 캐싱하지 않는다
            .disableCachingNullValues()
            // 기본 캐시 유지 시간을 유지 / Ttl(Time To Live)
            .entryTtl(Duration.ofSeconds(120))
            // 캐시를 구분하는 접두사 설정 / 캐시를할 때 캐시의 데이터가 Redis에 들어갈 때 키의 모습
            .computePrefixWith(CacheKeyPrefix.simple())
            // Redis 캐시에 저장할 값을 어떻게 직렬화/역직렬화 할것인지
            .serializeValuesWith(
                    // RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.java())
                    SerializationPair.fromSerializer(RedisSerializer.java())
            );

    // configuration 을 사용하는 RedisCacheManager 만들고 반환
    return RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(configuration)
            .build();
  }

}
