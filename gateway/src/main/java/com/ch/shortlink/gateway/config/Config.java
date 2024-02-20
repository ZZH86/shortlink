package com.ch.shortlink.gateway.config;

import lombok.Data;

import java.util.List;

/**
 * @Author hui cao
 * @Description: 过滤器配置
 */
@Data
public class Config {

    /**
     * 过滤器配置
     */
    private List<String> whitePathList;
}
