/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.scrapers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * スクレイパーファクトリー.
 */
public class ScraperFactory {
    private static Logger log = LoggerFactory.getLogger(ScraperFactory.class);

    private ScraperFactory() {
    }

    /**
     * スクレイパーのインスタンスを取得する。
     * @param type スクレイパーのタイプ
     * @return スクレイパーのインスタンス。
     */
    public static Scraper create(String type) {
        log.debug("type: " + type);
        Scraper scrp = null;
        if (type == null) {
            return null;
        }
        if (type.equals("namie_news")) {
            scrp = new NamieNewsScraper();
        } else if (type.equals("blog_jugem")) {
            scrp = new JugemBlogScraper();
        } else if (type.equals("minpo")) {
            scrp = new MinpoScraper();
        } else {
            scrp = null;
        }
        return scrp;
    }
}
