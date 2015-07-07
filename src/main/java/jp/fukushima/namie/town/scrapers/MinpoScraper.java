/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.scrapers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.fukushima.namie.town.CrawlerException;
import jp.fukushima.namie.town.model.ArticleEntityBean;
import jp.fukushima.namie.town.model.HttpModel;

/**
 * 「福島民報」のためのスクレイパー.
 */
public class MinpoScraper extends AbstractScraper implements Scraper {
    private static Logger log = LoggerFactory.getLogger(MinpoScraper.class);

    @Override
    public void scraping(ArticleEntityBean article) throws CrawlerException {
        HttpModel http = HttpModel.getInstance();
        try {
            // URLをオープン
            readDocument(article.link, "utf-8");

            // 本文の要素を取得
            Element body = doc.select("article").get(0);

            // 本文のテキスト
            article.description = body.text();

            // imageタグの相対パスを絶対パスに
            Elements images = body.getElementsByTag("img");
            String src = "";
            URL newUrl;
            int len = images.size();
            for (int i = 0; i < len; i++) {
                Element elm = images.get(i);
                src = elm.attr("src");
                src = src.replaceAll("&amp;", "&");
                try {
                    newUrl = new URL(articleUrl, src);
                } catch (MalformedURLException e) {
                    log.warn("invalid image url for scraping : " + src);
                    throw new CrawlerException(e);
                }
                String fileName = UUID.randomUUID() + ".jpg";
                String[] filePath = http.putPicture(article.id, newUrl.toString(), fileName, "http://www.minpo.jp/");
                if (filePath.length != 0) {
                    article.imagePath = filePath[0];
                    if (i == 0) {
                        article.imageUrl = filePath[1];
                        article.imageThumbUrl = filePath[1];
                    } else if (i == 1) {
                        article.imageUrl2 = filePath[1];
                    } else if (i == 2) {
                        article.imageUrl3 = filePath[1];
                    }
                    elm.attr("src", filePath[1]);
                }
            }

            // raw
            List<String> list = splitBody(body.html());
            article.rawHTML = list.get(0);
            article.rawHTML2 = list.get(1);
            article.rawHTML3 = list.get(2);
        } catch (IOException e) {
            log.warn("web page parse error : " + article.link);
            throw new CrawlerException(e);
        }
    }

}
