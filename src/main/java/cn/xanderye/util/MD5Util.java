package cn.xanderye.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author XanderYe
 * @date 2020/2/12
 */
public class MD5Util {
    /**
     * MD5加密
     * @param string
     * @return java.lang.String
     * @author yezhendong
     * @date 2020/2/12
     */
    public static String encrypt(String string) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(string.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = m.digest();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                result.append(Integer.toHexString((0x000000FF & bytes[i]) | 0xFFFFFF00).substring(6));
            }
            return result.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
