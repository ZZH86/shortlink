package com.ch.shortlink.project.dto.req;

import lombok.Data;

/**
 * @Author hui cao
 * @Description: 删除回收站短链接请求参数
 */
@Data
public class RecycleBinRemoveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}

