package com.ch.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ch.shortlink.admin.common.convention.result.Result;
import com.ch.shortlink.admin.common.convention.result.Results;
import com.ch.shortlink.admin.remote.ShortLinkRemoteService;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkSaveRecycleBinReqDTO;
import com.ch.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author hui cao
 * @Description: 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    // TODO 后续重构为 springCloud Feign 调用
    ShortLinkRemoteService shortLinkService = new ShortLinkRemoteService(){};

    /**
     * 移至回收站
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody ShortLinkSaveRecycleBinReqDTO requestParam){
        shortLinkService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        return shortLinkService.pageRecycleShortLink(requestParam);
    }
}
