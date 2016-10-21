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
import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.SaveData;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.ThemeProps;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;

import java.util.ArrayList;
import java.util.List;

import icepick.State;
import io.realm.Realm;
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

    private Realm realm;

    private ApiMethod service;

    /*@State
    String teamId;*/
    @State
    ListSaveData mSaveData = new ListSaveData();
    private LogoutData user;
    private String channelId;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        realm = Realm.getDefaultInstance();

        user = new LogoutData();
        MattermostApp application = MattermostApp.getSingleton();
        service = application.getMattermostRetrofitService();
        initRequest();
        requestDirectProfile();
    }

    @Override
    public void takeView(GeneralRxActivity generalActivity) {
        super.takeView(generalActivity);
        setSelectedLast(MattermostPreference.getInstance().getLastChannelId());
    }

    //TODO review evgenysuetin
    public void setSelectedLast(String id) {
        Log.d(TAG, "setSelectedLast");
        Channel channel;
        if (id != null) {
            try {
                channel = new ChannelRepository.ChannelByIdSpecification(id).toRealmResults(realm).first();
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
            RealmResults<Channel> channels = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("O"));
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
        restartableFirst(REQUEST_DIRECT_PROFILE,
                () -> service.getDirectProfile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (generalRxActivity, stringUserMap) -> {
                    List<User> users = new ArrayList<User>();
                    users.addAll(stringUserMap.values());
                    users.add(new User("materMostAll", "all", "Notifies everyone in the channel, use in Town Square to notify the whole team"));
                    users.add(new User("materMostChannel", "channel", "Notifies everyone in the channel"));
                    UserRepository.add(users);
                    requestLoadChannels();
                }, (generalRxActivity1, throwable) -> {
                    sendShowError(throwable.toString());
                });

        restartableFirst(REQUEST_LOAD_CHANNELS, () -> {
            return service.getChannelsTeam(MattermostPreference.getInstance().getTeamId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io());
        }, (generalRxActivity, channelsWithMembers) -> {
            ChannelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
                    MattermostPreference.getInstance().getMyUserId());
            requestUserTeam();
        }, (generalRxActivity1, throwable) -> sendShowError(throwable.toString()));

        restartableFirst(REQUEST_USER_TEAM, () -> {
            return service.getTeamUsers(MattermostPreference.getInstance().getTeamId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io());
        }, (generalRxActivity, stringUserMap) -> {
            UserRepository.add(stringUserMap.values());
            if (MattermostPreference.getInstance().getLastChannelId() == null) {
                Channel channel = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("O")).first();
                sendSetSelectChannel(channel.getId(), channel.getType());
                if (channel != null) {
                    setSelectedMenu(channel.getId(), channel.getName(), channel.getType());
                }
            }
        }, (generalRxActivity1, throwable) -> throwable.printStackTrace());

        restartableFirst(REQUEST_LOGOUT, () -> {
            return service.logout(new Object())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io());
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
            return service.joinChannel(MattermostPreference.getInstance().getTeamId(), channelId)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());

        }, (generalRxActivity, channel) -> {
            List<Channel> channelList = new ArrayList<>();
            channelList.add(channel);
            ChannelRepository.prepareChannelAndAdd(channelList,MattermostPreference.getInstance().getMyUserId());
            sendSetFragmentChat(channel.getId(), channel.getName(), channel.getType());
        }, (generalRxActivity, throwable) -> {
            throwable.printStackTrace();
            Log.d(TAG, throwable.getMessage());
        });
    }

    private void initSaveRequest() {
        restartableFirst(REQUEST_SAVE, () -> Observable.defer(
                () -> Observable.zip(
                        service.createDirect(MattermostPreference.getInstance().getTeamId(), user)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),

                        service.save(mSaveData.getmSaveData())
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),

                        (channel, aBoolean) -> {
                            if (aBoolean == Boolean.FALSE) {
                                return null;
                            }
                            ChannelRepository.prepareDirectChannelAndAdd(channel, user.getUserId());
                            return channel;
                        })), (generalRxActivity, channel) -> {
            mSaveData.getmSaveData().clear();
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
    protected void onTakeView(GeneralRxActivity generalRxActivity) {
        super.onTakeView(generalRxActivity);
        //loadChannels(realm.where(Team.class).findFirst().getId());
    }

    public void setSelectedMenu(String channelId, String name, String type) {
        if(MattermostPreference.getInstance().getLastChannelId() != null &&
                !MattermostPreference.getInstance().getLastChannelId().equals(channelId)) {
            // For clearing attached files on channel change
            FileToAttachRepository.getInstance().deleteUploadedFiles();
        }
        sendSetFragmentChat(channelId, name, type);
        MattermostPreference.getInstance().setLastChannelId(channelId);
    }

    private void clearPreference() {
        MattermostPreference.getInstance().setAuthToken(null);
        MattermostPreference.getInstance().setLastChannelId(null);
    }

    private void clearDataBaseAfterLogout() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm1.delete(Post.class);
            realm1.delete(Channel.class);
            realm1.delete(InitObject.class);
            realm1.delete(LicenseCfg.class);
            realm1.delete(NotifyProps.class);
            realm1.delete(RealmString.class);
            realm1.delete(Team.class);
            realm1.delete(InitObject.class);
            realm1.delete(ThemeProps.class);
            realm1.delete(User.class);
        });
    }

    //to view methods
    private void sendShowError(String error) {
        createTemplateObservable(error)
                .subscribe(split(GeneralRxActivity::showErrorText));
    }

    private void sendShowMainRxActivity() {
        createTemplateObservable(new Object())
                .subscribe(split((mainRxAcivity, o) -> mainRxAcivity.showMainRxActivity()));

    }

    private void sendSetFragmentChat(String channelId, String name, String type) {
        createTemplateObservable(new OpenChatObject(channelId,name, type))
                .subscribe(split((generalRxActivity1, openChatObject)
                        -> generalRxActivity1.setFragmentChat(openChatObject.getChannelId(), name, type)));
    }

    private void sendSetSelectChannel(String channelId, String type) {
        createTemplateObservable(new OpenChatObject(channelId, type))
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
