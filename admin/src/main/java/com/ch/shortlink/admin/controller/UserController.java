package com.ch.shortlink.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author hui cao
 * @Description: 用户管理控制层
 */

@RestController
public class UserController {

    @GetMapping("api/shortlink/v1/user/{username}")
    public String getUserByUserName(@PathVariable("username") String username){
        return "Hi" + username;
    }


}
