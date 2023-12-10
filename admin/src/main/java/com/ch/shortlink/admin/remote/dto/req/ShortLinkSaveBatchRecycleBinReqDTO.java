package com.ch.shortlink.admin.remote.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author hui cao
 * @Description: 整个分组移至回收站请求对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShortLinkSaveBatchRecycleBinReqDTO {

    /**
     * 分组标识
     */
    private String gid;
}
