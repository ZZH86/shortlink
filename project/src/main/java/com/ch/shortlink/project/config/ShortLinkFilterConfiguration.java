package com.ch.shortlink.project.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author hui cao
 * @Description: 布隆过滤器配置类
 */
@Configuration
public class ShortLinkFilterConfiguration {

    /**
     * 防止短链接注册查询数据库的布隆过滤器
     */
    @Bean
    public RBloomFilter<String> shortLinkBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter("shortLinkBloomFilter");
        cachePenetrationBloomFilter.tryInit(100000000L, 0.001);
        return cachePenetrationBloomFilter;
    }
}
