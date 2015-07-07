/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.model;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import jp.fukushima.namie.town.Conf;
import jp.fukushima.namie.town.CrawlerException;
import jp.fukushima.namie.town.Util;

import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fujitsu.dc.client.Accessor;
import com.fujitsu.dc.client.DaoException;
import com.fujitsu.dc.client.DavCollection;
import com.fujitsu.dc.client.DcContext;
import com.fujitsu.dc.client.ODataCollection;

/**
 * PCSへのアクセスモデル.
 */
public class PcsModel {
    /** Logger. */
    private static Logger log = LoggerFactory.getLogger(PcsModel.class);

    private String baseUrl = Conf.getValue("pcs.base");
    private String cellName = Conf.getValue("pcs.cell");
    private String schema = "";
    private String boxName = Conf.getValue("pcs.box");
    private String userId = Conf.getValue("pcs.user");
    private String password = Conf.getValue("pcs.password");
    private DcContext dc = null;
    private ODataCollection odata = null;
    private DavCollection dav = null;

    private static PcsModel instance = new PcsModel();

    /**
     * privateコンストラクタ.
     */
    private PcsModel() {
        dc = new DcContext(baseUrl, cellName, schema, boxName);
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (proxyHost != null) {
            dc.getDaoConfig().setProxyHostname(proxyHost);
        }
        if (proxyPort != null) {
            dc.getDaoConfig().setProxyPort(Integer.valueOf(proxyPort));
        }
        // dc.setHttpClient(new HttpClientForProxy());
    }

    /**
     * Singletone用インスタンス生成.
     * @return インスタンス
     */
    public static PcsModel getInstance() {
        return instance;
    }

    /**
     * ODataCollectionの取得を行う.
     * @return ODataCollection
     * @throws CrawlerException Crawler共通例外
     */
    private ODataCollection getODataCollection() throws CrawlerException {
        Accessor ac = null;
        // to-do : 本来は再認証の処理が必要
        if (odata != null) {
            return odata;
        }
        try {
            ac = dc.getAccessorWithAccount(cellName, userId, password);
            odata = ac.cell().box().odata("odata");
            dav = ac.cell().box().col("dav");
        } catch (DaoException e) {
            log.warn("server access error : " + e.getCode());
            throw new CrawlerException(e);
        }
        return odata;
    }

    /**
     * ODataの一覧取得.
     * @param entityType 対象のエンティティタイプ
     * @return 取得した配列.
     * @throws CrawlerException Crawler共通例外
     */
    public JSONArray list(String entityType) throws CrawlerException {
        JSONArray list = null;
        try {
            odata = getODataCollection();
            JSONObject json = (JSONObject) odata.entitySet(entityType).query().run();
            list = (JSONArray) ((JSONObject) json.get("d")).get("results");
        } catch (DaoException e) {
            log.warn("odata search error : " + e.getCode());
            throw new CrawlerException(e);
        }
        return list;
    }

    /**
     * 最終クロール日の更新.
     * @param id 更新対象のODataのID.
     * @param date 更新する日付.
     * @throws CrawlerException Crawler共通例外
     */
    public void updateLastCrawl(String id, Date date) throws CrawlerException {
        HashMap<String, Object> hash = new HashMap<String, Object>();
        hash.put("lastCrawl", Util.formatIsoDate(date));

        odata = getODataCollection();
        try {
            odata.entitySet("rss").merge(id, hash, "*");
        } catch (DaoException e) {
            log.warn("lastCrawl field update error : " + e.getCode());
            throw new CrawlerException(e);
        }
    }

    /**
     * POST.
     * @param entityType 登録対象のエンティティタイプ
     * @param json 登録対象のデータ.
     * @throws CrawlerException Crawler共通例外
     */
    public void post(String entityType, HashMap<String, Object> json) throws CrawlerException {
        odata = getODataCollection();
        try {
            odata.entitySet(entityType).createAsJson(json);
        } catch (DaoException e) {
            log.warn("odata create error : " + e.getCode());
            throw new CrawlerException(e);
        }
    }

    /**
     * ファイルをWebDAVに保存する。
     * @param articleId 対象記事ID
     * @param fileName ファイル名
     * @param type ファイル種別
     * @param is ファイル本体
     * @return 0番目の要素にファイルのパス、1番目の要素にファイル名を入れた配列
     * @throws CrawlerException Crawler共通例外
     */
    public String[] putPhoto(String articleId, String fileName, String type, InputStream is) throws CrawlerException {
        Calendar calendar = Calendar.getInstance();
        String yearCol = String.valueOf(calendar.get(Calendar.YEAR));
        String monthCol = String.format("%02d-%02d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE));

        DavCollection subCol = null;
        try {
            dav.mkCol(yearCol);
        } catch (DaoException e) {
            if (!e.getCode().equals(String.valueOf(HttpStatus.SC_METHOD_NOT_ALLOWED))) {
                log.warn("webdav request error (" + yearCol + ") : " + e.getCode());
                throw new CrawlerException(e);
            }
        }
        try {
            subCol = dav.col(yearCol);
            subCol.mkCol(monthCol);
        } catch (DaoException e) {
            if (!e.getCode().equals(String.valueOf(HttpStatus.SC_METHOD_NOT_ALLOWED))) {
                log.warn("webdav request error (" + monthCol + ") : " + e.getCode());
                throw new CrawlerException(e);
            }
        }
        try {
            subCol = subCol.col(monthCol);
            subCol.mkCol(articleId);
        } catch (DaoException e) {
            if (!e.getCode().equals(String.valueOf(HttpStatus.SC_METHOD_NOT_ALLOWED))) {
                log.warn("webdav request error (" + articleId + "): " + e.getCode());
                throw new CrawlerException(e);
            }
        }

        subCol = subCol.col(articleId);
        try {
            subCol.put(fileName, type, is, "*");
        } catch (DaoException e) {
            log.warn("webdav put error : " + e.getCode());
            throw new CrawlerException(e);
        }
        String[] ret = {yearCol + "/" + monthCol + "/" + articleId, fileName};
        return ret;
    }

    /**
     * 記事(Article)を urlとdateの複合検索する。
     * @param url 記事のURL
     * @param dateStr 記事の作成日時文字列
     * @return true: すでに登録済み、false: 未登録
     * @throws CrawlerException クローラー共通Exception
     */
    public JSONArray findArticle(String url, String dateStr) throws CrawlerException {
        JSONArray list = null;
        try {
            String filter = "link eq '" + url + "' and createdAt eq '" + dateStr + "'";
            odata = getODataCollection();
            JSONObject json = (JSONObject) odata.entitySet("article").query().filter(filter).run();
            list = (JSONArray) ((JSONObject) json.get("d")).get("results");
        } catch (DaoException e) {
            log.warn("article search error : " + e.getCode());
            throw new CrawlerException(e);
        }
        return list;
    }
}
