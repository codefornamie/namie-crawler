/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import jp.fukushima.namie.town.model.ArticleEntityBean;
import jp.fukushima.namie.town.model.RssEntityBean;

/**
 * Youtubeの収集クラス.
 */
public class YoutubeCrawler extends ArticleCrawler {
    private final long maxResult = 10L;

    /** Logger. */
    private static Logger log = LoggerFactory.getLogger(YoutubeCrawler.class);

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    @SuppressWarnings("deprecation")
    @Override
    public Boolean crawl(RssEntityBean site) throws CrawlerException {
        log.info("start YouTube crawl channel id = " + site.url);
        Boolean updated = false;
        YouTube youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("youtube-cmdline-search-sample").build();

        try {
            YouTube.Search.List search = youtube.search().list("id,snippet");
            String apiKey = Conf.getValue("youtube.appkey");
            search.setKey(apiKey);
            search.setType("video");
            search.setOrder("date");
            search.setMaxResults(maxResult);
            search.setChannelId(site.url);

            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            log.info("search response video count = " + searchResultList.size());
            for (SearchResult result : searchResultList) {

                SearchResultSnippet snippet = result.getSnippet();
                DateTime publishDate = snippet.getPublishedAt();
                DateTime lastCrawlDate = null;
                if (site.lastCrawl != null) {
                    lastCrawlDate = new DateTime(site.lastCrawl);
                }
                if ((lastCrawlDate == null) || (publishDate.getValue() > lastCrawlDate.getValue())) {
                    YouTube.Videos.List videolist = youtube.videos().list("id,snippet");
                    videolist.setId(result.getId().getVideoId());
                    videolist.setKey(apiKey);
                    VideoListResponse videoListResponse = videolist.execute();
                    List<Video> videos = videoListResponse.getItems();
                    if (videos.size() != 1) {
                        log.warn("video not found : " + videolist.getId());
                        continue;
                    }
                    Video video = videos.get(0);

                    ArticleEntityBean article = new ArticleEntityBean();
                    article.link = video.getId();
                    article.title = snippet.getTitle();
                    article.description = snippet.getDescription();
                    article.imageUrl = snippet.getThumbnails().getDefault().getUrl();
                    article.url = site.url;
                    article.createdAt = snippet.getPublishedAt();
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
                    log.info("save article video id = " + article.link + " / video title = " + article.title);
                    article.save();
                    updated = true;
                }
            }
        } catch (IOException e) {
            log.warn("youtube api error : " + e.getMessage());
            throw new CrawlerException(e);
        }
        log.info("end YouTube crawl");
        return updated;
    }
}
