package com.kilogramm.mattermost.presenter;

import android.os.Bundle;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.createChannelGroup.CreateNewChannelActivity;

import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 01.11.16.
 */

public class CreateNewChannelPresenter extends BaseRxPresenter<CreateNewChannelActivity> {
    private static final int REQUEST_CREATE_CHANNEL = 1;
//    private static final int REQUEST_GET_INFO = 2;

    private ApiMethod mService;
    private Channel mCreateChannel;
    private String mTeamId;
    private String mChannelId;
    private String mDisplayName;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        mService = MattermostApp.getSingleton().getMattermostRetrofitService();
        mCreateChannel = new Channel();
        mTeamId = MattermostPreference.getInstance().getTeamId();

        initRequests();
    }

    private void initRequests() {
        restartableFirst(REQUEST_CREATE_CHANNEL,
                () -> mService.createChannel(mTeamId, mCreateChannel)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (createNewChGrActivity, channel) -> {
                    if (channel != null) {
                        this.mChannelId = channel.getId();
                        this.mDisplayName = channel.getDisplayName();
                        sendFinishActivity(mChannelId, mDisplayName);
                    }
                    sendSetProgressVisibility(false);
                }, (createNewChGrActivity, throwable) -> {
                    sendShowError(parceError(throwable, CREATE_CHANNEL));
                    sendSetProgressVisibility(false);
                });
    }

//    private void getChannelsInfo() {
//        restartableFirst(REQUEST_GET_INFO, () -> Observable.defer(
//                () -> Observable.zip(
//                        mService.getChannelsTeam(this.mTeamId)
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(Schedulers.io()),
//                        mService.getExtraInfoChannel(this.mTeamId, this.mChannelId)
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(Schedulers.io()),
//                        (channelsWithMembers, extraInfo) -> {
//                            ChannelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
//                                    MattermostPreference.getInstance().getMyUserId());
//                            return extraInfo;
//                        })),
//                (createNewChGrActivity, extraInfo) -> {
//                    sendFinishActivity(mChannelId, mDisplayName);
//                    sendSetProgressVisibility(false);
//                }, (createNewChGrActivity, throwable) -> {
//                    this.sendShowError(getError(throwable));
//                    sendSetProgressVisibility(false);
//                });
//    }

    public void requestCreateChannel(String name, String displayName, String header, String purpose) {
        mCreateChannel.setAttributesToCreate(name, displayName, purpose, header, "O");
        sendSetProgressVisibility(true);
        start(REQUEST_CREATE_CHANNEL);
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
