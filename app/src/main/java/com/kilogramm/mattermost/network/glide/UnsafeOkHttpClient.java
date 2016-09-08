package com.kilogramm.mattermost.network.glide;

import com.kilogramm.mattermost.MattermostPreference;

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
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token;
                    if((token = MattermostPreference.getInstance().getAuthToken())!=null){
                        Request request = original.newBuilder()
                                .addHeader("Authorization","Bearer " + token)
                                .build();
                        return chain.proceed(request);
                    } else {
                        return chain.proceed(original);
                    }
                })
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        MattermostPreference.getInstance().saveCookies(cookies);
                    }
                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = MattermostPreference.getInstance().getCookies();
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                })
                .build();
        return client;
    }

}
