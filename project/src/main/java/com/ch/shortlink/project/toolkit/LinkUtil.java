package com.ch.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.ch.shortlink.project.common.constant.ShortLinkConstant;
import jakarta.servlet.http.HttpServletRequest;
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
     *
     * @param validDate 有效期时间
     * @return 过期时间
     */
    public static long getLinkCacheValidTime(Date validDate) {
        return Optional.ofNullable(validDate)
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(ShortLinkConstant.DEFAULT_CACHE_VALID_TIME);
    }

    /**
     * 获取网页的标识的 url
     *
     * @param websiteUrl 网页 url
     * @return 标识地址
     */
    @SneakyThrows
    public static String getWebsiteIcon(String websiteUrl) {
        URL url = new URL(websiteUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(websiteUrl).get();
            Element first = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (first != null) {
                return first.attr("abs:href");
            }
        }
        return null;
    }

    /**
     * 获取网站的真实 ip
     *
     * @param request http 请求
     * @return 真实 ip
     */
    public static String getActualIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        return ipAddress;
    }

    /**
     * 获取访问的操作系统
     *
     * @param request http 请求
     * @return os
     */
    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        String os = ""; // 存储操作系统信息

        // 判断User-Agent中的关键词来确定操作系统
        if (userAgent.toLowerCase().contains("windows")) {
            os = "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            os = "Mac";
        } else if (userAgent.toLowerCase().contains("linux")) {
            os = "Linux";
        } else if (userAgent.toLowerCase().contains("android")) {
            os = "Android";
        } else if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
            os = "iOS";
        } else {
            os = "Unknown";
        }

        return os;
    }

    /**
     * 获取用户访问浏览器
     *
     * @param request 请求
     * @return 访问浏览器
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.toLowerCase().contains("chrome")) {
            return "Google Chrome";
        } else if (userAgent.toLowerCase().contains("firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.toLowerCase().contains("safari")) {
            return "Apple Safari";
        } else if (userAgent.toLowerCase().contains("opera")) {
            return "Opera";
        } else if (userAgent.toLowerCase().contains("msie") || userAgent.toLowerCase().contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取用户访问设备
     *
     * @param request 请求
     * @return 访问设备
     */
    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("mobile")) {
            return "Mobile";
        }
        return "PC";
    }

    /**
     * 获取用户访问网络
     *
     * @param request 请求
     * @return 访问设备
     */
    public static String getNetwork(HttpServletRequest request) {
        String actualIp = getActualIp(request);
        // 这里简单判断IP地址范围，您可能需要更复杂的逻辑
        // 例如，通过调用IP地址库或调用第三方服务来判断网络类型
        return actualIp.startsWith("192.168.") || actualIp.startsWith("10.") ? "WIFI" : "Mobile";
    }
}
