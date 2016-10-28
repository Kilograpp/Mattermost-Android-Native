package com.kilogramm.mattermost.tools;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.RealmString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Evgeny on 24.10.2016.
 */
public class NetworkUtil {
    public static Gson createGson(){
        return new GsonBuilder()
                .registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {}.getType(), new TypeAdapter<RealmList<RealmString>>() {

                    @Override
                    public void write(JsonWriter out, RealmList<RealmString> value) throws IOException {
                        out.beginArray();
                        for (RealmString realmString : value) {
                            out.value(realmString.getString());
                        }
                        out.endArray();
                    }

                    @Override
                    public RealmList<RealmString> read(JsonReader in) throws IOException {
                        RealmList<RealmString> list = new RealmList<>();
                        in.beginArray();
                        while (in.hasNext()) {
                            list.add(new RealmString(in.nextString()));
                        }
                        in.endArray();
                        return list;
                    }
                })
                .setLenient()
                .create();
    }
    public static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
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
                .cookieJar(getCookieJar())
                .build();
    }

    @NonNull
    public static CookieJar getCookieJar() {
        return new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                MattermostPreference.getInstance().saveCookies(cookies);
            }
            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = MattermostPreference.getInstance().getCookies();
                return cookies != null ? cookies : new ArrayList<>();
            }
        };
    }
}
