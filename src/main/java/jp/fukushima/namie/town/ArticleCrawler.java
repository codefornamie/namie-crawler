/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.DateTime;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

import jp.fukushima.namie.town.model.ArticleEntityBean;
import jp.fukushima.namie.town.model.PcsModel;
import jp.fukushima.namie.town.model.RssEntityBean;
import jp.fukushima.namie.town.scrapers.Scraper;
import jp.fukushima.namie.town.scrapers.ScraperFactory;

/**
 * 記事の収集クラス.
 */
public class ArticleCrawler {
    /** Logger. */
    private static Logger log = LoggerFactory.getLogger(ArticleCrawler.class);
    /** 締め切り時間. */
    public Calendar fixedDate;

    /** 本日. */
    public String today;
    /** 次の日. */
    public String tomorrow;

    /**
     * コンストラクタ.
     */
    public ArticleCrawler() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        today = sdf.format(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, 1);
        tomorrow = sdf.format(c.getTime());
    }

    /**
     * 収集開始.
     * @param site 収集元情報.
     * @return 更新があったか否か.
     * @throws CrawlerException Crawler共通例外
     */
    public Boolean crawl(RssEntityBean site) throws CrawlerException {
        log.info("start crawl for feed = " + site.site);
        Boolean updated = false;
        Scraper scrp = null;
        log.info("crawl scraping type = " + site.scraping);
        scrp = ScraperFactory.create(site.scraping);
        ArrayList<ArticleEntityBean> articleList = null;
        try {
            articleList = parseRss(site);
        } catch (Exception e) {
            log.warn("feed parse error : " + e.getMessage());
            throw new CrawlerException(e);
        }
        log.info("newer item count = " + articleList.size());
        for (ArticleEntityBean article : articleList) {
            if (scrp != null) {
                scrp.scraping(article);
            }
            article.save();
            updated = true;
        }
        log.info("end crawl for feed");
        return updated;
    }

    /**
     * RSSの解析処理。
     * @param rss １つのRSS情報
     * @return 解析後のLink情報
     * @throws FeedException Feedの解析失敗時の例外
     * @throws IOException IOException IO例外
     * @throws URISyntaxException URIの形式が不正な場合の例外
     * @throws CrawlerException Crawler共通例外
     */
    @SuppressWarnings("deprecation")
    public ArrayList<ArticleEntityBean> parseRss(RssEntityBean rss) throws IOException, FeedException,
                    URISyntaxException, CrawlerException {
        log.info("start feed parse : " + rss.url);
        SyndFeed feed = null;
        feed = buildSyndFeed(new URI(rss.url));
        ArrayList<ArticleEntityBean> result = new ArrayList<ArticleEntityBean>();
        TimeZone tz = TimeZone.getTimeZone("GMT");
        if (feed != null) {
            List<SyndEntry> entries = feed.getEntries();
            log.info("feed entries count : " + entries.size());
            for (Object obj : entries) {
                SyndEntry entry = (SyndEntry) obj;
                String link = entry.getLink();
                DateTime createdAt = new DateTime(entry.getPublishedDate(), tz);
                String createdAtStr = Util.formatIsoDate(createdAt);
                String title = org.apache.commons.lang.StringEscapeUtils.unescapeXml(entry.getTitle());

                log.info("article : " + title + " / " + createdAtStr + " / " + link);
                if (isExistArticle(link, createdAt)) {
                    log.info("->  article already exists");
                    continue;
                }
                log.info("->  article not exists");

                ArticleEntityBean article = new ArticleEntityBean();
                article.link = link;
                article.title = title;
                article.auther = entry.getAuthor();
                article.url = entry.getUri();
                article.createdAt = createdAt;
                String fixHour = Conf.getValue("fix_time");
                Date now = new Date();
                Date fixDate = new Date(now.getYear(), now.getMonth(), now.getDate(), Integer.parseInt(fixHour), 0);
                if (now.getTime() > fixDate.getTime()) {
                    article.publishedAt = tomorrow;
                } else {
                    article.publishedAt = today;
                }
                article.site = rss.site;
                article.type = rss.type;
                article.description = null;
                SyndContent sc = entry.getDescription();
                if (sc != null) {
                    article.description = sc.getValue();
                    if (rss.replaceCR != null) {
                        article.rawHTML = article.description.replaceAll("\n", rss.replaceCR);
                    } else {
                        article.rawHTML = article.description;
                    }
                }

                article.tags = rss.defaultTag;
                result.add(article);
            }
        } else {
            log.warn("feed parse error url : " + rss.site);
            throw new CrawlerException();
        }
        return result;
    }

    /**
     * Perosnium内の記事(Article)を urlとdateの複合検索し、すでに登録されているか調べる。
     * @param url 記事のURL
     * @param date 記事の作成日時
     * @return true: すでに登録済み、false: 未登録
     * @throws CrawlerException クローラー共通Exception
     */
    protected Boolean isExistArticle(String url, DateTime date) throws CrawlerException {
        String dateStr = Util.formatIsoDate(date);
        PcsModel pcs = PcsModel.getInstance();
        JSONArray list = pcs.findArticle(url, dateStr);
        return !(list.size() == 0);
    }

    /**
     * Feedを取得する.
     * @param uri 対象URL
     * @return 取得したSyndFeed
     * @throws IOException IO例外
     * @throws FeedException Feed解析失敗
     * @throws CrawlerException Crawler共通例外
     */
    protected SyndFeed buildSyndFeed(URI uri) throws IOException, FeedException, CrawlerException {
        InputStream is = null;
        try {
            is = httpget(uri);
        } catch (UnknownHostException e) {
            log.warn("http request failure url : " + uri.toString());
            throw new CrawlerException();
        }
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = null;
        if (is != null) {
            InputStreamReader sr = new InputStreamReader(is, StandardCharsets.UTF_8);
            feed = input.build(sr);
        }
        return feed;
    }

    /**
     * Proxyを利用したGETリクエストを行う.
     * @param uri 対象URL.
     * @return レスポンスボディ
     * @throws IOException IOException
     * @throws CrawlerException Crawler共通例外
     */
    protected InputStream httpget(URI uri) throws IOException, CrawlerException {
        HttpClient httpClient = new HttpClientForProxy();
        HttpGet req = new HttpGet(uri);
        HttpResponse res = httpClient.execute(req);

        int status = res.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_OK) {
            return res.getEntity().getContent();
        } else {
            log.warn("Http error status = " + status);
            throw new CrawlerException();
        }
    }
}
