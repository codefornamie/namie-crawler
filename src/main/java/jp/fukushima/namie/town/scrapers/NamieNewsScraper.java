/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.scrapers;

import java.io.IOException;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.fukushima.namie.town.CrawlerException;
import jp.fukushima.namie.town.model.ArticleEntityBean;

/**
 * 「浪江町役場新着情報」のためのスクレイパー.
 */
public class NamieNewsScraper extends AbstractScraper implements Scraper {
    private static Logger log = LoggerFactory.getLogger(MinpoScraper.class);

    @Override
    public void scraping(ArticleEntityBean article) throws CrawlerException {
        try {
            readDocument(article.link, "utf-8");
            Elements contents = null;
            contents = doc.select("#main_body");
            if ((contents == null) || (contents.size() == 0)) {
                contents = doc.select("#kosodateBody");
            }

            Element body = null;
            if (contents.size() > 0) {
                body = contents.get(0);
                article.description = body.text();
                // 不要な要素の削除
                removeDisuseElements(body);
                // PDFの説明文の削除
                removeDisuseElementsFromNamieNews(body, "【PDFファイル】", "www.adobe.co.jp/products/acrobat");
                // Wordの説明文の削除
                removeDisuseElementsFromNamieNews(body, "【Wordファイル】", "http://support.microsoft.com/");
                // Excelの説明文の削除
                removeDisuseElementsFromNamieNews(body, "【Excelファイル】", "http://support.microsoft.com/");
                // 「印刷用ページを表示する」を削除
                removeElement(body.getElementById("print_mode_link"));
                // imageタグの相対パスを絶対パスに
                convertImagePath(body);
                //
                List<String> list = splitBody(body.html());
                article.rawHTML = list.get(0);
                article.rawHTML2 = list.get(1);
                article.rawHTML3 = list.get(2);
            }
        } catch (IOException e) {
            log.warn("web page parse error : " + article.link);
            throw new CrawlerException(e);
        }
    }

    /**
     * 不要記事の削除.
     * @param parent 対象のトップ要素
     * @param key1 条件キー１
     * @param key2 条件キー２
     */
    void removeDisuseElementsFromNamieNews(Element parent, String key1, String key2) {
        Elements elements = parent.getElementsContainingOwnText(key1);
        for (Element elm : elements) {
            Elements children = elm.getElementsByAttributeValueContaining("href", key2);
            if (children.size() > 0) {
                elm.remove();
            }
        }
    }
}
