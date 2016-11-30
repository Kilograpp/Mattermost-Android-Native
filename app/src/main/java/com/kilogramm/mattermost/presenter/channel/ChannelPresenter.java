package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.ListPreferences;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.channel.ChannelActivity;

import icepick.State;
import io.realm.RealmList;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class ChannelPresenter extends BaseRxPresenter<ChannelActivity> {
    private static final String TAG = "ChannelPresenter";
    private static final int REQUEST_EXTRA_INFO = 1;
    private static final int REQUEST_LEAVE = 2;
    private static final int REQUEST_SAVE = 3;
    private static final int REQUEST_CHANNEL = 4;

    private ApiMethod service;

    private LogoutData user;

    @State
    ListPreferences listPreferences = new ListPreferences();

    @State
    ExtraInfo extraInfo;
    @State
    String teamId;
    @State
    String channelId;
    @State
    Channel channel;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();

        initRequests();
    }

    public void initPresenter(String teamId, String channelId) {
        Log.d(TAG, "initPresenter");
        this.teamId = teamId;
        this.channelId = channelId;
        this.channel = ChannelRepository.query(new ChannelRepository.ChannelByIdSpecification(channelId)).first();
    }

    //region Init Requests
    private void initRequests() {
        initExtraInfo();
        leaveChannel();
        initSaveRequest();
    }

    private void initExtraInfo() {
        restartableFirst(REQUEST_EXTRA_INFO,
                () -> service.getExtraInfoChannel(this.teamId, this.channelId)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (channelActivity, extraInfo) -> {
                    RealmList<User> results = new RealmList<>();
                    results.addAll(UserRepository.query(new UserRepository.UserByIdsSpecification(extraInfo.getMembers())));
                    extraInfo.setMembers(results);
                    ExtroInfoRepository.add(extraInfo);
                    start(REQUEST_CHANNEL);
                }, (channelActivity, throwable) -> {
                    sendError("Error loading channel info");
                    sendCloseActivity();
                }
        );

        restartableFirst(REQUEST_CHANNEL,
                () -> service.getChannel(this.teamId, this.channelId)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (channelActivity, channelWithMember) -> {
                    ChannelRepository.update(channelWithMember.getChannel());
                    requestMembers();
                }, (channelActivity, throwable) -> {
                    sendError("Error loading channel info");
                    sendCloseActivity();
                }
        );
    }

    private void leaveChannel() {
        restartableFirst(REQUEST_LEAVE,
                () -> service.leaveChannel(this.teamId, this.channelId)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (channelActivity, channel) -> requestFinish(),
                (channelActivity, throwable) -> sendError(getError(throwable))
        );
    }

    private void initSaveRequest() {
        restartableFirst(REQUEST_SAVE, () -> Observable.defer(
                () -> Observable.zip(
                        service.save(listPreferences.getmSaveData())
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                        service.createDirect(MattermostPreference.getInstance().getTeamId(), user)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                        (aBoolean, channel) -> {
                            if (aBoolean == Boolean.FALSE) {
                                return null;
                            }
                            ChannelRepository.prepareDirectChannelAndAdd(channel, user.getUserId());
                            return channel;
                        })), (generalRxActivity, channel) -> {
            listPreferences.getmSaveData().clear();
            MattermostPreference.getInstance().setLastChannelId(channel.getId());
            sendSetFragmentChat();
        }, (generalRxActivity, throwable) -> throwable.printStackTrace());
    }

    public void requestSaveData(Preferences data, String userId) {
        listPreferences.getmSaveData().clear();
        listPreferences.getmSaveData().add(data);
        user.setUserId(userId);
        start(REQUEST_SAVE);
    }

    private void sendSetFragmentChat() {
        createTemplateObservable(new Object())
                .subscribe(split((channelActivity, o) -> channelActivity.startGeneralActivity()));
    }

    public void requestExtraInfo() {
        start(REQUEST_EXTRA_INFO);
    }

    public void requestLeave() {
        start(REQUEST_LEAVE);
    }

    public Channel getChannel() {
        return channel;
    }

    private void requestMembers() {
        createTemplateObservable(new Object()).subscribe(split((channelActivity, o) ->
                channelActivity.initiationData(ExtroInfoRepository.query(
                        new ExtroInfoRepository.ExtroInfoByIdSpecification(channelId)).first())));
    }

    private void requestFinish() {
        createTemplateObservable(new Object()).subscribe(split((channelActivity, o) -> {
            Toast.makeText(channelActivity,
                    String.format("You've just leaved %s %s",
                                this.channel.getDisplayName(),
                                channel.getType().equals(Channel.OPEN) ? "channel" : "private group"),
                    Toast.LENGTH_SHORT).show();
            ChannelRepository.remove(channel);
            channelActivity.finishActivity();
        }));
    }

    private void sendError(String error) {
        createTemplateObservable(error).subscribe(split((channelActivity, s) -> {
            Toast.makeText(channelActivity, s, Toast.LENGTH_SHORT).show();
            channelActivity.errorRequest();
        }));
    }

    private void sendCloseActivity() {
        createTemplateObservable(new Object())
                .subscribe(split((channelActivity, o) -> channelActivity.finish()));
    }
}
