package com.ch.shortlink.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author hui cao
 * @Description: 用户登录返回响应
 */
@Data
@AllArgsConstructor
public class UserLoginRespDTO {

    /**
     * 用户 Token
     */
    private String token;
}
