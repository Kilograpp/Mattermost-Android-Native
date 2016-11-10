package com.kilogramm.mattermost.network;

import com.kilogramm.mattermost.model.entity.FileUploadResponse;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.SaveData;
import com.kilogramm.mattermost.model.entity.SearchParams;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.notifyProps.NotifyUpdate;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostEdit;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.fromnet.ChannelsWithMembers;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.model.fromnet.ForgotData;
import com.kilogramm.mattermost.model.fromnet.ListInviteObj;
import com.kilogramm.mattermost.model.fromnet.LoginData;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.presenter.channel.AddMembersPresenter;
import com.kilogramm.mattermost.presenter.channel.HeaderPresenter;
import com.kilogramm.mattermost.presenter.channel.PurposePresenter;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import rx.Observable;

/**
 * Created by Evgeny on 25.07.2016.
 */
// TODO некоторые методы не используются, почистить (by Kepar)
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
    @GET("api/v3/users/profiles/{teamId}")
    Observable<Map<String, User>> getTeamUsers(@Path("teamId") String teamId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/users/status")
    Observable<Map<String, String>> getStatus(@Body List<String> userIds);

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
    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/{id}/get")
    Observable<Posts> getPost(@Path("teamId") String teamId, @Path("channelId") String channelId, @Path("id") String id);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/{lastMessageId}/before/0/{limit}")
    Observable<Posts> getPostsBefore(@Path("teamId") String teamId,
                                     @Path("channelId") String channelId,
                                     @Path("lastMessageId") String lastMessageId,
                                     @Path("limit") String limit);

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
    @GET("api/v3/users/profiles_for_dm_list/{teamId}")
    Observable<Map<String, User>> getProfilesForDMList(@Path("teamId") String teamId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/users/logout")
    Observable<LogoutData> logout(@Body Object object);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/{channelId}/posts/{postId}/delete")
    Observable<Post> deletePost(@Path("teamId") String teamId,
                                @Path("channelId") String channelId,
                                @Path("postId") String psotId,
                                @Body Object object);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/{channelId}/posts/updateMembers")
    Observable<Post> editPost(@Path("teamId") String teamId,
                              @Path("channelId") String channelId,
                              @Body PostEdit post);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/users/direct_profiles")
    Observable<Map<String, User>> getDirectProfile();


    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest"})
    @Multipart
    @POST("api/v3/teams/{team_id}/files/upload")
    Observable<FileUploadResponse> uploadFile(@Path("team_id") String team_id,
                                              @Part MultipartBody.Part file,
                                              @Part("channel_id") RequestBody channel_id,
                                              @Part("client_ids") RequestBody client_ids);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/preferences/save")
    Observable<Boolean> save(@Body List<SaveData> saveData);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{team_id}/posts/search")
    Observable<Posts> searchForPosts(@Path("team_id") String team_id,
                                     @Body SearchParams searchParams);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/{firstMessageId}/after/0/{limit}")
    Observable<Posts> getPostsAfter(@Path("teamId") String teamId,
                                    @Path("channelId") String channelId,
                                    @Path("firstMessageId") String firstMessageId,
                                    @Path("limit") String limit);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/invite_members")
    Observable<ListInviteObj> invite(
            @Path("teamId") String teamId,
            @Body ListInviteObj listInviteObj);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{team_id}/channels/create_direct")
    Observable<Channel> createDirect(@Path("team_id") String teamId,
                                     @Body LogoutData user);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @Streaming
    @GET("api/v3/teams/{team_id}/files/get{file_id}")
    Observable<ResponseBody> downloadFile(@Path("team_id") String team_id,
                                          @Path("file_id") String file_id);


    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{team_id}/channels/more")
    Observable<ChannelsWithMembers> channelsMore(@Path("team_id") String teamId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{team_id}/channels/{channel_id}/join")
    Observable<Channel> joinChannel(@Path("team_id") String teamId,
                                    @Path("channel_id") String channelId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{team_id}/channels/{channel_id}/leave")
    Observable<Channel> leaveChannel(@Path("team_id") String teamId,
                                     @Path("channel_id") String channelId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/users/update_notify")
    Observable<User> updateNotify(@Body NotifyUpdate notifyUpdate);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/users/updateMembers")
    Observable<User> updateUser(@Body User user);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest"})
    @Multipart
    @POST("api/v3/users/newimage")
    Observable<Boolean> newimage(@Part MultipartBody.Part image);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/all")
    Observable<List<Team>> getAllTeams();

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{team_id}/channels/create")
    Observable<Channel> createChannel(@Path("team_id") String teamId,
                                      @Body Channel creatingChannel);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/{channelId}/add")
    Observable<AddMembersPresenter.Members> addMember(@Path("teamId") String teamId,
                                                      @Path("channelId") String channelId,
                                                      @Body AddMembersPresenter.Members members);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/update_header")
    Observable<Channel> updateHeader(@Path("teamId") String teamId,
                                     @Body HeaderPresenter.ChannelHeader channelHeader);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/update_purpose")
    Observable<Channel> updatePurpose(@Path("teamId") String teamId,
                                      @Body PurposePresenter.ChannelPurpose channelPurpose);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/update")
    Observable<Channel> updateChannel(@Path("teamId") String teamId,
                                      @Body Channel channel);

}
