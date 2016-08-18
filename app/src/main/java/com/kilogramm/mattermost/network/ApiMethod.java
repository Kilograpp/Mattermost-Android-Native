package com.kilogramm.mattermost.network;

import android.databinding.ObservableInt;

import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.model.fromnet.Channels;
import com.kilogramm.mattermost.model.fromnet.LoginData;
import com.kilogramm.mattermost.model.fromnet.UserStatusList;

import java.util.List;
import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Evgeny on 25.07.2016.
 */
public interface ApiMethod {

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/users/initial_load")
    Observable<InitObject> initLoad();

    @POST("api/v3/users/login")
    Observable<User> login(@Body LoginData loginData);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/")
    Observable<Channels> getChannelsTeam(@Path("teamId") String teamId);


    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/users/profiles/{userId}")
    Observable<Channel> getDirectChannels(@Path("userId") String userId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/users/status")
    Observable<Map<String,String>> getStatus(@Body List<String> userIds);


}
