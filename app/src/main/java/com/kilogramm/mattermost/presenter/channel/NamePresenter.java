package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.channel.NameChannelActivity;

import icepick.State;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class NamePresenter extends BaseRxPresenter<NameChannelActivity> {
    private static final String TAG = "NamePresenter";
    private static final int REQUEST_UPDATE_CHANNEL = 1;

    private ApiMethod mService;

    @State
    String mTeamId;
    @State
    Channel mChannel;


    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        mService = mMattermostApp.getMattermostRetrofitService();

        initRequests();
    }

    public void initPresenter(String channelId) {
        Log.d(TAG, "initPresenter");
        this.mTeamId = MattermostPreference.getInstance().getTeamId();
        this.mChannel = new Channel(ChannelRepository.query(
                new ChannelRepository.ChannelByIdSpecification(channelId)).first());
    }

    //region Init Requests
    private void initRequests() {
        initName();
    }

    private void initName() {
        restartableFirst(REQUEST_UPDATE_CHANNEL,
                () -> mService.updateChannel(this.mTeamId, mChannel)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (nameActivity, channel) -> {
                    ChannelRepository.update(channel);
                    requestFinish("Saved successfully");
                }, (nameActivity, throwable) -> {
                    throwable.printStackTrace();
                    requestFinish("Unable to save");
                }
        );
    }

    public void requestUpdateChannel() {
        start(REQUEST_UPDATE_CHANNEL);
    }

    private void requestFinish(String s) {
        createTemplateObservable(new Object()).subscribe(split((purposeActivity, o) ->
                purposeActivity.requestSave(s)));
    }

    public Channel getChannel() {
        return mChannel;
    }

    public void setDisplayName(String displayName) {
        this.mChannel.setDisplayName(displayName);
    }

    public void setName(String name) {
        this.mChannel.setName(name);
    }


}
