package com.kilogramm.mattermost.presenter.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.channel.ChannelActivity;

import icepick.State;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class ChannelPresenter extends BaseRxPresenter<ChannelActivity> {
    private static final String TAG = "ChannelPresenter";
    private static final int REQUEST_EXTRA_INFO = 1;
    private static final int REQUEST_LEAVE = 2;
    private ApiMethod service;

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
    }

    private void initExtraInfo() {
        restartableFirst(REQUEST_EXTRA_INFO,
                () -> service.getExtraInfoChannel(this.teamId, this.channelId)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (channelActivity, extraInfo) -> {
                    this.extraInfo = extraInfo;

                    requestMembers();
                }, (channelActivity, throwable) ->
                        sendError(getError(throwable))
        );
    }

    private void leaveChannel() {
        restartableFirst(REQUEST_LEAVE,
                () -> service.leaveChannel(this.teamId, this.channelId)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (channelActivity, channel) -> {
                    ChannelRepository.remove(new
                            ChannelRepository.ChannelByIdSpecification(channel.getId()));
                    requestFinish();
                }, (channelActivity, throwable) ->
                        sendError(getError(throwable))
        );
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
                channelActivity.initiationData(extraInfo)));
    }

    private void requestFinish() {
        createTemplateObservable(new Object()).subscribe(split((channelActivity, o) -> {
            channelActivity.setResult(Activity.RESULT_OK, new Intent());
            channelActivity.finish();
        }));
    }

    private void sendError(String error) {
        createTemplateObservable(error).subscribe(split((channelActivity, s) ->
                Toast.makeText(channelActivity, s, Toast.LENGTH_LONG).show()));
    }

}
