/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Crawlerファクトリー.
 */
public class CrawlerFactory {
    private static Logger log = LoggerFactory.getLogger(CrawlerFactory.class);

    /**
     * コンストラクタ.
     */
    private CrawlerFactory() {
    }

    /**
     * Crawlerのインスタンスを取得する。
     * @param type 記事種別.
     * @return Crawlerのインスタンス。
     */
    public static ArticleCrawler create(String type) {
        log.debug("type: " + type);
        ArticleCrawler crawler = null;
        if (type == null) {
            return null;
        }
        if ((type.equals("1")) || (type.equals("8"))) {
            crawler = new ArticleCrawler();
        } else if (type.equals("2")) {
            crawler = new YoutubeCrawler();
        } else if (type.equals("7")) {
            crawler = new FacebookCrawler();
        }

        return crawler;
    }
}
