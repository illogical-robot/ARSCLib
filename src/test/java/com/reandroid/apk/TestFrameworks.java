package com.reandroid.apk;

import com.reandroid.apk.framework.FrameworkManager;

import java.io.File;
import java.io.IOException;

public class TestFrameworks extends FrameworkManager {

    public static final TestFrameworks INSTANCE = new TestFrameworks();

    private static final String PROPERTY_PATH = "arsclib.test.framework";
    private static final String PROPERTY_VERSION = "arsclib.test.framework.version";

    private FrameworkApk frameworkApk;

    private TestFrameworks() {
        super();
    }

    public static void install() {
        AndroidFrameworks.setFrameworkManager(INSTANCE);
    }

    @Override
    public FrameworkApk get(int version) {
        return version == getLatestVersion() ? getLatest() : null;
    }

    @Override
    public FrameworkApk getBestMatch(int version) {
        return getLatest();
    }

    @Override
    public Integer getNearestVersion(int version) {
        return getLatestVersion();
    }

    @Override
    public Integer getLatestVersion() {
        return Integer.getInteger(PROPERTY_VERSION, 0);
    }

    @Override
    public FrameworkApk getLatest() {
        synchronized (this) {
            FrameworkApk frameworkApk = this.frameworkApk;
            if (frameworkApk == null || frameworkApk.isDestroyed()) {
                try {
                    frameworkApk = load();
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }
                this.frameworkApk = frameworkApk;
                if (getCurrent() == null) {
                    setCurrent(frameworkApk);
                }
            }
            return frameworkApk;
        }
    }

    private FrameworkApk load() throws IOException {
        String path = System.getProperty(PROPERTY_PATH);
        if (path == null) {
            throw new IOException("Missing system property: " + PROPERTY_PATH);
        }
        FrameworkApk frameworkApk = FrameworkApk.loadApkFile(new File(path));
        frameworkApk.getTableBlock().setVersionCode(getLatestVersion());
        return frameworkApk;
    }
}
