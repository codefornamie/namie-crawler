/**
 * Copyright (C) 2014 Namie Town. All Rights Reserved.
 */

package jp.fukushima.namie.town;

import org.junit.runners.model.InitializationError;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;

/**
 * テストRunnnerクラス.
 */
public class CrawlerRunner extends BlockJUnit4ClassRunner {
    /**
     * コンストラクタ.
     * @param klass klass
     * @throws InitializationError 例外.
     * @throws org.junit.runners.model.InitializationError 例外.
     */
    public CrawlerRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    /**
     * This method calls its overridden version of super class.
     * @param method method.
     * @param notifier notifier.
     */
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        System.out.println("■■■■ " + method.getName() + " ■■■■");
        super.runChild(method, notifier);
    }
}
