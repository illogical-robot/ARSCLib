/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.apk.framework;

import android.os.Build;
import android.os.Environment;

import com.reandroid.apk.FrameworkApk;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;

import java.io.File;
import java.io.IOException;

/**
 * Framework provider backed by the running device's own {@code framework-res.apk}.
 * The only framework available is the one matching {@link Build.VERSION#SDK_INT}.
 */
public class DeviceFrameworks extends FrameworkManager {

    public static final DeviceFrameworks INSTANCE = new DeviceFrameworks();

    private static final String FRAMEWORK_RES_PATH = "framework/framework-res.apk";

    private File frameworkFile;
    private FrameworkApk frameworkApk;

    private DeviceFrameworks() {
        super();
    }

    public File getFrameworkFile() {
        synchronized (this) {
            File file = frameworkFile;
            if (file == null) {
                file = new File(Environment.getRootDirectory(), FRAMEWORK_RES_PATH);
                frameworkFile = file;
            }
            return file;
        }
    }
    public void setFrameworkFile(File frameworkFile) {
        synchronized (this) {
            this.frameworkFile = frameworkFile;
            this.frameworkApk = null;
        }
    }

    @Override
    public FrameworkApk get(int version) {
        if (version == getLatestVersion()) {
            return getLatest();
        }
        return null;
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
        return Build.VERSION.SDK_INT;
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
        FrameworkApk frameworkApk = FrameworkApk.loadApkFile(getFrameworkFile());
        int version = getLatestVersion();
        // framework-res.apk is not an optimized framework, so the table carries no version
        frameworkApk.getTableBlock().setVersionCode(version);
        if (frameworkApk.getVersionCode() == 0 && frameworkApk.hasAndroidManifest()) {
            AndroidManifestBlock manifest = frameworkApk.getAndroidManifest();
            manifest.setVersionCode(version);
            frameworkApk.setManifest(manifest);
        }
        return frameworkApk;
    }
}
