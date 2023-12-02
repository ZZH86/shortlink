package com.ch.shortlink.project.common.constant;

/**
 * @Author hui cao
 * @Description: redis key 常量类
 */
public class RedisKeyConstant {

    /**
     * 短链接跳转 key
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link_goto_%s";

    /**
     * 短链接跳转锁 key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "lock_short-link_goto_%s";

    /**
     * 短链接空值跳转
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link_is_null_goto_%s";

    /**
     * 短链接uv统计
     */
    public static final String UV_STATS_SHORT_LINK_KEY = "short-link_stats_uv_%s";

    /**
     * 短链接uip统计
     */
    public static final String UIP_STATS_SHORT_LINK_KEY = "short-link_stats_uip_%s";

}
