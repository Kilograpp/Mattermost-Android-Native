package com.kilogramm.mattermost.network;

import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.SearchParams;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.notifyProps.NotifyUpdate;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostEdit;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.fromnet.AutocompleteUsers;
import com.kilogramm.mattermost.model.fromnet.ChannelWithMember;
import com.kilogramm.mattermost.model.fromnet.ChannelsWithMembers;
import com.kilogramm.mattermost.model.fromnet.CommandFromNet;
import com.kilogramm.mattermost.model.fromnet.CommandToNet;
import com.kilogramm.mattermost.model.fromnet.ExtraInfoWithOutMember;
import com.kilogramm.mattermost.model.fromnet.ForgotData;
import com.kilogramm.mattermost.model.fromnet.ListInviteObj;
import com.kilogramm.mattermost.model.fromnet.LoginData;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.presenter.channel.AddMembersPresenter;
import com.kilogramm.mattermost.presenter.channel.HeaderPresenter;
import com.kilogramm.mattermost.presenter.channel.PurposePresenter;
import com.kilogramm.mattermost.presenter.settings.PasswordChangePresenter;
import com.kilogramm.mattermost.rxtest.left_menu.model.ResponseLeftMenuData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Created by Evgeny on 26.12.2016.
 */

public class ServerMethod {

    private static ServerMethod instance;

    private static ApiMethod mApi;

    private ServerMethod(ApiMethod api) {
        mApi = api;
    }

    public static void buildServerMethod(ApiMethod api) {
        instance = new ServerMethod(api);
    }

    public static ServerMethod getInstance() {
        return instance;
    }

    public Observable<List<Channel>> getChannelsTeam(String teamId) {
        return mApi.getChannelsTeamNew(teamId);
    }

    public Observable<Channel> saveOrCreateDirectChannel(List<Preferences> preferences, String teamId, String userId) {
        User user = new User();
        user.setId(userId);
        return Observable.defer(() -> Observable.zip(
                mApi.save(preferences),
                mApi.createDirect(teamId, user),
                (aBoolean, channel) -> {
                    if (!aBoolean)
                        return null;
                    return channel;
                }
        ));
    }

    public Observable<InitObject> initLoad() {
        return mApi.initLoad();
    }

    public Observable<User> login(LoginData loginData) {
        return mApi.login(loginData);
    }

    public Observable<Map<String, User>> getSiteAllUsers(int offset, int limit) {
        return mApi.getSiteAllUsers(offset, limit);
    }

    public Observable<LogoutData> logout() {
        return mApi.logout(new Object());
    }

    public Observable<Boolean> save(List<Preferences> preferencesList) {
        return mApi.save(preferencesList);
    }

    public Observable<ChannelsWithMembers> extraInfo(String teamId) {
        return Observable.defer(() -> Observable.zip(
                mApi.getChannelsTeamNew(teamId),
                mApi.getMembersTeamNew(teamId),
                ChannelsWithMembers::new));
    }

    public Observable<ExtraInfoWithOutMember> extraInfoChannel(String teamId, String channelId) {
        return Observable.defer(() -> Observable.zip(
                mApi.getExtraInfoChannel(teamId, channelId),
                mApi.getUsersInChannel(teamId, channelId, 0, 100),
                ExtraInfoWithOutMember::new));
    }

    public Observable<ChannelWithMember> getChannel(String teamId, String channelId) {
        return mApi.getChannel(teamId, channelId);
    }

    public Observable<Posts> getPosts(String teamId, String channelId) {
        return mApi.getPosts(teamId, channelId);
    }

    public Observable<CommandFromNet> executeCommand(String teamId, CommandToNet command) {
        return mApi.executeCommand(teamId, command);
    }

    public Observable<Post> sendPost(String teamId, String channelId, Post post) {
        return mApi.sendPost(teamId, channelId, post);
    }

    public Observable<Post> updateLastViewedAt(String teamId, String channelId) {
        return mApi.updatelastViewedAt(teamId, channelId);
    }

    public Observable<Post> deletePost(String teamId, String channelId, String postId) {
        return mApi.deletePost(teamId, channelId, postId, new Object());
    }

    public Observable<Post> editPost(String teamId, String channelId, PostEdit postEdit) {
        return mApi.editPost(teamId, channelId, postEdit);
    }

    public Observable<Posts> getPostsBefore(String teamId, String channelId, String lastSinceId, String limit) {
        return mApi.getPostsBefore(teamId, channelId, lastSinceId, limit);
    }

    public Observable<Posts> getPostsAfter(String teamId, String channelId, String firstSinceId, String limit) {
        return mApi.getPostsAfter(teamId, channelId, firstSinceId, limit);
    }

