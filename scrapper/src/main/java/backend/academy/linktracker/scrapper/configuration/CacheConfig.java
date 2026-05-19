package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.cache.CacheInvalidationListener;
import backend.academy.linktracker.scrapper.cache.CustomJsonRedisSerializer;
import backend.academy.linktracker.scrapper.cache.TwoLevelCacheManager;
import backend.academy.linktracker.scrapper.properties.CacheProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    @Bean
    public ObjectMapper cacheObjectMapper() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("backend.academy.linktracker.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.time.")
                .build();

        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build()
                .activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    @Bean
    @Primary
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Qualifier("cacheObjectMapper") ObjectMapper cacheObjectMapper,
            CacheProperties properties) {

        CustomJsonRedisSerializer serializer = new CustomJsonRedisSerializer(cacheObjectMapper);

        RedisCacheConfiguration redisConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(properties.getL2Ttl())
                .computePrefixWith(cacheName -> cacheName + ":")
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisConfig)
                .build();

        if (!properties.getClientSide().isEnabled()) {
            return new TransactionAwareCacheManagerProxy(redisCacheManager);
        }

        TwoLevelCacheManager twoLevelCacheManager =
                new TwoLevelCacheManager(caffeineCacheManager(properties), redisCacheManager);

        return new TransactionAwareCacheManagerProxy(twoLevelCacheManager);
    }

    @Bean
    public CaffeineCacheManager caffeineCacheManager(CacheProperties properties) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(properties.getL1Ttl())
                .maximumSize(properties.getClientSide().getMaxSize()));
        return manager;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            CaffeineCacheManager caffeineCacheManager,
            CacheProperties properties) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        if (properties.getClientSide().isEnabled()) {
            CacheInvalidationListener listener = new CacheInvalidationListener(caffeineCacheManager);
            container.addMessageListener(listener, new PatternTopic("__keyevent@*__:del"));
            container.addMessageListener(listener, new PatternTopic("__keyevent@*__:expired"));
        }
        return container;
    }
}
