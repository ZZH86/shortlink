package com.ch.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ch.shortlink.project.dao.entity.ShortLinkDO;
import lombok.Data;

/**
 * @Author hui cao
 * @Description: 短链接分页请求参数
 */
@Data
public class ShortLinkPageReqDTO extends Page<ShortLinkDO> {

    /**
     * 分组标识
     */
    private String gid;
}
