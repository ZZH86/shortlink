package com.ch.shortlink.project.dto.req;

import lombok.Data;

/**
 * @Author hui cao
 * @Description: 整个分组移至回收站请求对象
 */
@Data
public class ShortLinkSaveBatchRecycleBinReqDTO {

    /**
     * 分组标识
     */
    private String gid;
}
