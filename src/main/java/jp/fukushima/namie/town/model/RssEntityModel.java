/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.model;

import java.util.ArrayList;

import jp.fukushima.namie.town.CrawlerException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * 「RSS」EntityTypeに対するPCSアクセスモデル.
 */
public class RssEntityModel {
    PcsModel pcs;

    /**
     * コンストラクタ.
     */
    public RssEntityModel() {
        pcs = PcsModel.getInstance();
    }

    /**
     * RSS一覧を取得.
     * @return 取得したRSS情報をマッピングしたRssEntityBeanの配列.
     * @throws CrawlerException Crawler共通例外
     */
    public ArrayList<RssEntityBean> list() throws CrawlerException {
        ArrayList<RssEntityBean> ar = null;
        JSONArray list = pcs.list("rss");
        ar = new ArrayList<RssEntityBean>();
        for (Object obj : list) {
            ar.add(new RssEntityBean((JSONObject) obj));
        }
        return ar;
    }
}
