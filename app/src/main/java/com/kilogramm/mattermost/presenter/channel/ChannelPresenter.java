package com.kilogramm.mattermost.presenter.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.ListSaveData;
import com.kilogramm.mattermost.model.entity.SaveData;
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
import io.realm.RealmResults;
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

    private ApiMethod service;

    private LogoutData user;

    @State
    ListSaveData mSaveData = new ListSaveData();

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
                (channelActivity, channel) -> {
                    RealmResults<Channel> leftChannel = ChannelRepository.query(new ChannelRepository.ChannelByIdSpecification(channelId));
                    String leftChannelName = leftChannel.first().getDisplayName();
                    ChannelRepository.remove(
                            new ChannelRepository.ChannelByIdSpecification(channel.getId()));
                    requestFinish(leftChannelName);
                }, (channelActivity, throwable) -> sendError(getError(throwable))
        );
    }

    private void initSaveRequest() {
        restartableFirst(REQUEST_SAVE, () -> Observable.defer(
                () -> Observable.zip(
                        service.save(mSaveData.getmSaveData())
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
            mSaveData.getmSaveData().clear();
            MattermostPreference.getInstance().setLastChannelId(channel.getId());
            sendSetFragmentChat();
        }, (generalRxActivity, throwable) -> throwable.printStackTrace());
    }

    public void requestSaveData(SaveData data, String userId) {
        mSaveData.getmSaveData().clear();
        mSaveData.getmSaveData().add(data);
        user.setUserId(userId);
        start(REQUEST_SAVE);
    }

    private void sendSetFragmentChat() {
        createTemplateObservable(new Object())
                .subscribe(split((channelActivity, o) ->
                        channelActivity.startGeneralActivity()));
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

//    private void requestFinish() {
//        createTemplateObservable(new Object()).subscribe(split((channelActivity, o) -> {
//            channelActivity.setResult(Activity.RESULT_OK, new Intent().putExtra(LEAVED_CHANNEL, channel.getName()));
//            channelActivity.finish();
//        }));
//    }
    private void requestFinish(String leftChannelName) {
        createTemplateObservable(new Object()).subscribe(split((channelActivity, o) -> {
            channelActivity.finishActivity(leftChannelName);
        }));
    }

    private void sendError(String error) {
        createTemplateObservable(error)
                .subscribe(split((channelActivity, s) -> Toast.makeText(channelActivity, s, Toast.LENGTH_SHORT).show()));
    }

    private void sendCloseActivity(){
        createTemplateObservable(new Object())
                .subscribe(split((channelActivity, o) -> channelActivity.finish()));
    }

}
