package com.kilogramm.mattermost.network.glide;

import com.kilogramm.mattermost.tools.NetworkUtil;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Evgeny on 07.09.2016.
 */
public class UnsafeOkHttpClient {

    public static OkHttpClient getUnsafeOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        HttpLoggingInterceptor headerInterceprion = new HttpLoggingInterceptor();
        headerInterceprion.setLevel(HttpLoggingInterceptor.Level.BODY);
        return NetworkUtil.createOkHttpClient();
    }

}
