/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */
package jp.fukushima.namie.town;

import java.util.ResourceBundle;

/**
 * 外部設定ファイルのユーティリティクラス。
 */
public class Conf {
    /** リソース。 */
    private static ResourceBundle rb = ResourceBundle.getBundle("crawler");

    /**
     * コンストラクタ.
     */
    private Conf() {
    }

    /**
     * 指定したKeyの値を取得する。
     * @param key 取得するキー
     * @return キーに対応する値
     */
    public static String getValue(String key) {
        return rb.getString(key);
    }
}
