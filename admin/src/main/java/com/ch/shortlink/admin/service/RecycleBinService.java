package com.ch.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ch.shortlink.admin.common.convention.result.Result;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkRecoverRecycleBinReqDTO;
import com.ch.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ch.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * @Author hui cao
 * @Description: URL 回收站接口层
 */
public interface RecycleBinService {

    /**
     * 分页查询回收站
     *
     * @param requestParam 分页查询回收站请求参数
     * @return 分页查询回收站返回对象
     */
    Result<Page<ShortLinkPageRespDTO>> pageRecycleShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 恢复复短连接
     *
     * @param requestParam 恢复短连接请求参数
     */
    void recoverRecycleBinShortLink(ShortLinkRecoverRecycleBinReqDTO requestParam);
}
