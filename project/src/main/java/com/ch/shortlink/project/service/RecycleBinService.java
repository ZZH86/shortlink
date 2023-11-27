package com.ch.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ch.shortlink.project.dao.entity.ShortLinkDO;
import com.ch.shortlink.project.dto.req.ShortLinkRecoverRecycleBinReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ch.shortlink.project.dto.req.ShortLinkSaveRecycleBinReqDTO;
import com.ch.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * @Author hui cao
 * @Description: 回收站接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 移至回收站
     *
     * @param requestParam 移至回收站请求参数
     */
    void saveRecycleBin(ShortLinkSaveRecycleBinReqDTO requestParam);

    /**
     * 分页查询回收站
     *
     * @param requestParam 分页查询请求参数
     * @return 分页查询返回对象
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 恢复回收站短链接
     *
     * @param requestParam 恢复回收站短链接请求参数
     */
    void recoverRecycleBinShortLink(ShortLinkRecoverRecycleBinReqDTO requestParam);
}
