/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */

package jp.fukushima.namie.town;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;

import jp.fukushima.namie.town.model.ArticleEntityBean;
import jp.fukushima.namie.town.model.HttpModel;
import jp.fukushima.namie.town.model.RssEntityBean;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.DateTime;

/**
 * Facebookクローラー。
 */
public class FacebookCrawler extends ArticleCrawler {
    /** Logger. */
    private static Logger log = LoggerFactory.getLogger(FacebookCrawler.class);
    /** Facebook API のURL. */
    private final String apiBase = "https://graph.facebook.com/v2.2/";
    /** タイトル最大長. */
    private final int titleLength = 40;
    /** JavaのISO8601形式からミリ秒を除去するための定数. */
    private final int millisecond = 1000;
    /** 記事取得件数. (Facebookデフォルトは25件). */
    private final int postLimit = 100;

    @Override
    public Boolean crawl(RssEntityBean site) throws CrawlerException {
        log.info("start Facebook crawl page id = " + site.url);
        boolean updated = false;

        // 記事一覧を取得する
        JSONArray ar = readPostList(site, site.url);
        int len = ar.size();
        log.info("post size : " + len);

        // １記事毎にループ
        for (int i = 0; i < len; i++) {
            JSONObject item = (JSONObject) ar.get(i);
            if (createArticle(site, item)) {
                updated = true;
            }
        }
        log.info("end Facebook crawl");
        return updated;
    }

    /**
     * Facebookの記事情報からArticleデータを生成する。
     * @param site RSSエンティティ情報
     * @param item Facebookから取得した１件の記事
     * @return 更新が必要かどうかのフラグ
     * @throws CrawlerException
     */
    @SuppressWarnings("deprecation")
    private boolean createArticle(RssEntityBean site, JSONObject item) throws CrawlerException {
        HttpModel http = HttpModel.getInstance();
        ArticleEntityBean article = new ArticleEntityBean();
        String originalMessage = (String) item.get("message");
        if ((originalMessage == null) || (originalMessage.equals(""))) {
            //本文がないメッセージは対象外とする
            return false;
        }
        String objectId = (String) item.get("object_id");
        String[] filePath = {};
        if (!item.get("type").equals("video")) {
            if (objectId != null) {
                // 写真記事の場合、オリジナルサイズの画像URLを取得する
                String originalPitcureURL = readPictureUrl(objectId);
                filePath = http.putPicture(article.id, originalPitcureURL);
            }
        }
        String body = originalMessage;
        String title = body;
        if ((title != null) && (title.length() > titleLength)) {
            title = title.substring(0, titleLength);
            int cr = title.indexOf("\n");
            if (cr != -1) {
                title = title.substring(0, cr);
            }
        }
        if (body != null) {
            // 新聞アプリで表示する際、改行コードは<br>を期待しているため、改行コードを全置換する
            body = body.replaceAll("\n", "<br/>");
        }
        article.link = (String) item.get("link");
        article.title = title;
        article.description = originalMessage;
        article.rawHTML = body;
        article.imageThumbUrl = (String) item.get("picture");
        if (filePath.length != 0) {
            article.imageUrl = filePath[1];
            article.imagePath = filePath[0];
        } else {
            article.imageUrl = article.imageThumbUrl;
        }
        article.url = site.url;

        String dateString = (String) item.get("created_time");
        Date date = Util.parseIsoDate2(dateString);
        article.createdAt = new DateTime(date);

        String fixHour = Conf.getValue("fix_time");
        Date now = new Date();
        Date fixDate = new Date(now.getYear(), now.getMonth(), now.getDate(), Integer.parseInt(fixHour), 0);
        if (now.getTime() > fixDate.getTime()) {
            article.publishedAt = tomorrow;
        } else {
            article.publishedAt = today;
        }

        article.site = site.site;
        article.type = site.type;
        article.tags = site.defaultTag;
        log.info("save article id = " + article.id + " / type = " + item.get("type") + " / title = " + article.title);
        article.save();
        return true;
    }

    /**
     * Facebook graph API を利用して記事一覧を取得する。
     * @param pageId 対象のページID
     * @return 取得したJSON配列
     * @throws CrawlerException
     */
    private JSONArray readPostList(RssEntityBean site, String pageId) throws CrawlerException {
        StringBuilder sb = new StringBuilder();
        sb.append(apiBase);
        sb.append(pageId);
        sb.append("/posts?");
        try {
            sb.append("limit=" + String.valueOf(postLimit) + "&");
            sb.append("access_token=" + URLEncoder.encode(Conf.getValue("facebookAccessToken"), "utf-8"));
            if (site.lastCrawl != null) {
                sb.append("&since=" + String.valueOf(site.lastCrawl.getTime() / millisecond));
            }
        } catch (UnsupportedEncodingException e) {
            log.warn("facebook api url encode failure  : " + e.getMessage());
            throw new CrawlerException(e);
        }
        JSONObject json = null;
        try {
            json = httpGetAsJSON(sb.toString());
        } catch (IOException e) {
            log.warn("facebook api request failure  : " + e.getMessage());
            throw new CrawlerException(e);
        }
        JSONArray ret = null;
        if (json != null) {
            ret = (JSONArray) json.get("data");
        } else {
            ret = new JSONArray();
        }
        return ret;
    }

    /**
     * Facebook graph API を利用して写真情報を取得する。
     * @param objectId 対象のオブジェクトID
     * @return 取得した写真URL
     * @throws CrawlerException
     */
    private String readPictureUrl(String objectId) throws CrawlerException {
        StringBuilder sb = new StringBuilder();
        sb.append(apiBase);
        sb.append(objectId);
        sb.append("?");
        try {
            sb.append("access_token=" + URLEncoder.encode(Conf.getValue("facebookAccessToken"), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            log.warn("facebook api url encode failure  : " + e.getMessage());
            throw new CrawlerException(e);
        }
        JSONObject json = null;
        try {
            json = httpGetAsJSON(sb.toString());
        } catch (IOException e) {
            log.warn("facebook api request failure  : " + e.getMessage());
            throw new CrawlerException(e);
        }
        return (String) json.get("source");
    }

    /**
     * HTTP GETリクエストを行う。
     * @param url 対象URL
     * @return 取得したレスポンスボディ
     * @throws IOException
     * @throws CrawlerException
     */
    private JSONObject httpGetAsJSON(String url) throws IOException, CrawlerException {
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = httpget(new URI(url));
        } catch (IOException | URISyntaxException e1) {
            log.warn("http request failure  : " + e1.getMessage());
            throw new CrawlerException(e1);
        }
        if (is != null) {
            reader = new BufferedReader(new InputStreamReader(is));
        }

        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            log.warn("http response parse failure  : " + e.getMessage());
            throw new CrawlerException(e);
        }
        if (is != null) {
            is.close();
        }
        if (reader != null) {
            reader.close();
        }
        return json;
    }
}
