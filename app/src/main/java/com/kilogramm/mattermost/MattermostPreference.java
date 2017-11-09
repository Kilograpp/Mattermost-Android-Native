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
    private static final String AUTH_TOKEN = "auth_token";
    private static final String LAST_CHANNEL_ID = "last_channel_id";
    private static final String TEAM_ID = "team_id";
    private static final String SITE_NAME = "site_name";
    private static final String MY_E_MAIL = "my_e_mail";

    public MattermostPreference(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static synchronized MattermostPreference createInstance(Context context) {
        if (instance == null)
            instance = new MattermostPreference(context);

        return instance;
    }

    public String getMyEMail() {
        return sharedPreferences.getString(MY_E_MAIL, null);
    }

    public void setMyEMail(String email) {
        sharedPreferences.edit().putString(MY_E_MAIL, email).apply();
    }

    public String getSiteName(){
        return sharedPreferences.getString(SITE_NAME,null);
    }

    public void setSiteName(String teamId){
        sharedPreferences.edit().putString(SITE_NAME, teamId).apply();
    }

    public String getTeamId(){
        return sharedPreferences.getString(TEAM_ID, null);
    }

    public void setTeamId(String teamId){
        sharedPreferences.edit().putString(TEAM_ID, teamId).apply();
    }

    public String getBaseUrl() {
        return sharedPreferences.getString(BASE_URL, null);
    }

    public void setBaseUrl(String baseUrl) {
        sharedPreferences.edit().putString(BASE_URL, baseUrl).apply();
    }

    public void setLastChannelId(String id) {
        sharedPreferences.edit().putString(LAST_CHANNEL_ID, id).apply();
    }

    public String getLastChannelId() {
        return sharedPreferences.getString(LAST_CHANNEL_ID, null);
    }

    public String getAuthToken() {
        return sharedPreferences.getString(AUTH_TOKEN, null);
    }

    public void setAuthToken(String authToken) {
        sharedPreferences.edit().putString(AUTH_TOKEN, authToken).apply();
    }

    public String getMyUserId() {
        return sharedPreferences.getString(MY_USER_ID, null);
    }

    public void setMyUserId(String id) {
        sharedPreferences.edit().putString(MY_USER_ID, id).apply();
    }

    public void saveCookies(List<Cookie> cookies) {
        String json = (new Gson()).toJson(cookies);
        setAuthToken(cookies.get(0).value());
        sharedPreferences.edit().putString(COOKIES, json).apply();
        Log.d(TAG, json);
    }

    public List<Cookie> getCookies() {
        String json = sharedPreferences.getString(COOKIES, null);
        Type type = new TypeToken<List<Cookie>>() {
        }.getType();
        if (json != null) {
            return new Gson().fromJson(json, type);
        } else {
            return null;
        }
    }

    public static synchronized MattermostPreference getInstance() {
        return instance;
    }


}
