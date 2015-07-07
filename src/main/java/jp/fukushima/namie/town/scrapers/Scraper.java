/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.scrapers;

import jp.fukushima.namie.town.CrawlerException;
import jp.fukushima.namie.town.model.ArticleEntityBean;

/**
 * スクレーパーを表すインターフェイス。
 */
public interface Scraper {
    /**
     * スクレーピングを実行する。
     * @param article 実施対象URL情報を含むアーティクル。
     * @throws CrawlerException Crawler共通例外
     */
    void scraping(ArticleEntityBean article) throws CrawlerException;
}
