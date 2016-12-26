package com.kilogramm.mattermost.network;

import android.content.Context;
import android.util.Log;

import com.kilogramm.mattermost.tools.NetworkUtil;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Evgeny on 23.08.2016.
 */
public class PicassoService {

    public static void create(Context context) throws IllegalArgumentException {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        HttpLoggingInterceptor headerInterceprion = new HttpLoggingInterceptor();
        headerInterceprion.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = NetworkUtil.createOkHttpClient();

        Picasso.setSingletonInstance(new Picasso.Builder(context)
                .listener((picasso, uri, exception) -> {
                    exception.printStackTrace();
                    Log.d("PICASSO", exception.getMessage());
                })
                .downloader(new OkHttp3Downloader(client))
                .memoryCache(new LruCache(context))
                .build());
    }
}
