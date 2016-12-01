package com.kilogramm.mattermost.network;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.tools.NetworkUtil;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Evgeny on 21.07.2016.
 */
public class MattermostRetrofitService {


    public static final int TIMEOUT = 40;

    public static ApiMethod create() throws IllegalArgumentException {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        HttpLoggingInterceptor headerInterception = new HttpLoggingInterceptor();
        headerInterception.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                // Caused twice writeTo() method call for uploading file
                //TODO release version comment this line
//                .addInterceptor(logging)
                .addInterceptor(getAuthInterceptor())

                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT,TimeUnit.SECONDS)
                .addNetworkInterceptor(new StethoInterceptor())
                .cookieJar(NetworkUtil.getCookieJar())
                .build();

        Gson gson = NetworkUtil.createGson();

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://"+MattermostPreference.getInstance().getBaseUrl() + "/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            return retrofit.create(ApiMethod.class);
        } catch (IllegalArgumentException e){
            throw e;
        }
    }

    public static ApiMethod refreshRetrofitService(){
        return create();
    }


    public static Interceptor getAuthInterceptor(){
        return chain -> {
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
        };
    }
}
