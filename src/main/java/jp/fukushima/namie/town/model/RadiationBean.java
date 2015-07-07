/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.model;

import java.util.HashMap;

import jp.fukushima.namie.town.CrawlerException;

/**
 * 放射線量データBean.
 */
public class RadiationBean {
    /** 県. */
    public String area = "";
    /** 測定日時. */
    public String dateTime = "";
    /** 緯度. */
    public String latitude = "";
    /** 経度. */
    public String longitude = "";
    /** 測定地点. */
    public String station = "";
    /** 単位. */
    public String unit = "";
    /** 測定値. */
    public String value = "";

    private final int pos0 = 0;
    private final int pos1 = 1;
    private final int pos2 = 2;
    private final int pos3 = 3;
    private final int pos4 = 4;
    private final int pos5 = 5;
    private final int pos6 = 6;

    /**
     * コンストラクタ.
     */
    public RadiationBean() {
    }

    /**
     * コンストラクタ. 渡されたCSVレコードを各フィールドに展開する.
     * @param in CSVレコード
     */
    public RadiationBean(String in) {
        String[] ar = in.split(",");
        area = ar[pos0];
        station = ar[pos1];
        latitude = ar[pos2];
        longitude = ar[pos3];
        dateTime = ar[pos4];
        value = ar[pos5];
        unit = ar[pos6];
    }

    /**
     * このエンティティの永続化JSON形式を返す.
     * @return 永続化形式のJSON
     */
    public HashMap<String, Object> toJSON() {
        HashMap<String, Object> hash = new HashMap<String, Object>();
        hash.put("area", area);
        hash.put("dateTime", dateTime);
        hash.put("latitude", latitude);
        hash.put("longitude", longitude);
        hash.put("station", station);
        hash.put("unit", unit);
        hash.put("value", value);
        return hash;
    }

    /**
     * このエンティティをPCSに保存する。
     * @throws CrawlerException Crawler共通例外
     */
    public void save() throws CrawlerException {
        PcsModel pcs = PcsModel.getInstance();
        pcs.post("radiation", toJSON());
    }

    /**
     * デバッグ用の文字列化する.
     * @return 連結した文字列
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("area : " + area + "\n");
        sb.append("dateTime : " + dateTime + "\n");
        sb.append("latitude : " + latitude + "\n");
        sb.append("longitude : " + longitude + "\n");
        sb.append("station : " + station + "\n");
        sb.append("unit : " + unit + "\n");
        sb.append("value : " + value + "\n");
        return sb.toString();
    }
}
