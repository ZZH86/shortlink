package com.ch.shortlink.project.dto.resp;

import lombok.Data;

/**
 * @Author hui cao
 * @Description: 短链接分组查询返回参数
 */
@Data
public class ShortLinkGroupCountQueryRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接数
     */
    private Integer ShortLinkCount;
}
