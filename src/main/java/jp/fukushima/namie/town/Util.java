/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.api.client.util.DateTime;

/**
 * ユーティリティ.
 */
public class Util {
    /**
     * コンストラクタ.
     */
    private Util() {
    }

    /**
     * DateTime型からISO日付形式の文字列に変換する。
     * @param d 変換対象のDate
     * @return 変換後の文字列
     */
    public static String formatIsoDate(DateTime d) {
        return d.toStringRfc3339();
    }

    /**
     * Date型からISO日付形式の文字列に変換する。
     * @param d 変換対象のDate
     * @return 変換後の文字列
     */
    public static String formatIsoDate(Date d) {
        SimpleDateFormat iso8601format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return iso8601format.format(d);
    }

    /**
     * ISO日付形式の文字列をDate型に変換する。変換に失敗した場合、EPOC_DATEを返す。
     * @param s ISO日付形式の文字列
     * @return 変換後のDate値
     */
    public static Date parseIsoDate(String s) {
        SimpleDateFormat iso8601format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            return iso8601format.parse(s);
        } catch (Exception e) {
            return null; // EPOC_DATE;
        }
    }

    /**
     * ISO日付形式の文字列をDate型に変換する。変換に失敗した場合、EPOC_DATEを返す。
     * @param s ISO日付形式の文字列
     * @return 変換後のDate値
     */
    public static Date parseIsoDate2(String s) {
        //SimpleDateFormat iso8601format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+Z");
        SimpleDateFormat iso8601format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        try {
            return iso8601format.parse(s);
        } catch (Exception e) {
            return null; // EPOC_DATE;
        }
    }
}
