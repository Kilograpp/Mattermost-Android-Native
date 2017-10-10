package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.LicenseCfg;
import com.kilogramm.mattermost.model.entity.Preference.PreferenceRepository;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.ThemeProps;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelByHadleSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.notifyProps.NotifyProps;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ServerMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import icepick.State;
import io.realm.Realm;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
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
//    private static final int REQUEST_CREATE_DIRECT = 9;

    private int mOffset;
    private int mLimit;
    private LogoutData user;

    @State
    String currentUserId;
    @State
    String teamId;

    List<Preferences> preferenceList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        user = new LogoutData();
        currentUserId = MattermostPreference.getInstance().getMyUserId();
        teamId = MattermostPreference.getInstance().getTeamId();
        mOffset = 0;
        mLimit = 100;

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
                () -> ServerMethod.getInstance()
                        .getSiteAllUsers(mOffset, mLimit)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (activity, stringUserMap) -> {
                    List<User> users = new ArrayList<>();
                    /*if (stringUserMap.keySet().size() == 100) {
                        this.mOffset += 100;
                        requestDirectProfile();
                    } else {*/
                        users.addAll(stringUserMap.values());
                        UserRepository.add(users);
                    //}

                    Log.d(TAG, users.toString());

                    users.add(new User("materMostAll", "all", "Notifies everyone in the channel, use in Town Square to notify the whole team"));
                    users.add(new User("materMostChannel", "channel", "Notifies everyone in the channel"));

                    requestLoadChannels();
                }, (generalRxActivity1, throwable) -> {
                    sendShowError(parceError(throwable, null));
                    relogIfDirectProfileFailed(throwable);                  // simple logout on error #bugfix
                } );// TODO: 18.01.17  entry point

        restartableFirst(REQUEST_LOAD_CHANNELS,
                () -> ServerMethod.getInstance()
                        .getChannelsTeam(teamId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (activity, channelsCollection) -> {
                    ArrayList<Channel> channels = new ArrayList<>();
                    channels.addAll(channelsCollection);

                    ChannelRepository.prepareChannelAndAdd(channels,
                            MattermostPreference.getInstance().getMyUserId());
                    Log.d(TAG, channels.toString());
                    requestUserTeam();

                    //start(REQUEST_EXTROINFO_DEFAULT_CHANNEL);
                }, (generalRxActivity1, throwable) -> sendShowError(parceError(throwable, null)));


        restartableFirst(REQUEST_USER_TEAM,
                () -> Observable.just("test"),
                (generalRxActivity, stringUserMap) -> {
                    Log.d(TAG, "initRequest: " + stringUserMap);
                    //UserRepository.add(stringUserMap.values());
                    if (MattermostPreference.getInstance().getLastChannelId() == null) {
                        Channel channel = ChannelRepository.query(
                                new ChannelRepository.ChannelByTypeSpecification(Channel.OPEN)).first();
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
                () -> ServerMethod.getInstance()
                        .logout()
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (generalRxActivity, logoutData) -> {
                    Log.d(TAG, "Complete logout");
                    clearDataBaseAfterLogout();
                    clearPreference();
                    sendShowMainRxActivity();
                }, (generalRxActivity1, throwable) -> {
                    sendShowError(parceError(throwable, null));
                    throwable.printStackTrace();
                    Log.d(TAG, "Error logout");
                });

        restartableFirst(REQUEST_SAVE, () ->
                ServerMethod.getInstance()
                        .save(preferenceList)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (activity, aBoolean) ->
                    PreferenceRepository.add(preferenceList)
                , (activity, throwable) ->
                        sendShowError(parceError(throwable, SAVE_PREFERENCES))
        );
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

    public void requestLogout(){
        ServerMethod.getInstance().logout()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<LogoutData>() {
                    @Override
                    public void onCompleted() {
                        logoutCleaning();
                    }

                    @Override
                    public void onError(Throwable e) {
                        logoutCleaning();
                    }

                    @Override
                    public void onNext(LogoutData logoutData) {
                    }
                });
    }

    private void logoutCleaning() {
        MattermostApp.clearDataBaseAfterLogout();
        MattermostApp.clearPreference();
        MattermostApp.showMainRxActivity();
    }

//    public void requestLogout() {
//        MattermostApp.logout().subscribe(new Subscriber<LogoutData>() {
//            @Override
//            public void onCompleted() {
//                Log.d(TAG, "Complete logout");
//                MattermostApp.clearDataBaseAfterLogout();
//                MattermostApp.clearPreference();
//                MattermostApp.showMainRxActivity();
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                e.printStackTrace();
//                sendShowError(parceError(e, "Error logout"));
//            }
//
//            @Override
//            public void onNext(LogoutData logoutData) {
//            }
//        });
////        start(REQUEST_LOGOUT);
//    }

    public void requestSave(String name, String idUserToStartDialogWith) {
        preferenceList.add(new Preferences(
                idUserToStartDialogWith,
                currentUserId,
                true,
                "direct_channel_show"));

        start(REQUEST_SAVE);
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
                        -> generalRxActivity.setFragmentChat(ChatFragmentV2.START_NORMAL, channel.getId(), channel.getName(), channel.getType())));
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
                if(error != null) {
                    Toast.makeText(getView(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
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

    /** Give it a throwable (error) from onError() or smth and it gives brings you the login menu
     *  if the error.getMessage() contains "Unauthorized"
     *
     * @param throwable a throwable wich should contain "Unauthorized" in its Message
     */
    private void relogIfDirectProfileFailed(Throwable throwable) {
        if (throwable.getMessage().toLowerCase().contains("unauthorized")) {
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                Log.d(TAG + "Sleep", e.getMessage());
            }
            relog();
        }
    }

    /** Brings you to the MainRxActivity, while clearing the Preferences (old login data)
     *
     */
    private void relog() {
        try {
            //MattermostApp.clearPreference();
            MattermostPreference.getInstance().setAuthToken(null);
            MattermostApp.showMainRxActivity();
        } catch (Exception e) {
            Log.d(TAG + "Relog", e.getMessage());
        }
    }
}