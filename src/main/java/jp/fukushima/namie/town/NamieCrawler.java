/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.fukushima.namie.town.model.RadiantionModel;
import jp.fukushima.namie.town.model.RadiationBean;
import jp.fukushima.namie.town.model.RssEntityBean;
import jp.fukushima.namie.town.model.RssEntityModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fujitsu.dc.client.utils.DcLoggerFactory;

/**
 * メインクラス.
 */
public class NamieCrawler {
    /** Logger. */
    private static Logger log = LoggerFactory.getLogger(NamieCrawler.class);

    /** クロールの開始時刻. */
    private Date processStarted;

    /**
     * Main.
     * @param args 引数
     */
    public static void main(String[] args) {
        PcsLoggerFactory pcsLoggerFactory = new PcsLoggerFactory();
        DcLoggerFactory.setDefaultFactory(pcsLoggerFactory);

        if (args.length == 0) {
            log.error("need parameter 'rss' or 'radiation'");
            return;
        }
        String cmd = args[0];
        NamieCrawler crawler = new NamieCrawler();

        if (cmd.equals("rss")) {
            crawler.crawlRss();
        } else if (cmd.equals("radiation")) {
            crawler.crawlRadiation();
        } else {
            log.error("need parameter 'rss' or 'radiation'");
        }
    }

    /**
     * 処理開始.
     */
    public void crawlRss() {
        log.info("process start");
        processStarted = new Date();
        boolean hasError = false;

        RssEntityModel targets = new RssEntityModel();
        ArrayList<RssEntityBean> siteList;
        try {
            siteList = targets.list();
        } catch (CrawlerException e1) {
            log.info("process failure.");
            return;
        }

        for (RssEntityBean site : siteList) {
            if (site.deletedAt != null) {
                // 論理削除済み
                continue;
            }

            boolean updated = false;
            try {
                // 収集処理.
                ArticleCrawler crawler = CrawlerFactory.create(site.type);
                if (crawler == null) {
                    log.warn("invalid article type of the inside rss entity : " + site.type);
                    continue;
                }
                updated = crawler.crawl(site);
            } catch (CrawlerException e) {
                hasError = true;
            }

            // 新記事があった場合、最終更新日を更新する.
            if (updated) {
                site.lastCrawl = processStarted;
                try {
                    site.update();
                } catch (CrawlerException e) {
                    hasError = true;
                }
            }
        }
        if (hasError) {
            log.info("process failure.");
        } else {
            log.info("process success.");
        }
    }

    /**
     * 放射線量データのクロール.
     */
    @SuppressWarnings("deprecation")
    public void crawlRadiation() {
        log.info("process start");
        boolean hasError = false;
        RadiantionModel radiantion = new RadiantionModel();
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(today);
        String hourStr = String.valueOf(today.getHours());
        log.info("target date = " + dateStr + "  , target hour = " + hourStr);
        try {
            ArrayList<RadiationBean> ar = radiantion.read(dateStr, hourStr);
            log.info("radiation csv record count = " + String.valueOf(ar.size()));
            for (RadiationBean item : ar) {
                item.save();
            }
        } catch (CrawlerException e) {
            hasError = true;
        } catch (IOException e) {
            log.error("radiation read exception : " + e.getMessage());
            hasError = true;
        }
        if (hasError) {
            log.info("process failure.");
        } else {
            log.info("process success.");
        }
    }
}
