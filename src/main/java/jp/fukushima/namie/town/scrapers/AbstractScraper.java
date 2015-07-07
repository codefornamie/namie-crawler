/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.scrapers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.fukushima.namie.town.CrawlerException;
import jp.fukushima.namie.town.StrUtil;
import jp.fukushima.namie.town.model.ArticleEntityBean;

/**
 * スクレイプ抽象クラス.
 */
public abstract class AbstractScraper implements Scraper {
    /** Logger. */
    private static Logger log = LoggerFactory.getLogger(AbstractScraper.class);
    private final int maxContentLength = 50000;
    String articleUrlString;
    URL articleUrl;
    Document doc;

    @Override
    public void scraping(ArticleEntityBean article) throws CrawlerException {
    }

    /**
     * Webページを読み込み、DOMを生成する.
     * @param urlStr 対象URL
     * @param charset 文字コード
     * @return DOM
     * @throws IOException
     * @throws CrawlerException
     */
    Document readDocument(String urlStr, String charset) throws IOException, CrawlerException {
        HttpURLConnection connection = null;
        articleUrlString = urlStr;
        articleUrl = new URL(urlStr);
        doc = null;
        if (charset == null) {
            charset = "utf-8";
        }
        try {
            connection = (HttpURLConnection) articleUrl.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                doc = Jsoup.parse(connection.getInputStream(), charset, getBaseURL(articleUrl));
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        if (doc == null) {
            log.warn("article request error : " + urlStr);
            throw new CrawlerException();
        }
        return doc;
    }

    /**
     * URLからベースURLの文字列を取得する。
     * @param url
     * @return ベースURLの文字列。ベースURLが取得できない場合は、もとのurl文字列。
     */
    private static String getBaseURL(URL url) {
        Pattern p = Pattern.compile("^([a-zA-Z]*://[^/]*/)");
        String result = url.toString();
        Matcher m = p.matcher(url.toString());
        if (m.matches()) {
            result = m.group(1);
        }
        return result;
    }

    /**
     * 指定したElement配下のscriptタグの中身を除去(空文字置換)する.
     * @param parent 対象要素
     */
    public void removeDisuseElements(Element parent) {
        // <script>タグの除去
        removeElements(parent.getElementsByTag("script"));
        // Twitterボタンの除去
        removeElements(parent.getElementsByAttributeValueContaining("href", "twitter.com"));
        // mixiボタンの除去
        removeElements(parent.getElementsByAttributeValueContaining("href", "mixi.jp"));
        // Facebookボタンの除去
        removeElements(parent.getElementsByClass("fb-like"));
        removeElement(parent.getElementById("fb-root"));
        // サービスボタン
        removeElements(parent.getElementsByClass("service_button"));
    }

    /**
     * anchorタグのhrefをジャンプしないように書き換える.
     * @param parent 対象の親要素
     */
    public void replaceAnchorsHref(Element parent) {
        Elements anchors = parent.getElementsByTag("a");
        for (Element elm : anchors) {
            elm.attr("href", "javascript:void(0)");
        }
    }

    /**
     * imageタグのsrc属性が相対パスならば絶対パスに書き換える.
     * @param parent 対象の親要素
     * @throws CrawlerException Crawler共通例外
     */
    public void convertImagePath(Element parent) throws CrawlerException {
        Elements images = parent.getElementsByTag("img");
        String src = "";
        URL newUrl;
        for (Element elm : images) {
            src = elm.attr("src");
            src = src.replaceAll("&amp;", "&");
            try {
                newUrl = new URL(articleUrl, src);
                elm.attr("src", newUrl.toString());
            } catch (MalformedURLException e) {
                log.warn("invalid image url for scraping : " + src);
                throw new CrawlerException(e);
            }
        }
    }

    /**
     * 指定された複数の要素を削除する.
     * @param elements 削除対象の要素一覧
     */
    void removeElements(Elements elements) {
        for (Element elm : elements) {
            elm.remove();
        }
    }

    /**
     * 指定された要素を削除する.
     * @param element 削除対象の要素
     */
    void removeElement(Element element) {
        if (element != null) {
            element.remove();
        }
    }

    /**
     * 文字列を指定した長さ毎に分割し、配列を生成する.
     * @param body 対象文字列
     * @return 分割した配列
     */
    public List<String> splitBody(String body) {
        List<String> list = StrUtil.splitBySize(body, maxContentLength);
        if (list.size() == 2) {
            list.add("");
        } else if (list.size() == 1) {
            list.add("");
            list.add("");
        }
        return list;
    }
}
