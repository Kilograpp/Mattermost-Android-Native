package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.LicenseCfg;
import com.kilogramm.mattermost.model.entity.ListSaveData;
import com.kilogramm.mattermost.model.entity.NotifyProps;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.SaveData;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.ThemeProps;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelByIdSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelByTypeSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 05.10.2016.
 */
public class GeneralRxPresenter extends BaseRxPresenter<GeneralRxActivity> {
    public static final String TAG = "GeneralRxPresenter";

    private static final int REQUEST_DIRECT_PROFILE = 1;
    private static final int REQUEST_LOAD_CHANNELS = 2;
    private static final int REQUEST_USER_TEAM = 3;
    private static final int REQUEST_LOGOUT = 4;
    private static final int REQUEST_SAVE = 5;
    private static final int REQUEST_ADD_CHAT = 6;

    Realm realm;
    private UserRepository userRepository;
    private ChannelRepository channelRepository;

    ApiMethod service;

    @State
    String teamId;
    @State
    ListSaveData mSaveData = new ListSaveData();

    LogoutData user;
    String channelId;

    String postId;
    String offset;
    String limit;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        realm = Realm.getDefaultInstance();
        teamId = realm.where(Team.class).findFirst().getId();
        userRepository = new UserRepository();
        channelRepository = new ChannelRepository();
        user = new LogoutData();
        MattermostApp application = MattermostApp.getSingleton();
        service = application.getMattermostRetrofitService();
        initRequest();
    }

    @Override
    public void takeView(GeneralRxActivity generalActivity) {
        super.takeView(generalActivity);
        setSelectedLast(MattermostPreference.getInstance().getLastChannelId());
    }

    public void setSelectedLast(String id) {
        Channel channel;
        if (id != null) {
            try {
                channel = channelRepository.query(new ChannelByIdSpecification(id)).first();
                if (channel != null)
                    switch (channel.getType()) {
                        case "O":
                            setSelectedMenu(channel.getId(), channel.getName(), channel.getType());
                            break;
                        case "D":
                            setSelectedMenu(channel.getId(), channel.getUsername(), channel.getType());
                            break;
                        case "P":
                            setSelectedMenu(channel.getId(), channel.getName(), channel.getType());
                            break;
                    }
                sendSetSelectChannel(id, channel.getType());
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        } else {
            RealmResults<Channel> channels = channelRepository.query(new ChannelByTypeSpecification("O"));
            if (channels.size() != 0) {
                setSelectedMenu(channels.first().getId(), channels.first().getName(), channels.first().getType());
            } else {
                channels.addChangeListener(element -> {
                    if (element.size() != 0)
                        setSelectedMenu(element.first().getId(), element.first().getName(), channels.first().getType());
                });
            }
        }
    }

    private void initRequest() {
        restartableFirst(REQUEST_DIRECT_PROFILE, () -> {
            return service.getDirectProfile()
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());
        }, (generalRxActivity, stringUserMap) -> {
            RealmList<User> users = new RealmList<>();
            users.addAll(stringUserMap.values());
            users.add(new User("materMostAll", "all", "Notifies everyone in the channel, use in Town Square to notify the whole team"));
            users.add(new User("materMostChannel", "channel", "Notifies everyone in the channel"));
            userRepository.add(users);
            requestLoadChannels();
        }, (generalRxActivity1, throwable) -> {
            sendShowError(throwable.toString());
        });

        restartableFirst(REQUEST_LOAD_CHANNELS, () -> {
            return service.getChannelsTeam(teamId)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());
        }, (generalRxActivity, channelsWithMembers) -> {
            channelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
                    MattermostPreference.getInstance().getMyUserId(), userRepository);
            requestUserTeam();
        }, (generalRxActivity1, throwable) -> {
            sendShowError(throwable.toString());
        });

        restartableFirst(REQUEST_USER_TEAM, () -> {
            return service.getTeamUsers(teamId)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());
        }, (generalRxActivity, stringUserMap) -> {
            userRepository.add(stringUserMap.values());
            if (MattermostPreference.getInstance().getLastChannelId() == null) {
                Channel channel = channelRepository.query(new ChannelByTypeSpecification("O")).first();
                sendSetSelectChannel(channel.getId(), channel.getType());
                if (channel != null) {
                    setSelectedMenu(channel.getId(), channel.getName(), channel.getType());
                }
            }
        }, (generalRxActivity1, throwable) -> {
            throwable.printStackTrace();
        });

        restartableFirst(REQUEST_LOGOUT, () -> {
            return service.logout(new Object())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());
        }, (generalRxActivity, logoutData) -> {
            Log.d(TAG, "Complete logout");
            clearDataBaseAfterLogout();
            clearPreference();
            sendShowMainRxActivity();
        }, (generalRxActivity1, throwable) -> {
            throwable.printStackTrace();
            Log.d(TAG, "Error logout");
        });

        initSaveRequest();

        restartableFirst(REQUEST_ADD_CHAT, () -> {
            return service.joinChannel(teamId, channelId)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());

        }, (generalRxActivity, channel) -> {
            channelRepository.add(channel);
            sendSetFragmentChat(channel.getId(), channel.getName(), channel.getType());

        }, (generalRxActivity, throwable) -> {
            sendShowError(throwable.toString());
            Log.d(TAG, throwable.getMessage());
        });
    }

    private void initSaveRequest() {
        restartableFirst(REQUEST_SAVE, () -> {
            return Observable.defer(() -> Observable.zip(
                    service.createDirect(teamId, user)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread()),

                    service.save(mSaveData.getmSaveData())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread()),

                    (channel, aBoolean) -> {
                        if (aBoolean == Boolean.FALSE) {
                            Log.d(TAG, "aBoolean == null");
                            return null;
                        }

                        Realm realm = Realm.getDefaultInstance();
                        User directUser = realm.where(User.class).equalTo("id", user.getUserId()).findFirst();
                        channel.setUser(directUser);
                        channel.setUsername(directUser.getUsername());

                        channelRepository.add(channel);

                        return channel;
                    }));

        }, (generalRxActivity, channel) -> {
            mSaveData.getmSaveData().clear();
            channelRepository.add(channel);
            sendSetFragmentChat(channel.getId(), channel.getUsername(), channel.getType());

        }, (generalRxActivity, throwable) -> throwable.printStackTrace());
    }

    public void requestAddChat(String joinChannelId) {
        channelId = joinChannelId;
        start(REQUEST_ADD_CHAT);
    }

    public void requestSaveData(SaveData data, String userId) {
        mSaveData.getmSaveData().clear();
        mSaveData.getmSaveData().add(data);
        user.setUserId(userId);
        start(REQUEST_SAVE);
    }

    public void requestLoadChannels() {
        start(REQUEST_LOAD_CHANNELS);
    }

    public void requestDirectProfile() {
        start(REQUEST_DIRECT_PROFILE);
    }

    public void requestUserTeam() {
        start(REQUEST_USER_TEAM);
    }

    public void requestLogout() {
        start(REQUEST_LOGOUT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onTakeView(GeneralRxActivity generalRxActivity) {
        super.onTakeView(generalRxActivity);
        requestDirectProfile();
        //loadChannels(realm.where(Team.class).findFirst().getId());
    }

    public void setSelectedMenu(String channelId, String name, String type) {
        sendSetFragmentChat(channelId, name, type);
        MattermostPreference.getInstance().setLastChannelId(channelId);
    }

    private void clearPreference() {
        MattermostPreference.getInstance().setAuthToken(null);
        MattermostPreference.getInstance().setLastChannelId(null);
    }

    private void clearDataBaseAfterLogout() {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm.delete(Post.class);
            realm.delete(Channel.class);
            realm.delete(InitObject.class);
            realm.delete(LicenseCfg.class);
            realm.delete(NotifyProps.class);
            realm.delete(RealmString.class);
            realm.delete(Team.class);
            realm.delete(InitObject.class);
            realm.delete(ThemeProps.class);
            realm.delete(User.class);
        });
        realm.close();
    }

    //to view methods
    private void sendShowError(String error) {
        Observable.just(error)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split(GeneralRxActivity::showErrorText));
    }

    private void sendShowMainRxActivity() {
        Observable.just(new Object())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((mainRxAcivity, o) -> mainRxAcivity.showMainRxActivity()));

    }

    private void sendSetFragmentChat(String channelId, String name, String type) {
        Observable.just(new OpenChatObject(channelId, name, type))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((generalRxActivity1, openChatObject)
                        -> generalRxActivity1.setFragmentChat(openChatObject.getChannelId(), name, type)));
    }

    private void sendSetSelectChannel(String channelId, String type) {
        Observable.just(new OpenChatObject(channelId, type))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((generalRxActivity1, openChatObject)
                        -> generalRxActivity1.setSelectItemMenu(openChatObject.getChannelId(), type)));
    }

    public static class OpenChatObject {
        private String channelId;
        private String name;
        private String type;

        public OpenChatObject(String channelId, String name, String type) {
            this.channelId = channelId;
            this.name = name;
            this.type = type;
        }

        public OpenChatObject(String channelId, String type) {
            this.channelId = channelId;
            this.type = type;
        }

        public String getChannelId() {
            return channelId;
        }

        public String getName() {
            return name;
        }

        public String getChannel() {
            return type;
        }
    }
}
