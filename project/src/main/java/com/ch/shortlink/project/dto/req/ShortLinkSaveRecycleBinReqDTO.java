package com.ch.shortlink.project.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author hui cao
 * @Description: 移至回收站请求对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortLinkSaveRecycleBinReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
