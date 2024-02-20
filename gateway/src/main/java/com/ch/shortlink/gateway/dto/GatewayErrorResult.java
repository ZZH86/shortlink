package com.ch.shortlink.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author hui cao
 * @Description: 网关错误返回信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GatewayErrorResult {

    /**
     * HTTP 状态码
     */
    private Integer status;

    /**
     * 返回信息
     */
    private String message;
}
