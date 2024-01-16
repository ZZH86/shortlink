package com.ch.shortlink.project.common.constant;

/**
 * @Author hui cao
 * @Description: redis key 常量类
 */
public class RedisKeyConstant {

    /**
     * 短链接跳转 key
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link:goto:%s";

    /**
     * 短链接跳转锁 key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock:goto:%s";

    /**
     * 短链接空值跳转
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link:is_null_goto:%s";

    /**
     * 短链接uv统计
     */
    public static final String UV_STATS_SHORT_LINK_KEY = "short-link:stats:uv:%s";

    /**
     * 短链接uip统计
     */
    public static final String UIP_STATS_SHORT_LINK_KEY = "short-link:stats:uip:%s";

    /**
     * 短链接修改分组 ID 锁前缀 Key
     */
    public static final String LOCK_GID_UPDATE_KEY = "short-link:lock:update-gid:%s";

    /**
     * 短链接延迟队列消费统计 Key
     */
    public static final String DELAY_QUEUE_STATS_KEY = "short-link:delay-queue:stats";

}
