/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.scrapers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import jp.fukushima.namie.town.Conf;
import jp.fukushima.namie.town.CrawlerException;
import jp.fukushima.namie.town.model.ArticleEntityBean;
import jp.fukushima.namie.town.model.PcsModel;
import net.coobird.thumbnailator.Thumbnails;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * デフォルトのスクレーパー.
 */
public class JugemBlogScraper extends AbstractScraper implements Scraper {
    /** Logger. */
    private static Logger log = LoggerFactory.getLogger(JugemBlogScraper.class);

    @Override
    public void scraping(ArticleEntityBean article) throws CrawlerException {
        try {
            Document doc = readDocument(article.link, "EUC-JP");
            Element body = doc.select(".entryBody").get(0);
            article.description = body.text();
            removeDisuseElements(body);
            List<String> list = splitBody(body.html());
            article.rawHTML = list.get(0);
            article.rawHTML2 = list.get(1);
            article.rawHTML3 = list.get(2);
            Elements images = body.select("img");
            if (images.size() > 0) {
                article.imageUrl = images.get(0).attr("src");
                if (!article.imageUrl.isEmpty()) {
                    String[] filePath = createThumbnail(article.id, article.imageUrl);
                    article.imageThumbUrl = filePath[1];
                    article.imagePath = filePath[0];
                }
            }
        } catch (IOException e) {
            log.warn("web page parse error : " + article.link);
            throw new CrawlerException(e);
        }
    }

    private String[] createThumbnail(String articleId, String sourceUrl) throws CrawlerException {
        int width = Integer.valueOf(Conf.getValue("thumbnail.width"));
        int height = Integer.valueOf(Conf.getValue("thumbnail.height"));

        URL url = null;
        try {
            url = new URL(sourceUrl);
        } catch (MalformedURLException e) {
            log.warn("invalid original pitcure url   : " + e.getMessage());
            throw new CrawlerException(e);
        }
        String fileName = Paths.get(url.getPath()).getFileName().toString();

        byte[] image = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Thumbnails.of(url)
            .size(width, height)
            .keepAspectRatio(true)
            .outputFormat("jpg")
            .toOutputStream(baos);
            image = baos.toByteArray();
        } catch (IOException e) {
            log.warn("thumbnail create error : " + articleId);
            throw new CrawlerException(e);
        }
        InputStream inputStream = new ByteArrayInputStream(image);

        String[] ret;
        PcsModel pcs = PcsModel.getInstance();
        try {
            ret = pcs.putPhoto(articleId, fileName, "image/jpeg", inputStream);
        } catch (CrawlerException e) {
            log.warn("thumbnail put error : " + articleId);
            throw new CrawlerException(e);
        }

        log.info("thumbnail saved. path = " + ret[0] + " / filename = " + ret[1]);
        return ret;
    }
}
