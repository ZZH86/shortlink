package com.ch.shortlink.admin.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author hui cao
 * @Description: 布隆过滤器配置类
 */
@Configuration
public class RBloomFilterConfiguration {

    @Value("${short-link.bloomFilter.count}")
    private Long count;

    @Value("${short-link.bloomFilter.error}")
    private Double error;
    /**
     * 防止用户注册查询数据库的布隆过滤器
     */
    @Bean
    public RBloomFilter<String> userRegisterCachePenetrationBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter("userRegisterCachePenetrationBloomFilter");
        cachePenetrationBloomFilter.tryInit(count, error);
        return cachePenetrationBloomFilter;
    }
}
