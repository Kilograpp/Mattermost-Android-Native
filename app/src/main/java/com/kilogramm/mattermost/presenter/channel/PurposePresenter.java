package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.network.ServerMethod;
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

    @State
    String mTeamId;
    @State
    String mChannelId;
    @State
    String mPurpose;
    @State
    String mChannelType;

    public String getChannelType() {
        return mChannelType;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        initRequests();
    }

    public void initPresenter(Channel channel) {
        Log.d(TAG, "initPresenter");
        this.mTeamId = MattermostPreference.getInstance().getTeamId();
        this.mChannelId = channel.getId();
        this.mPurpose = channel.getPurpose();
        this.mChannelType = channel.getType();
    }

    //region Init Requests
    private void initRequests() {
        initPurpose();
    }

    private void initPurpose() {
        restartableFirst(REQUEST_UPDATE_PURPOSE,
                () -> ServerMethod.getInstance()
                        .updatePurpose(this.mTeamId, new ChannelPurpose(mChannelId, mPurpose))
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (purposeActivity, channel) -> {
                    ChannelRepository.update(channel);
                    requestFinish("Saved successfully");
                }, (purposeActivity, throwable) ->{
                        throwable.printStackTrace();
                    requestFinish("Unable to save");}
        );
    }

    public void requestUpdatePurpose() {
        start(REQUEST_UPDATE_PURPOSE);
    }

    private void requestFinish(String s) {
        createTemplateObservable(new Object()).subscribe(split((purposeActivity, o) ->
                purposeActivity.requestSave(s)));
    }

    public String getPurpose() {
        return mPurpose;
    }

    public void setHeader(String purpose) {
        this.mPurpose = purpose;
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