    public Observable<List<Posts>> loadBeforeAndAfter(String teamId, String channelId, String searchMessageId, String limit) {
        return Observable.defer(() -> Observable.zip(
                mApi.getExtraInfoChannel(teamId, channelId),
                mApi.getPostsBefore(teamId, channelId, searchMessageId, limit),
                mApi.getPost(teamId, channelId, searchMessageId),
                mApi.getPostsAfter(teamId, channelId, searchMessageId, limit),
                (extraInfo, postsBef, foundPosts, postsAft) -> {
                    ArrayList<Posts> allPosts = new ArrayList<>();
                    if (postsAft.getPosts() != null) {
                        allPosts.add(postsAft);
                    }
                    allPosts.add(foundPosts);
                    if (postsBef.getPosts() != null) {
                        allPosts.add(postsBef);
                    }
                    return allPosts;
                }));
    }

    public Observable<AddMembersPresenter.Members> addMember(String teamId, String channelId, AddMembersPresenter.Members members) {
        return mApi.addMember(teamId, channelId, members);
    }

    public Observable<Channel> updateHeader(String teamId, HeaderPresenter.ChannelHeader channelHeader) {
        return mApi.updateHeader(teamId, channelHeader);
    }

    public Observable<Channel> updateChannel(String teamId, Channel channel) {
        return mApi.updateChannel(teamId, channel);
    }

    public Observable<Channel> updatePurpose(String teamId, PurposePresenter.ChannelPurpose channelPurpose) {
        return mApi.updatePurpose(teamId, channelPurpose);
    }

    public Observable<User> updateUser(User user) {
        return mApi.updateUser(user);
    }

    public Observable<User> updateNotify(NotifyUpdate notifyUpdate) {
        return mApi.updateNotify(notifyUpdate);
    }

    public Observable<ResponseBody> changePassword(PasswordChangePresenter.NewPassword password) {
        return mApi.changePassword(password);
    }

    public Observable<Channel> createChannel(String teamId, Channel channel) {
        return mApi.createChannel(teamId, channel);
    }

    public Observable<ForgotData> forgotPassword(ForgotData forgotData) {
        return mApi.forgotPassword(forgotData);
    }

    public Observable<Posts> searchForPosts(String teamId, SearchParams params) {
        return mApi.searchForPosts(teamId, params);
    }

    public Observable<ListInviteObj> invite(String teamId, ListInviteObj listInviteObj) {
        return mApi.invite(teamId, listInviteObj);
    }

    public Observable<List<Channel>> channelsMore(String teamId) {
        return mApi.channelsMore(teamId);
    }

    public Observable<Channel> joinChannel(String teamId, String channelId) {
        return mApi.joinChannel(teamId, channelId);
    }

    public Observable<Channel> createDirect(String teamId, User user) {
        return mApi.createDirect(teamId, user);
    }

    public Observable<Channel> leaveChannel(String teamId, String channelId) {
        return mApi.leaveChannel(teamId, channelId);
    }

    public Observable<Channel> deleteChannel(String teamId, String channelId) {
        return mApi.deleteChannel(teamId, channelId);
    }

    public Observable<Map<String, User>> getUsersInChannel(String team_id, String channel_id, int offset, int limit) {
        return mApi.getUsersInChannel(team_id, channel_id, offset, limit);
    }

    public Observable<Map<String, User>> getAllUsers(String team_id, int offset, int limit) {
        return mApi.getAllUsers(team_id, offset, limit);
    }
    public Observable<Map<String, User>> getUsersById(List<String> ids){
        return mApi.getUsersById(ids);
    }

    public Observable<Channel> joinChannelName(String team_id, String channel_name) {
        return mApi.joinChannelName(team_id, channel_name);
    }

    public Observable<AutocompleteUsers> getAutocompleteUsers(String teamId, String channelId, String term) {
        return mApi.getAutocompleteUsers(teamId, channelId, term);
    }

    public Observable<Map<String, User>> getUsersNotInChannel(String team_id, String channel_id, int offset, int limit) {
        return mApi.getUsersNotInChannel(team_id, channel_id, offset, limit);
    }

    public Observable<ResponseLeftMenuData> loadLeftMenu(List<String> ids, String teamId) {
        return Observable.defer(() -> Observable.zip(
                mApi.getUserByIds(ids),
                mApi.getUserMembersByIds(teamId, ids),
                mApi.getChannelsTeamNew(teamId),
                mApi.getMembersTeamNew(teamId),
                (stringUserMap, userMembers, channels, members) -> {
                    ResponseLeftMenuData responseLeftMenuData = new ResponseLeftMenuData();
                    responseLeftMenuData.setData(stringUserMap, userMembers, channels, members);
                    return responseLeftMenuData;
                }));
    }
}