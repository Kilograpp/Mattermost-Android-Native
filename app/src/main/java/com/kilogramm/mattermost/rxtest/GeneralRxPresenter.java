package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.ClientCfg;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.LicenseCfg;
import com.kilogramm.mattermost.model.entity.ListPreferences;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.ThemeProps;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelByHadleSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.member.MembersRepository;
import com.kilogramm.mattermost.model.entity.notifyProps.NotifyProps;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit2.adapter.rxjava.HttpException;
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
    private static final int REQUEST_INITLOAD = 7;
    private static final int REQUEST_EXTROINFO_DEFAULT_CHANNEL = 8;

    private ApiMethod service;

    @State
    ListPreferences listPreferences = new ListPreferences();

    private LogoutData user;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        user = new LogoutData();
        MattermostApp application = MattermostApp.getSingleton();
        service = application.getMattermostRetrofitService();
        initRequest();
        requestDirectProfile();
    }



    /*//TODO review evgenysuetin
    public void setSelectedLast(String id) {
        Log.d(TAG, "setSelectedLast");
        Channel channel;
        if (id != null) {
            try {
                channel = ChannelRepository.query(new ChannelRepository.ChannelByIdSpecification(id)).first();
                if (channel != null)
                    if (channel.getType().equals(Channel.DIRECT)) {
                        sendSetFragmentChat(channel.getId(), channel.getUsername(), channel.getType());
                    } else {
                        sendSetFragmentChat(channel.getId(), channel.getName(), channel.getType());
                    }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        } else {
            RealmResults<Channel> channels = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification(Channel.OPEN));
            if (channels.size() != 0) {
                sendSetFragmentChat(channels.first().getId(), channels.first().getName(), channels.first().getType());
            } else {
                channels.addChangeListener(element -> {
                    if (element.size() != 0) {
                        sendSetFragmentChat(element.first().getId(), element.first().getName(), element.first().getType());
                    }
                });
            }
        }
    }*/

    private void initRequest() {
        restartableFirst(REQUEST_DIRECT_PROFILE,
                () -> service.getDirectProfile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (generalRxActivity, stringUserMap) -> {
                    List<User> users = new ArrayList<>();
                    users.addAll(stringUserMap.values());
                    users.add(new User("materMostAll", "all", "Notifies everyone in the channel, use in Town Square to notify the whole team"));
                    users.add(new User("materMostChannel", "channel", "Notifies everyone in the channel"));
                    UserRepository.add(users);
                    requestLoadChannels();
                }, (generalRxActivity1, throwable) -> sendShowError(throwable.getMessage()));

        restartableFirst(REQUEST_LOAD_CHANNELS,
                () -> service.getChannelsTeam(MattermostPreference.getInstance().getTeamId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (generalRxActivity, channelsWithMembers) -> {
                    ChannelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
                            MattermostPreference.getInstance().getMyUserId());
                    MembersRepository.add(channelsWithMembers.getMembers().values());
                    requestUserTeam();
                    start(REQUEST_EXTROINFO_DEFAULT_CHANNEL);
                }, (generalRxActivity1, throwable) -> sendShowError(throwable.getMessage()));

        restartableFirst(REQUEST_USER_TEAM,
                () -> service.getTeamUsers(MattermostPreference.getInstance().getTeamId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (generalRxActivity, stringUserMap) -> {
                    UserRepository.add(stringUserMap.values());
                    if (MattermostPreference.getInstance().getLastChannelId() == null) {
                        Channel channel = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification(Channel.OPEN)).first();
                        //sendSetFragmentChat(channel.getId(), channel.getName(), channel.getType());
                        if (channel != null) {
                            sendSetFragmentChat(channel.getId(), channel.getDisplayName(), channel.getType());
                        }
                    } else {
                        Channel channel = ChannelRepository.query(new ChannelRepository.ChannelByIdSpecification(
                                MattermostPreference.getInstance().getLastChannelId())).first();
                        switch (channel.getType()) {
                            case Channel.DIRECT:
                                sendSetFragmentChat(channel.getId(), channel.getUsername(), channel.getType());
                                break;
                            default:
                                sendSetFragmentChat(channel.getId(), channel.getDisplayName(), channel.getType());
                                break;
                        }
                    }
                }, (generalRxActivity1, throwable) -> throwable.printStackTrace());

        restartableFirst(REQUEST_LOGOUT,
                () -> service.logout(new Object())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (generalRxActivity, logoutData) -> {
                    Log.d(TAG, "Complete logout");
                    clearDataBaseAfterLogout();
                    clearPreference();
                    sendShowMainRxActivity();
                }, (generalRxActivity1, throwable) -> {
                    throwable.printStackTrace();
                    Log.d(TAG, "Error logout");
                });

        restartableFirst(REQUEST_INITLOAD, () ->
                        service.initLoad()
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (generalRxActivity, initObject) -> {
                    saveDataAfterLogin(initObject);
                    sendShowChooseTeam();
                },
                (generalRxActivity, throwable) ->
                        handleErrorLogin(throwable)
        );

        restartableFirst(REQUEST_EXTROINFO_DEFAULT_CHANNEL, () ->
                        service.getExtraInfoChannel(MattermostPreference.getInstance().getTeamId(),
                                ChannelRepository.query(
                                        new ChannelByHadleSpecification("town-square"))
                                        .first()
                                        .getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (generalRxActivity, extraInfo) -> {
                    RealmList<User> results = new RealmList<>();
                    results.addAll(UserRepository.query(new UserRepository.UserByIdsSpecification(extraInfo.getMembers())));
                    extraInfo.setMembers(results);
                    ExtroInfoRepository.update(extraInfo);
                },
                (generalRxActivity, throwable) ->
                        handleErrorLogin(throwable)
        );
    }


    private List<Team> saveDataAfterLogin(InitObject initObject) {
        Realm mRealm = Realm.getDefaultInstance();
        mRealm.beginTransaction();
        RealmResults<ClientCfg> results = mRealm.where(ClientCfg.class).findAll();
        results.deleteAllFromRealm();
        mRealm.copyToRealmOrUpdate(initObject.getClientCfg());
        mRealm.copyToRealmOrUpdate(initObject);
        MattermostPreference.getInstance().setSiteName(initObject.getClientCfg().getSiteName());
        RealmList<User> directionProfiles = new RealmList<>();
        directionProfiles.addAll(initObject.getMapDerectProfile().values());
        mRealm.copyToRealmOrUpdate(directionProfiles);
        List<Team> teams = mRealm.copyToRealmOrUpdate(initObject.getTeams());
        mRealm.commitTransaction();
        return teams;
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
        MattermostApp.logout();
        //start(REQUEST_LOGOUT);
    }

    @Override
    protected void onTakeView(GeneralRxActivity generalRxActivity) {
        super.onTakeView(generalRxActivity);
        //loadChannels(realm.where(Team.class).findFirst().getId());
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
            realm1.delete(Preferences.class);
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

    private void sendShowChooseTeam() {
        createTemplateObservable(new Object())
                .subscribe(split((generalRxActivity, o) -> generalRxActivity.showTeemChoose()));
    }

    private void sendSetFragmentChat(String channelId, String name, String type) {
        createTemplateObservable(new Channel(channelId, type, name))
                .subscribe(split((generalRxActivity, channel)
                        -> generalRxActivity.setFragmentChat(channel.getId(), channel.getName(), channel.getType())));
    }

    public void setFirstChannelBeforeLeave() {
        Channel channel = ChannelRepository.query(new ChannelByHadleSpecification("town-square"))
                .first();
        sendSetFragmentChat(channel.getId(), channel.getName(), channel.getType());
    }

    private void handleErrorLogin(Throwable e) {
        if (e instanceof HttpException) {
            HttpError error;
            try {
                error = new Gson()
                        .fromJson((((HttpException) e)
                                .response()
                                .errorBody()
                                .string()), HttpError.class);
                Log.d(TAG, error.getMessage());
                Toast.makeText(getView(), error.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (IOException e1) {
                Log.d(TAG, "Message not has body.");
                e1.printStackTrace();
            }
        } else {
            Toast.makeText(getView(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SystemException, stackTrace: \n");
            e.printStackTrace();
        }
    }
}