package com.ch.shortlink.project.toolkit;

import cn.hutool.core.lang.hash.MurmurHash;

import java.util.Random;

/**
 * @Author hui cao
 * @Description: 生成六位数域名算法
 */
public class HashUtil {

    private static final char[] CHARS = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static final int SIZE = CHARS.length;

//    private static String convertDecToBase62(long num) {
//        StringBuilder sb = new StringBuilder();
//        while (num > 0) {
//            int i = (int) (num % SIZE);
//            sb.append(CHARS[i]);
//            num /= SIZE;
//        }
//        return sb.reverse().toString();
//    }
//
//    public static String hashToBase62(String str) {
//        int i = MurmurHash.hash32(str);
//        long num = i < 0 ? Integer.MAX_VALUE - (long) i : i;
//        return convertDecToBase62(num);
//    }

    /**
     * 直接随机生成 6 位数 base62 编码的六位数短链接
     */

    private static final Random random = new Random();
    public static String base62Generator() {

        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHARS[random.nextInt(SIZE)]);
        }
        return sb.toString();
    }
}
