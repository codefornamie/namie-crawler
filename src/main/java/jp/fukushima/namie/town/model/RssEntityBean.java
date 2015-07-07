/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.model;

import java.util.Date;
import java.util.HashMap;

import jp.fukushima.namie.town.CrawlerException;
import jp.fukushima.namie.town.Util;

import org.json.simple.JSONObject;

/**
 * RSS Bean.
 */
public class RssEntityBean {
    /** ID. */
    public String id;
    /** 対象サイトのタイトル. */
    public String site;
    /** 対象サイトのURL. */
    public String url;
    /** 記事種別. */
    public String type;
    /** 作成日. */
    public Date createdAt = null;
    /** 更新日. */
    public Date updatedAt = null;
    /** 削除日. */
    public Date deletedAt = null;
    /** 最終クロール日. */
    public Date lastCrawl;
    /** スクレイピングパターン. */
    public String scraping;
    /** デフォルトタグ. */
    public String defaultTag;
    /** 改行コード置換文字列. */
    public String replaceCR;

    /**
     * コンストラクタ.
     * @param json PCSから取得したOData
     */
    public RssEntityBean(JSONObject json) {
        id = (String) json.get("__id");
        site = (String) json.get("site");
        url = (String) json.get("url");
        type = (String) json.get("type");
        defaultTag = (String) json.get("defaultTag");
        createdAt = Util.parseIsoDate((String) json.get("createdAt"));
        updatedAt = Util.parseIsoDate((String) json.get("updatedAt"));
        deletedAt = Util.parseIsoDate((String) json.get("deletedAt"));
        lastCrawl = Util.parseIsoDate((String) json.get("lastCrawl"));
        replaceCR = (String) json.get("replaceCR");
        scraping = (String) json.get("scraping");
    }

    /**
     * このエンティティの永続化JSON形式を返す。
     *
     * @return 永続化形式のJSON
     */
    public HashMap<String, Object> toJSON() {
        HashMap<String, Object> hash = new HashMap<String, Object>();
        hash.put("site", site);
        hash.put("url", url);
        hash.put("type", type);
        hash.put("createdAt", Util.formatIsoDate(createdAt));
        hash.put("updatedAt", Util.formatIsoDate(updatedAt));
        hash.put("deletedAt", Util.formatIsoDate(deletedAt));
        hash.put("lastCrawl", Util.formatIsoDate(lastCrawl));
        hash.put("scraping", scraping);
        hash.put("defaultTag", defaultTag);
        hash.put("replaceCR", replaceCR);
        return hash;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("site : " + site + "\n");
        sb.append("url : " + url + "\n");
        sb.append("createdAt : " + createdAt + "\n");
        sb.append("updatedAt : " + updatedAt + "\n");
        sb.append("deletedAt : " + deletedAt + "\n");
        sb.append("lastCrawl : " + lastCrawl + "\n");
        sb.append("scraping : " + scraping + "\n");
        sb.append("defaultTag : " + defaultTag + "\n");
        sb.append("type : " + type + "\n");
        sb.append("replaceCR : " + replaceCR + "\n");
        return sb.toString();
    }

    /**
     * MERGE更新.
     * @throws CrawlerException Crawler共通例外
     */
    public void update() throws CrawlerException {
        PcsModel pcs = PcsModel.getInstance();
        pcs.updateLastCrawl(id, lastCrawl);
    }
}
