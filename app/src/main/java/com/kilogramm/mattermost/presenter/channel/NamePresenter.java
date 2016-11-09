package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.channel.NameActivity;

import icepick.State;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class NamePresenter extends BaseRxPresenter<NameActivity> {
    private static final String TAG = "NamePresenter";
    private static final int REQUEST_UPDATE_CHANNEL = 1;

    private ApiMethod service;

    @State
    String teamId;
    @State
    Channel channel;



    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();

        initRequests();
    }

    public void initPresenter(String channelId) {
        Log.d(TAG, "initPresenter");
        this.teamId = MattermostPreference.getInstance().getTeamId();
        this.channel = new Channel(ChannelRepository.query(
                new ChannelRepository.ChannelByIdSpecification(channelId)).first());
    }

    //region Init Requests
    private void initRequests() {
        initName();
    }

    private void initName() {
        restartableFirst(REQUEST_UPDATE_CHANNEL,
                () -> service.updateChannel(this.teamId, channel)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (nameActivity, channel) -> {
                    ChannelRepository.update(channel);
                    requestFinish();
                }, (nameActivity, throwable) ->
                        throwable.printStackTrace()
        );
    }

    public void requestUpdateChannel() {
        start(REQUEST_UPDATE_CHANNEL);
    }

    private void requestFinish() {
        createTemplateObservable(new Object()).subscribe(split((purposeActivity, o) ->
                purposeActivity.requestSave()));
    }

    public Channel getChannel() {
        return channel;
    }

    public void setDisplayName(String displayName) {
        this.channel.setDisplayName(displayName);
    }

    public void setName(String name) {
        this.channel.setName(name);
    }





}
