package com.kilogramm.mattermost.network.glide;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.tools.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
