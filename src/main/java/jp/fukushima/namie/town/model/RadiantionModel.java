/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fujitsu.dc.client.utils.StringUtils;

/**
 * 放射線量データ取得モデル.
 */
public class RadiantionModel {
    static final String FUKUSHIMA_SOUSOU = "07B";
    static final String NAMIE = "07547";
    /** 取得した一覧. */
    public ArrayList<RadiationBean> data = new ArrayList<RadiationBean>();

    /**
     * 放射線量データ(CSV)をダウンロードする.
     * @param dateStr 対象の日付文字列
     * @param timeStr 対象の時刻文字列
     * @return 取得したBeanの配列
     * @throws IOException 例外.
     */
    public ArrayList<RadiationBean> read(String dateStr, String timeStr) throws IOException {
        String urlstr = "http://radioactivity.nsr.go.jp/monitor_sv/downloadCSV";

        URL url = null;
        url = new URL(urlstr);
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        PrintWriter printWriter = null;
        printWriter = new PrintWriter(connection.getOutputStream());
        printWriter.print(makeRequestBody(dateStr, timeStr));
        printWriter.close();

        BufferedReader bufferReader = null;
        bufferReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "JISAutoDetect"));
        String str;
        RadiationBean bean;
        while (null != (str = bufferReader.readLine())) {
            bean = new RadiationBean(str);
            data.add(bean);
        }
        bufferReader.close();
        connection.disconnect();
        return data;
    }

    /**
     * 取得するためのリクエストボディ(検索条件)を生成する.
     * @param dateStr 日付文字列.
     * @param timeStr 時刻文字列.
     * @return 生成したリクエストボディ文字列.
     */
    String makeRequestBody(String dateStr, String timeStr) {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("ParentArea", FUKUSHIMA_SOUSOU);
        hash.put("AreaCode", NAMIE);
        hash.put("StationCode", "");
        hash.put("DataType", "real");
        hash.put("SensorCode", "air");
        hash.put("StartDate", dateStr);
        hash.put("StartHour", timeStr);
        hash.put("StartMinute", "00");
        hash.put("EndDate", dateStr);
        hash.put("EndHour", timeStr);
        hash.put("EndMinute", "00");
        hash.put("IntervalMinute", "10");
        ArrayList<String> ar = new ArrayList<String>();
        for (Map.Entry<String, String> e : hash.entrySet()) {
            ar.add(e.getKey() + "=" + e.getValue());
        }
        return StringUtils.join(ar, "&");
    }
}
