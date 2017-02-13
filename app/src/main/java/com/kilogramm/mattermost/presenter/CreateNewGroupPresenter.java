package com.kilogramm.mattermost.presenter;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.JsonSyntaxException;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.createChannelGroup.CreateNewGroupActivity;

import java.io.IOException;

import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 01.11.16.
 */

public class CreateNewGroupPresenter extends BaseRxPresenter<CreateNewGroupActivity> {
    private static final int REQUEST_CREATE_GROUP = 1;

    private Channel mCreateChannel;
    private String mTeamId;
    private String mChannelId;
    private String mDisplayName;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        mCreateChannel = new Channel();
        mTeamId = MattermostPreference.getInstance().getTeamId();

        initRequests();
    }

    @Override
    public String parseError(Throwable e) {
        try {
            HttpError httpError = getErrorFromResponse(e);
            Context context = MattermostApp.getSingleton().getApplicationContext();
            switch (httpError.getStatusCode()) {
                case 403:
                    return context.getString(R.string.error_not_belong_to_team);
                case 500:
                    return context.getString(R.string.error_channel_exists);
                default:
                    return httpError.getMessage();
            }
        } catch (IOException | JsonSyntaxException e1) {
            e1.printStackTrace();
            return super.parseError(e);
        }
    }

    private void initRequests() {
        restartableFirst(REQUEST_CREATE_GROUP,
                () -> ServerMethod.getInstance()
                        .createChannel(mTeamId, mCreateChannel)
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
                    sendShowError(parseError(throwable));
                    sendSetProgressVisibility(false);
                });
    }

    public void requestCreateGroup(String name, String displayName, String header, String purpose) {
        mCreateChannel.setAttributesToCreate(name, displayName, purpose, header, "P");
        sendSetProgressVisibility(true);
        start(REQUEST_CREATE_GROUP);
    }

    public void sendShowError(String error) {
        createTemplateObservable(error)
                .subscribe(split(BaseActivity::showErrorText));
    }

    private void sendFinishActivity(String groupId, String groupName) {
        createTemplateObservable(new Object())
                .subscribe(split((createNewChGrActivity, o) ->
                        createNewChGrActivity.finishActivity(groupId, groupName)));
    }

    private void sendSetProgressVisibility(boolean bool) {
        createTemplateObservable(bool)
                .subscribe(split((createNewGroupActivity, aBoolean) ->
                        createNewGroupActivity.setProgressVisibility(bool)));
    }
}
