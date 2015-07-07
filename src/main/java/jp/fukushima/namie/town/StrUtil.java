/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town;

import java.util.ArrayList;
import java.util.List;

/**
 * 文字列操作ユーティリティクラス.
 */
public class StrUtil {
    /**
     * コンストラクタ.
     */
    private StrUtil() {
    }

    /**
     * 文字列切り出し（Byte単位）.
     * @param str 切り出し対象文字列
     * @param len 切り出しバイト数
     * @param charset 文字コード
     * @return String 切り出し後の文字列
     */
    public static String leftB(String str, Integer len, String charset) {
        StringBuffer sb = new StringBuffer();
        int cnt = 0;
        try {
            for (int i = 0; i < str.length(); i++) {
                String tmpStr = str.substring(i, i + 1);
                byte[] b = tmpStr.getBytes(charset);
                if (cnt + b.length > len) {
                    return sb.toString();
                } else {
                    sb.append(tmpStr);
                    cnt += b.length;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 文字列を指定した長さ毎に分割し、配列を生成する.
     * @param str 入力文字列
     * @param len 分割する長さ
     * @return 生成した配列
     */
    public static List<String> splitBySize(String str, Integer len) {
        List<String> ss = new ArrayList<String>();
        String ret;
        while (str.getBytes().length > len) {
            ret = StrUtil.leftB(str, len, "utf-8");
            ss.add(ret);
            str = str.replace(ret, "");
        }
        ss.add(new String(str));
        return ss;
    }
}
