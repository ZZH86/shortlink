package com.ch.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.ch.shortlink.project.common.constant.ShortLinkConstant;

import java.util.Date;
import java.util.Optional;

/**
 * @Author hui cao
 * @Description: 短链接工具类
 */
public class LinkUtil {

    /**
     * 获取短连接缓存有效期时间
     * @param validDate 有效期时间
     * @return 过期时间
     */
    public static long getLinkCacheValidTime(Date validDate){
        return Optional.ofNullable(validDate)
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(ShortLinkConstant.DEFAULT_CACHE_VALID_TIME);
    }
}
