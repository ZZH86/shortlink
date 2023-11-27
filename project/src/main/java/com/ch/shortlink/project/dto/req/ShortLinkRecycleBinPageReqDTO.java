package com.ch.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ch.shortlink.project.dao.entity.ShortLinkDO;
import lombok.Data;

import java.util.List;

/**
 * @Author hui cao
 * @Description: 回收站短链接分页请求参数
 */
@Data
public class ShortLinkRecycleBinPageReqDTO extends Page<ShortLinkDO> {

    /**
     * 分组标识集合
     */
    private List<String> gidList;
}
