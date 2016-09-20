package com.kilogramm.mattermost.network;

import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.fromnet.ChannelsWithMembers;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.model.fromnet.ForgotData;
import com.kilogramm.mattermost.model.fromnet.LoginData;

import java.util.List;
import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
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

    @POST("api/v3/users/send_password_reset")
    Observable<ForgotData> forgotPassword(@Body ForgotData forgotData);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/")
    Observable<ChannelsWithMembers> getChannelsTeam(@Path("teamId") String teamId);


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

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/page/0/60")
    Observable<Posts> getPosts(@Path("teamId") String teamId, @Path("channelId") String channelId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/{lastMessageId}/before/0/60")
    Observable<Posts> getPostsBefore(@Path("teamId") String teamId,
                                     @Path("channelId") String channelId,
                                     @Path("lastMessageId") String lastMessageId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channelId}/extra_info")
    Observable<ExtraInfo> getExtraInfoChannel(@Path("teamId") String teamId,
                                              @Path("channelId") String channelId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/{channelId}/posts/create")
    Observable<Post> sendPost(@Path("teamId") String teamId,
                              @Path("channelId") String channelId,
                              @Body Post post);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/{channelId}/update_last_viewed_at")
    Observable<Post> updatelastViewedAt(@Path("teamId") String teamId,
                                      @Path("channelId") String channelId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET ("api/v3/users/profiles_for_dm_list/{teamId}")
    Observable<Map<String, User>> getProfilesForDMList(@Path("teamId") String teamId);

}
