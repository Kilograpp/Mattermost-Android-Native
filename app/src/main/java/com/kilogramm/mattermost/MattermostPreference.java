package com.kilogramm.mattermost;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Cookie;

/**
 * Created by Evgeny on 21.07.2016.
 */
public class MattermostPreference {

    private static final String TAG = "MattermostPreference";

    private static MattermostPreference instance;
    private static SharedPreferences sharedPreferences;

    private static final String PREFERENCES = "mattermost_preference";
    private static final String BASE_URL = "base_url";
    private static final String MY_USER_ID = "my_user_id";
    private static final String COOKIES = "cookies";

    public MattermostPreference(Context context){
        sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static synchronized MattermostPreference createInstance(Context context) {
        if (instance == null)
            instance = new MattermostPreference(context);

        return instance;
    }

    public String getBaseUrl(){
        return sharedPreferences.getString(BASE_URL,null);
    }
    public void setBaseUrl(String baseUrl){
        sharedPreferences.edit().putString(BASE_URL, baseUrl).apply();
    }

    public String getMyUserId() { return sharedPreferences.getString(MY_USER_ID, null);}
    public void setMyUserId(String id){
        sharedPreferences.edit().putString(MY_USER_ID, id).apply();
    }

    public void saveCookies(List<Cookie> cookies) {
        String json = (new Gson()).toJson(cookies);
        sharedPreferences.edit().putString(COOKIES, json).apply();
        Log.d(TAG, json);
    }

    public List<Cookie> getCookies(){
        String json = sharedPreferences.getString(COOKIES, null);
        Type type = new TypeToken<List<Cookie>>(){}.getType();
        if(json != null) {
            return new Gson().fromJson(json, type);
        } else {
            return null;
        }
    }

    public static synchronized MattermostPreference getInstance() {
        return instance;
    }


}
