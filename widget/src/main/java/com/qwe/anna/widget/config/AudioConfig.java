package com.qwe.anna.widget.config;

import android.os.Environment;

import com.qwe.anna.widget.util.CommonUtils;

public class AudioConfig {
    public static String TYPE_AUDIO = "audio";
    private static String AUDIO_NAME = "voice";
    private static String DOWNLOAD_AUDIO_NAME = "voice/download";
    //audio
    private static String AUDION_DEFAULT_PATH = Environment.getExternalStorageDirectory()
            + "/" + AUDIO_NAME;
    private static String AUDIO_DOWNLOAD_PATH = Environment.getExternalStorageDirectory()
            + "/" + DOWNLOAD_AUDIO_NAME;

    public static void setAudioPath(String path) {
        AUDION_DEFAULT_PATH = path;
        CommonUtils.makeMediaRootDirectory(AUDION_DEFAULT_PATH);
    }

    public static void setAudioDownloadPath(String path) {
        AUDIO_DOWNLOAD_PATH = path;
        CommonUtils.makeMediaRootDirectory(AUDIO_DOWNLOAD_PATH);
    }

    public static String getAudioPath() {
        return AUDION_DEFAULT_PATH;
    }

    public static String getAudioDownloadPath() {
        return AUDIO_DOWNLOAD_PATH;
    }

}
