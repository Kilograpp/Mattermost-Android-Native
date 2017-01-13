package com.kilogramm.mattermost.network;

import com.kilogramm.mattermost.model.entity.FileUploadResponse;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.SearchParams;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.notifyProps.NotifyUpdate;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostEdit;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.Users;
import com.kilogramm.mattermost.model.fromnet.AutocompleteUsers;
import com.kilogramm.mattermost.model.fromnet.ChannelWithMember;
import com.kilogramm.mattermost.model.fromnet.ChannelsWithMembers;
import com.kilogramm.mattermost.model.fromnet.CommandFromNet;
import com.kilogramm.mattermost.model.fromnet.CommandToNet;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.model.fromnet.ForgotData;
import com.kilogramm.mattermost.model.fromnet.ListInviteObj;
import com.kilogramm.mattermost.model.fromnet.LoginData;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.presenter.channel.AddMembersPresenter;
import com.kilogramm.mattermost.presenter.channel.HeaderPresenter;
import com.kilogramm.mattermost.presenter.channel.PurposePresenter;
import com.kilogramm.mattermost.presenter.settings.PasswordChangePresenter;

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

    @POST("api/v3/users/send_password_reset")
    Observable<ForgotData> forgotPassword(@Body ForgotData forgotData);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/")
    Observable<ChannelsWithMembers> getChannelsTeam(@Path("teamId") String teamId);

    //TODO теперь приходит не ChannelsWithMembers, а List<Channel>. потихоньку заменяю везде
    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/")
    Observable<List<Channel>> getChannelsTeamNew(@Path("teamId") String teamId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/members")
    Observable<List<Member>> getMembersTeamNew(@Path("teamId") String teamId);



    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{team_id}/channels/{channel_id}/users/not_in_channel/{offset}/{limit}")
    Observable<Map<String, User>> getUsersNotInChannel(@Path("team_id") String team_id,
                                                       @Path("channel_id") String channel_id,
                                                       @Path("offset") int offset,
                                                       @Path("limit") int limit);


    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channel_id}/")
    Observable<ChannelWithMember> getChannel(@Path("teamId") String teamId,
                                             @Path("channel_id") String channelId);

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
    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/page/0/60")
    Observable<Posts> getPosts(@Path("teamId") String teamId,
                               @Path("channelId") String channelId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/{id}/get")
    Observable<Posts> getPost(@Path("teamId") String teamId,
                              @Path("channelId") String channelId,
                              @Path("id") String id);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channelId}/posts/{lastMessageId}/before/0/{limit}")
    Observable<Posts> getPostsBefore(@Path("teamId") String teamId,
                                     @Path("channelId") String channelId,
                                     @Path("lastMessageId") String lastMessageId,
                                     @Path("limit") String limit);

   //TODO was updated by Mattermost to stats
//    @Headers({
//            "Accept: application/json",
//            "X-Request-With: XMLHttpRequest",
//            "Content-Type: application/json"})
//    @GET("api/v3/teams/{teamId}/channels/{channelId}/extra_info")
//    Observable<ExtraInfo> getExtraInfoChannel(@Path("teamId") String teamId,
//                                              @Path("channelId") String channelId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channelId}/stats")
    Observable<ExtraInfo> getExtraInfoChannel(@Path("teamId") String teamId,
                                              @Path("channelId") String channelId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{team_id}/channels/{channel_id}/users/{offset}/{limit}")
    Observable<Map<String, User>> getUsersInChannel(@Path("team_id") String team_id,
                                                    @Path("channel_id") String channel_id,
                                                    @Path("offset") int offset,
                                                    @Path("limit") int limit);


    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channel_id}/posts/{postId}/get_file_infos")
    Observable<List<FileInfo>> getFileInfo(@Path("teamId") String teamId,
                                     @Path("channel_id") String channel_id,
                                     @Path("postId") String postId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{team_id}/channels/{channel_id}")
    Observable<Channel> getChannelById(@Path("team_id") String team_id,
                                       @Path("channel_id") String channel_id);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{team_id}/channels/{channel_id}/members/{user_id}")
    Observable<Member> getChannelMember(@Path("team_id") String team_id,
                                        @Path("channel_id") String channel_id,
                                        @Path("user_id") String user_id);


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
    @POST("api/v3/users/logout")
    Observable<LogoutData> logout(@Body Object object);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/{channelId}/posts/{postId}/delete")
    Observable<Post> deletePost(@Path("teamId") String teamId,
                                @Path("channelId") String channelId,
                                @Path("postId") String postId,
                                @Body Object object);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/{channelId}/posts/update")
    Observable<Post> editPost(@Path("teamId") String teamId,
                              @Path("channelId") String channelId,
                              @Body PostEdit post);

//    @Headers({
//            "Accept: application/json",
//            "X-Request-With: XMLHttpRequest",
//            "Content-Type: application/json"})
//    @GET("api/v3/users/direct_profiles")
//    Observable<Map<String, User>> getDirectProfile();

//    @Headers({
//            "Accept: application/json",
//            "X-Request-With: XMLHttpRequest",
//            "Content-Type: application/json"})
//    @GET("api/v3/teams/{team_id}/channels/{channel_id}/stats")
//    Observable<Stats> getUsersInTeamQuantity(@Path("team_id") String team_id,
//                                             @Path("channel_id") String channel_id);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{team_id}/users/{offset}/{limit}")
    Observable<Map<String, User>> getAllUsers(@Path("team_id") String team_id,
                                              @Path("offset") int offset,
                                              @Path("limit") int limit);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/users/{offset}/{limit}")
    Observable<Map<String, User>> getSiteAllUsers(@Path("offset") int offset,
                                                  @Path("limit") int limit);

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
    Observable<Boolean> save(@Body List<Preferences> saveData);


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
    Observable<ListInviteObj> invite(@Path("teamId") String teamId,
                                     @Body ListInviteObj listInviteObj);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{team_id}/channels/create_direct")
    Observable<Channel> createDirect(@Path("team_id") String teamId,
                                     @Body User user);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{team_id}/channels/more")
//    Observable<ChannelsWithMembers> channelsMore(@Path("team_id") String teamId);
    Observable<List<Channel>> channelsMore(@Path("team_id") String teamId);

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
    @POST("api/v3/teams/{team_id}/channels/name/{channel_name}/join")
    Observable<Channel> joinChannelName(@Path("team_id") String team_id,
                                        @Path("channel_name") String channel_name);


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
    @POST("api/v3/users/update")
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

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/channels/{channelId}/delete")
    Observable<Channel> deleteChannel(@Path("teamId") String teamId,
                                      @Path("channelId") String channelId);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/users/newpassword")
    Observable<ResponseBody> changePassword(@Body PasswordChangePresenter.NewPassword newPassword);


    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @POST("api/v3/teams/{teamId}/commands/execute")
    Observable<CommandFromNet> executeCommand(@Path("teamId") String teamId,
                                              @Body CommandToNet command);

    @Headers({
            "Accept: application/json",
            "X-Request-With: XMLHttpRequest",
            "Content-Type: application/json"})
    @GET("api/v3/teams/{teamId}/channels/{channelId}/users/autocomplete")
    Observable<AutocompleteUsers> getAutocompleteUsers(@Path("teamId") String teamId,
                                                       @Path("channelId") String channelId,
                                                       @Query("term") String term);
}
