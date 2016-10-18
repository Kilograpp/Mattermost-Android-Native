package com.kilogramm.mattermost.rxtest;

import android.content.res.ObbInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.LicenseCfg;
import com.kilogramm.mattermost.model.entity.ListSaveData;
import com.kilogramm.mattermost.model.entity.NotifyProps;
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
//    private static final int REQUEST_CREATE_DIRECT = 6;

    Realm realm;
    private UserRepository userRepository;
    private ChannelRepository channelRepository;

    ApiMethod service;

    @State
    String teamId;
    @State
    ListSaveData mSaveData = new ListSaveData();

    LogoutData user;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        realm = Realm.getDefaultInstance();
        teamId = realm.where(Team.class).findFirst().getId();
        userRepository = new UserRepository();
        channelRepository = new ChannelRepository();
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
                            setSelectedChannel(channel.getId(), channel.getName());
                            break;
                        case "D":
                            setSelectedDirect(channel.getId(), channel.getUsername());
                            break;
                        case "P":
                            break;
                    }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        } else {
            RealmResults<Channel> channels = channelRepository.query(new ChannelByTypeSpecification("O"));
            if (channels.size() != 0) {
                setSelectedChannel(channels.first().getId(), channels.first().getName());
            } else {
                channels.addChangeListener(element -> {
                    if (element.size() != 0)
                        setSelectedChannel(element.first().getId(), element.first().getName());
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
                if (channel != null) {
                    setSelectedChannel(channel.getId(), channel.getName());
                }
            }
        }, (generalRxActivity1, throwable) -> {
            throwable.printStackTrace();
        });

        restartableFirst(REQUEST_LOGOUT, () -> {
                    return service.logout(new Object())
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread());
                },
                (generalRxActivity, logoutData) -> {
                    Log.d(TAG, "Complete logout");
                    clearDataBaseAfterLogout();
                    clearPreference();
                    sendShowMainRxActivity();
                },
                (generalRxActivity1, throwable) -> {
                    throwable.printStackTrace();
                    Log.d(TAG, "Error logout");
                });

//        restartableFirst(REQUEST_CREATE_DIRECT, () -> {
//            return service.createDirect(teamId, user)
//                    .subscribeOn(Schedulers.computation())
//                    .observeOn(AndroidSchedulers.mainThread());
//        }, (generalRxActivity, channel) ->  {
//            Log.d(TAG, "Complete creating channel");
//            channelRepository.add(channel);
//            setSelectedDirect( channel.getId(), channel.getUsername());
//        }, (generalRxActivity, throwable) -> {
//            Log.d(TAG, "Error creating channel");
//            throwable.printStackTrace();
//        });

        initSaveRequest();
    }

    private void initSaveRequest() {
        restartableFirst(REQUEST_SAVE, () -> {
            return Observable.zip(

                    service.createDirect(teamId, user)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread()),

                    service.save(mSaveData.getmSaveData())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread()),

//                    service.getChannelsTeam(teamId)
//                            .subscribeOn(Schedulers.computation())
//                            .observeOn(AndroidSchedulers.mainThread()),

                    (channel, aBoolean/*, channelsWithMembers*/) -> {
                        if (aBoolean == Boolean.FALSE) {
                            Log.d(TAG, "aBoolean == null");
                            return null;
                        }

                        String myId = MattermostPreference.getInstance().getMyUserId();
//                        channelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
//                                            myId, userRepository);

                        // возможно придется вернуть
//                        channelRepository.add(channelsWithMembers.getChannels());

                        channelRepository.add(channel);

//                        RealmResults<Channel> channels = realm.where(Channel.class)
//                                .equalTo("name", myId + "__" + mSaveData.getmSaveData().get(0).getName())
//                                .or()
//                                .equalTo("name", mSaveData.getmSaveData().get(0).getName() + "__" + myId)
//                                .findAll();
//                        RealmResults<Channel> channels = realm.where(Channel.class)
//                                .equalTo("name", channel.getName())
//                                .findAll();

                        // возможно придется вернуть
//                        RealmList<Channel> channels = new RealmList<>();
//                        RealmResults<Channel> allChannels = realm.where(Channel.class).findAll();
//                        for (Channel iter : allChannels) {
//                            if (iter.getId() == channel.getId()) {
//                                channels.add(iter);
//                            }
//                        }

//                        Log.d(TAG, "before channels if()");
//
//                        if (channels.size() != 0) {
//                            Log.d(TAG, String.valueOf(channels.size()));
//                            return channels.get(0);
//                        } else {
//                            Log.d(TAG, "channels.size() == null");
//                            return null;
//                        }
                        return channel;
                    });

        }, (generalRxActivity, channel) -> {
            Log.d(TAG, "OnNext initSaveRequest");
            mSaveData.getmSaveData().clear();
            if (channel == null) {
                Log.d(TAG, "channel = null");
            }
            channelRepository.add(channel);
            setSelectedDirect(channel.getId(), channel.getUsername());
        },(generalRxActivity, throwable) -> {
            throwable.printStackTrace();
        });
    }


    public void requestSaveData(SaveData data,String userId) {
        mSaveData.getmSaveData().clear();
        mSaveData.getmSaveData().add(data);
        user = new LogoutData();
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

//    public void requestCreateDirect(String userId) {
//        user = new LogoutData();
//        user.setUserId(userId);
//        start(REQUEST_CREATE_DIRECT);
//    }

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

    public void setSelectedDirect(String itemId, String name) {
//        String myId = realm.where(User.class).findFirst().getId();
//
//        String channelId = realm.where(Channel.class)
//                .equalTo("name", myId + "__" + itemId)
//                .or()
//                .equalTo("name", itemId + "__" + myId)
//                .findFirst()
//                .getId();
        sendSetFragmentChat(itemId, name, false);
        MattermostPreference.getInstance().setLastChannelId(itemId);
    }

    public void setSelectedChannel(String channelId, String name) {
        sendSetFragmentChat(channelId, name, true);
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

    private void sendSetFragmentChat(String channelId, String name, Boolean isChannel) {
        Observable.just(new OpenChatObject(channelId, name, isChannel))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((generalRxActivity1, openChatObject)
                        -> generalRxActivity1.setFragmentChat(openChatObject.getChannelId(), name, isChannel)));

    }


    public static class OpenChatObject {
        private String channelId;
        private String name;
        private Boolean isChannel;

        public OpenChatObject(String channelId, String name, Boolean isChannel) {
            this.channelId = channelId;
            this.name = name;
            this.isChannel = isChannel;
        }

        public String getChannelId() {
            return channelId;
        }

        public String getName() {
            return name;
        }

        public Boolean getChannel() {
            return isChannel;
        }
    }
}
