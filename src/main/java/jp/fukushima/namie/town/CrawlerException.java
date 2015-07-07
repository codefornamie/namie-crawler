/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */

package jp.fukushima.namie.town;

/**
 * 共通例外。
 */
public class CrawlerException extends Exception {
    private static final long serialVersionUID = 1L;
    Exception originalException;

    /**
     * コンストラクタ.
     */
    public CrawlerException() {
    }

    /**
     * コンストラクタ.
     * @param e オリジナル例外
     */
    public CrawlerException(Exception e) {
        originalException = e;
    }
}
