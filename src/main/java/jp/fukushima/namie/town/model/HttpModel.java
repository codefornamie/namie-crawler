/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.fukushima.namie.town.CrawlerException;

/**
 * HTTP通信用クラス.
 */
public class HttpModel {
    /** Logger. */
    private static Logger log = LoggerFactory.getLogger(PcsModel.class);

    HttpURLConnection connection;

    private static HttpModel instance = new HttpModel();

    /**
     * Singletone用インスタンス生成.
     * @return インスタンス
     */
    public static HttpModel getInstance() {
        return instance;
    }

    /**
     * HTTP POST.
     * @param urlString URL文字列.
     * @param body リクエストボディ.
     * @return BufferedReader
     * @throws IOException IOException.
     */
    public BufferedReader httpPost(String urlString, String body) throws IOException {
        URL url = null;
        url = new URL(urlString);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        PrintWriter printWriter = null;
        printWriter = new PrintWriter(connection.getOutputStream());
        printWriter.print(body);
        printWriter.close();

        BufferedReader bufferReader = null;
        bufferReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "JISAutoDetect"));
        return bufferReader;
    }

    /**
     * HTTP GET.
     * @param urlString URL文字列.
     * @return BufferedReader
     * @throws IOException IOException.
     */
    public BufferedReader httpGetAsString(String urlString) throws IOException {
        URL url = null;
        url = new URL(urlString);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "JISAutoDetect"));
        return br;
    }

    /**
     * HTTP切断.
     */
    public void disconnect() {
        connection.disconnect();
    }

    /**
     * Httpから画像データを取得し、WebDAVに保存する。
     * @param articleId 記事ID
     * @param pictureUrl 画像URL
     * @return 0番目の要素にファイルのパス、1番目の要素にファイル名を入れた配列
     * @throws CrawlerException CrawlerException
     */
    public String[] putPicture(String articleId, String pictureUrl) throws CrawlerException {
        return putPicture(articleId, pictureUrl, null, null);
    }

    /**
     * Httpから画像データを取得し、WebDAVに保存する。
     * @param articleId 記事ID
     * @param pictureUrl 画像URL
     * @param fName 画像ファイル名
     * @param referer リファラヘッダに指定する値
     * @return 0番目の要素にファイルのパス、1番目の要素にファイル名を入れた配列
     * @throws CrawlerException CrawlerException
     */
    public String[] putPicture(String articleId, String pictureUrl, String fName, String referer)
                    throws CrawlerException {
        URL url = null;
        try {
            url = new URL(pictureUrl);
        } catch (MalformedURLException e) {
            log.warn("invalid original pitcure url   : " + e.getMessage());
            throw new CrawlerException(e);
        }
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        if (fName != null) {
            fileName = fName;
        }

        HttpURLConnection conn = null;
        String[] ret;
        try {
            conn = (HttpURLConnection) url.openConnection();
            if (referer != null) {
                conn.setRequestProperty("REFERER", referer);
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Accept-Encoding", "gzip");
            }
        } catch (IOException e) {
            log.warn("original picture url connection error : " + e.getMessage());
            throw new CrawlerException(e);
        }
        try {
            conn.connect();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        // CHECKSTYLE:OFF
        try (InputStream in = conn.getInputStream()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                bos.write(b);
            }
            byte[] data = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            PcsModel pcs = PcsModel.getInstance();
            ret = pcs.putPhoto(articleId, fileName, conn.getContentType(), bis);
        } catch (IOException e) {
            log.warn("picture file parse error articleId(" + articleId + ")  : " + e.getMessage());
            throw new CrawlerException(e);
        }
        // CHECKSTYLE:ON
        return ret;
    }
}
