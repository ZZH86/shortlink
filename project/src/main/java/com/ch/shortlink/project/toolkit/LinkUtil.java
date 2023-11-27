package com.ch.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.ch.shortlink.project.common.constant.ShortLinkConstant;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.HttpURLConnection;
import java.net.URL;
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

    @SneakyThrows
    public static String getWebsiteIcon(String websiteUrl) {
        URL url = new URL(websiteUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK){
            Document document = Jsoup.connect(websiteUrl).get();
            Element first = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (first != null){
                return first.attr("abs:href");
            }
        }
        return null;
    }
}
