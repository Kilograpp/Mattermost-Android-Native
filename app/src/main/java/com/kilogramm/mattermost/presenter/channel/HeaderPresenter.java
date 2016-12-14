package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.channel.HeaderChannelActivity;

import icepick.State;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class HeaderPresenter extends BaseRxPresenter<HeaderChannelActivity> {
    private static final String TAG = "HeaderPresenter";
    private static final int REQUEST_UPDATE_HEADER = 1;

    private ApiMethod mService;

    @State
    String mTeamId;
    @State
    String mChannelId;
    @State
    String mHeader;
    @State
    String mTypeChannel;

    public String getTypeChannel() {
        return mTypeChannel;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        mService = mMattermostApp.getMattermostRetrofitService();

        initRequests();
    }

    public void initPresenter(Channel channel) {
        Log.d(TAG, "initPresenter");
        this.mTeamId = MattermostPreference.getInstance().getTeamId();
        this.mChannelId = channel.getId();
        this.mHeader = channel.getHeader();
        this.mTypeChannel = channel.getType();
    }

    public void requestUpdateHeader() {
        start(REQUEST_UPDATE_HEADER);
    }

    public String getHeader() {
        return mHeader;
    }

    public void setHeader(String header) {
        this.mHeader = header;
    }

    public class ChannelHeader {
        @SerializedName("channel_id")
        @Expose
        String channel_id;

        @SerializedName("channel_header")
        @Expose
        String channel_header;

        public ChannelHeader(String channel_id, String channel_header) {
            this.channel_id = channel_id;
            this.channel_header = channel_header;
        }

    }

    //region Init Requests
    private void initRequests() {
        initHeader();
    }

    private void initHeader() {
        restartableFirst(REQUEST_UPDATE_HEADER,
                () -> mService.updateHeader(this.mTeamId, new ChannelHeader(mChannelId, mHeader))
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (headerActivity, channel) -> {
                    ChannelRepository.update(channel);
                    requestFinish("Saved successfully");
                }, (headerActivity, throwable) -> {
                    throwable.printStackTrace();
                    requestFinish("Unable to save");
                }
        );
    }

    private void requestFinish(String s) {
        createTemplateObservable(new Object()).subscribe(split((headerActivity, o) ->
                headerActivity.requestSave(s)));
    }
}
