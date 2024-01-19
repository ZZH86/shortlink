package com.ch.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ch.shortlink.admin.common.convention.result.Result;
import com.ch.shortlink.admin.common.convention.result.Results;
import com.ch.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.ch.shortlink.admin.remote.dto.req.RecycleBinRemoveReqDTO;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkRecoverRecycleBinReqDTO;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkSaveRecycleBinReqDTO;
import com.ch.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.ch.shortlink.admin.service.RecycleBinService;
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

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    private final RecycleBinService recycleBinService;

    /**
     * 移至回收站
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody ShortLinkSaveRecycleBinReqDTO requestParam) {
        shortLinkActualRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return recycleBinService.pageRecycleShortLink(requestParam);
    }

    /**
     * 恢复回收站短链接
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBinShortLink(@RequestBody ShortLinkRecoverRecycleBinReqDTO requestParam) {
        recycleBinService.recoverRecycleBinShortLink(requestParam);
        return Results.success();
    }

    /**
     * 删除回收站短链接
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBinShortLink(@RequestBody RecycleBinRemoveReqDTO requestParam) {
        shortLinkActualRemoteService.removeRecycleBinShortLink(requestParam);
        return Results.success();
    }
}
