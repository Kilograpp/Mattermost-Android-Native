package com.kilogramm.mattermost.presenter;

import android.os.Bundle;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.createChannelGroup.CreateNewChannelActivity;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 01.11.16.
 */

public class CreateNewChannelPresenter extends BaseRxPresenter<CreateNewChannelActivity> {
    private static final int REQUEST_CREATE_CHANNEL = 1;
    private static final int REQUEST_GET_INFO = 2;

    private ApiMethod service;

    private Channel createChannel;
    private String teamId;
    private String channelId;
    private String displayName;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        createChannel = new Channel();
        teamId = MattermostPreference.getInstance().getTeamId();

        initRequests();
        getChannelsInfo();
    }

    private void initRequests() {
        restartableFirst(REQUEST_CREATE_CHANNEL,
                () -> service.createChannel(teamId, createChannel)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()
                        ), (createNewChGrActivity, channel) -> {
                    if (channel != null) {
                        this.channelId = channel.getId();
                        this.displayName = channel.getDisplayName();
                        requestGetChannelInfo();
                    } else {
                        sendSetProgressVisibility(false);
                    }
                }, (createNewChGrActivity, throwable) -> {
                    sendShowError(parceError(throwable, CREATE_CHANNEL));
                    sendSetProgressVisibility(false);
                });
    }

    private void getChannelsInfo() {
        restartableFirst(REQUEST_GET_INFO, () -> Observable.defer(
                () -> Observable.zip(
                        service.getChannelsTeam(this.teamId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                        service.getExtraInfoChannel(this.teamId, this.channelId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                        (channelsWithMembers, extraInfo) -> {
                            ChannelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
                                    MattermostPreference.getInstance().getMyUserId());
                            return extraInfo;
                        })),
                (createNewChGrActivity, extraInfo) -> {
                    sendFinishActivity(channelId, displayName);
                    sendSetProgressVisibility(false);
                }, (createNewChGrActivity, throwable) -> {
                    this.sendShowError(getError(throwable));
                    sendSetProgressVisibility(false);
                });
    }

    public void requestCreateChannel(String name, String displayName, String header, String purpose) {
        createChannel.setAttributesToCreate(name, displayName, purpose, header, "O");
        sendSetProgressVisibility(true);
        start(REQUEST_CREATE_CHANNEL);
    }

    public void requestGetChannelInfo() {
        start(REQUEST_GET_INFO);
    }

    public void sendShowError(String error) {
        createTemplateObservable(error)
                .subscribe(split(BaseActivity::showErrorText));
    }

    private void sendFinishActivity(String createdChannelId, String channelName) {
        createTemplateObservable(new Object())
                .subscribe(split((createNewChGrActivity, o) ->
                        createNewChGrActivity.finishActivity(createdChannelId, channelName)));
    }

    private void sendSetProgressVisibility(boolean bool) {
        createTemplateObservable(bool)
                .subscribe(split((createNewGroupActivity, aBoolean) ->
                        createNewGroupActivity.setProgressVisibility(bool)));
    }
}
