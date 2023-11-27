package com.ch.shortlink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author hui cao
 * @Description: 短链接不存在跳转控制器
 */
@Controller
public class ShortLinkNotFoundController {

    /**
     * 短链接不存在跳转页面
     * @return Thymeleaf 格式返回页面名称
     */
    @RequestMapping("/page/notfound")
    public String notfound(){
        return "notfound";
    }
}
