package com.ch.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ch.shortlink.project.common.convention.result.Result;
import com.ch.shortlink.project.common.convention.result.Results;
import com.ch.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkSaveRecycleBinReqDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ch.shortlink.project.service.RecycleBinService;
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

    private final RecycleBinService recycleBinService;

    /**
     * 移至回收站
     *
     * @param requestParam 移至回收站请求参数
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody ShortLinkSaveRecycleBinReqDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }

}
