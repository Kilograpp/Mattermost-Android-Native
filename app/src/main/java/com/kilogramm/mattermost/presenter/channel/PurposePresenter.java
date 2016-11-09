package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.channel.PurposeActivity;

import icepick.State;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class PurposePresenter extends BaseRxPresenter<PurposeActivity> {
    private static final String TAG = "PurposePresenter";
    private static final int REQUEST_UPDATE_PURPOSE = 1;

    private ApiMethod service;

    @State
    String teamId;
    @State
    String channelId;
    @State
    String purpose;


    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();

        initRequests();
    }

    public void initPresenter(String purpose, String channelId) {
        Log.d(TAG, "initPresenter");
        this.teamId = MattermostPreference.getInstance().getTeamId();
        this.channelId = channelId;
        this.purpose = purpose;
    }

    //region Init Requests
    private void initRequests() {
        initPurpose();
    }

    private void initPurpose() {
        restartableFirst(REQUEST_UPDATE_PURPOSE,
                () -> service.updatePurpose(this.teamId, new ChannelPurpose(channelId, purpose))
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (purposeActivity, channel) -> {
                    ChannelRepository.update(channel);
                    requestFinish();
                }, (purposeActivity, throwable) ->
                        throwable.printStackTrace()
        );
    }

    public void requestUpdatePurpose() {
        start(REQUEST_UPDATE_PURPOSE);
    }

    private void requestFinish() {
        createTemplateObservable(new Object()).subscribe(split((purposeActivity, o) ->
                purposeActivity.requestSave()));
    }

    public String getPurpose() {
        return purpose;
    }

    public void setHeader(String purpose) {
        this.purpose = purpose;
    }

    public class ChannelPurpose {
        @SerializedName("channel_id")
        @Expose
        String channel_id;

        @SerializedName("channel_purpose")
        @Expose
        String channel_purpose;

        public ChannelPurpose(String channel_id, String channel_purpose) {
            this.channel_id = channel_id;
            this.channel_purpose = channel_purpose;
        }

    }


}
