package com.ch.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * @Author hui cao
 * @Description: 回收站短链接分页请求参数
 */
@Data
public class ShortLinkRecycleBinPageReqDTO extends Page {

    /**
     * 分组标识集合
     */
    private List<String> gidList;
}
