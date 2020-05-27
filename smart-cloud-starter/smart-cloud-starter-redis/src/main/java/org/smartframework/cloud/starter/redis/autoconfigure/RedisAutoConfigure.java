package org.smartframework.cloud.starter.redis.autoconfigure;

import java.io.Serializable;

import org.redisson.Redisson;
import org.smartframework.cloud.starter.redis.component.RedisComponent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

/**
 * redis配置
 *
 * @author liyulin
 * @date 2018-10-17
 */
@Configuration
@ConditionalOnClass({ Redisson.class, RedisOperations.class })
@EnableCaching
public class RedisAutoConfigure {

	@Bean
	public RedisTemplate<Serializable, Serializable> initRedisTemplate(final RedisConnectionFactory connectionFactory) {
		// 使用Jackson2JsonRedisSerialize 替换默认序列化(默认采用的是JDK序列化)
		RedisTemplate<Serializable, Serializable> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setEnableDefaultSerializer(true);
		redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	@Bean
	public StringRedisTemplate initStringRedisTemplate(final RedisConnectionFactory connectionFactory) {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
		stringRedisTemplate.setConnectionFactory(connectionFactory);
		stringRedisTemplate.setEnableDefaultSerializer(true);
		stringRedisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());

		stringRedisTemplate.afterPropertiesSet();
		return stringRedisTemplate;
	}

	@Bean
	public CacheManager cacheManager() {
		return new SimpleCacheManager();
	}

	@Bean
	public ValueOperations<Serializable, Serializable> redisValueOperations(
			final RedisTemplate<Serializable, Serializable> redisTemplate) {
		return redisTemplate.opsForValue();
	}

	@Bean
	public ListOperations<Serializable, Serializable> redisListOperations(
			final RedisTemplate<Serializable, Serializable> redisTemplate) {
		return redisTemplate.opsForList();
	}

	@Bean
	public SetOperations<Serializable, Serializable> redisSetOperations(
			final RedisTemplate<Serializable, Serializable> redisTemplate) {
		return redisTemplate.opsForSet();
	}

	@Bean
	public ZSetOperations<Serializable, Serializable> redisZSetOperations(
			final RedisTemplate<Serializable, Serializable> redisTemplate) {
		return redisTemplate.opsForZSet();
	}

	@Bean
	public HashOperations<Serializable, Serializable, Serializable> redisHashOperations(
			final RedisTemplate<Serializable, Serializable> redisTemplate) {
		return redisTemplate.opsForHash();
	}

	@Bean
	public RedisComponent redisComponent(final StringRedisTemplate stringRedisTemplate) {
		return new RedisComponent(stringRedisTemplate);
	}

}