/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.model;

import java.util.HashMap;
import java.util.UUID;

import jp.fukushima.namie.town.CrawlerException;
import jp.fukushima.namie.town.Util;

import org.json.simple.JSONObject;

import com.google.api.client.util.DateTime;

/**
 * 記事(Article)Bean.
 */
public class ArticleEntityBean {
    /** ID. */
    public String id = "";
    /** 対象サイトのタイトル. */
    public String site = "";
    /** 対象サイトのURL. */
    public String url = "";
    /** 記事へのLINK. */
    public String link = "";
    /** 記事種別. */
    public String type = "";
    /** 作成日. */
    public DateTime createdAt;
    /** 更新日. */
    public DateTime updatedAt;
    /** 削除日. */
    public DateTime deletedAt;
    /** 公開日. */
    public String publishedAt;
    /** 記事タイトル. */
    public String title = "";
    /** 記事詳細. */
    public String description = "";
    /** 記事HTMLソース. */
    public String rawHTML = "";
    /** 記事HTMLソース. */
    public String rawHTML2 = "";
    /** 記事HTMLソース. */
    public String rawHTML3 = "";
    /** 記事作成者. */
    public String auther = "";
    /** スクレイピングパターン. */
    public String scraping = "";
    /** 画像URL. */
    public String imageUrl = "";
    /** 画像URL. */
    public String imageUrl2 = "";
    /** 画像URL. */
    public String imageUrl3 = "";
    /** サムネイル画像URL. */
    public String imageThumbUrl = "";
    /** 画像パス. */
    public String imagePath = "";
    /** 生成したJSON. */
    public JSONObject json = null;
    /** タグ. */
    public String tags = "";

    /**
     * コンストラクタ.
     */
    public ArticleEntityBean() {
        id = UUID.randomUUID().toString();
    }

    /**
     * このエンティティの文字列表現を返す。
     * @return 結合した文字列.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("site : " + site + "\n");
        sb.append("url : " + url + "\n");
        sb.append("link : " + link + "\n");
        sb.append("type : " + type + "\n");
        sb.append("createdAt : " + createdAt + "\n");
        sb.append("updatedAt : " + updatedAt + "\n");
        sb.append("deletedAt : " + deletedAt + "\n");
        sb.append("publishedAt : " + publishedAt + "\n");
        sb.append("title : " + title + "\n");
        sb.append("description : " + description + "\n");
        sb.append("rawHTML : " + rawHTML + "\n");
        sb.append("rawHTML2 : " + rawHTML2 + "\n");
        sb.append("rawHTML3 : " + rawHTML3 + "\n");
        sb.append("auther : " + auther + "\n");
        sb.append("scraping : " + scraping + "\n");
        sb.append("imageUrl : " + imageUrl + "\n");
        sb.append("imageUrl2 : " + imageUrl2 + "\n");
        sb.append("imageUrl3 : " + imageUrl3 + "\n");
        sb.append("imageThumbUrl : " + imageThumbUrl + "\n");
        sb.append("imagePath : " + imagePath + "\n");
        sb.append("tags : " + tags + "\n");
        return sb.toString();
    }

    /**
     * このエンティティの永続化JSON形式を返す。
     * @return 永続化形式のJSON
     */
    public HashMap<String, Object> toJSON() {
        HashMap<String, Object> hash = new HashMap<String, Object>();
        hash.put("__id", id);
        hash.put("site", site);
        hash.put("url", url);
        hash.put("link", link);
        hash.put("type", type);
        if (createdAt == null) {
            hash.put("createdAt", "");
        } else {
            hash.put("createdAt", Util.formatIsoDate(createdAt));
        }
        if (updatedAt == null) {
            hash.put("updatedAt", "");
        } else {
            hash.put("updatedAt", Util.formatIsoDate(updatedAt));
        }
        if (deletedAt == null) {
            hash.put("deletedAt", "");
        } else {
            hash.put("deletedAt", Util.formatIsoDate(deletedAt));
        }
        hash.put("publishedAt", publishedAt);
        hash.put("title", title);
        hash.put("description", description);
        hash.put("rawHTML", rawHTML);
        hash.put("rawHTML2", rawHTML2);
        hash.put("rawHTML3", rawHTML3);
        hash.put("auther", auther);
        hash.put("scraping", scraping);
        hash.put("imageUrl", imageUrl);
        hash.put("imageUrl2", imageUrl2);
        hash.put("imageUrl3", imageUrl3);
        hash.put("imageThumbUrl", imageThumbUrl);
        hash.put("imagePath", imagePath);
        hash.put("tags", tags);
        return hash;
    }

    /**
     * このエンティティをPCSに保存する。
     * @throws CrawlerException Crawler共通例外
     */
    public void save() throws CrawlerException {
        PcsModel pcs = PcsModel.getInstance();
        pcs.post("article", toJSON());
    }
}
