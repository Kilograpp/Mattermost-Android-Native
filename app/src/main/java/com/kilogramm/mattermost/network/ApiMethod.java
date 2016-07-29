package com.kilogramm.mattermost.network;

import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.model.fromnet.LoginData;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
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

}
