package com.kilogramm.mattermost.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.addchat.AddExistingChannelsActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmList;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 18.10.16.
 */

public class AddExistingChannelsPresenter extends BaseRxPresenter<AddExistingChannelsActivity> {
    public static final String TAG = "AddChannelsPresenter";

    private static final int REQUEST_CHANNELS_MORE = 1;
    private static final int REQUEST_ADD_CHAT = 2;
    private static final int REQUEST_JOIN_CHANNEL = 3;


    private String teamId;
    private List<ChannelsDontBelong> moreChannels;
    private LogoutData user;
    private String mChannelId;
    private String mChannelName;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        user = new LogoutData();
        teamId = MattermostPreference.getInstance().getTeamId();
        moreChannels = new ArrayList<>();
        initRequest();
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

    private void initRequest() {
        restartableFirst(REQUEST_CHANNELS_MORE,
                () -> ServerMethod.getInstance()
                        .channelsMore(teamId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (addExistingChannelsActivity, channelsList) -> {
                    if (channelsList.size() == 0) {
                        sendSetNoMoreChannels(true);
                        sendSetProgress(false);
                    } else {
                        Log.d(TAG, "initRequest: moreChannels: " + moreChannels.size());
                        for (Channel channel : channelsList) {
                            moreChannels.add(new ChannelsDontBelong(channel));
                        }

                        Realm.getDefaultInstance()
                                .executeTransaction(realm1 -> {
                                    realm1.delete(ChannelsDontBelong.class);
                                    realm1.insertOrUpdate(moreChannels);
                                });
                    }

                    Log.d(TAG, "initRequest: moreChannels: " + moreChannels.size());
                    sendSetRecycleView(true);
                    sendSetProgress(false);
                },
                (addExistingChannelsActivity, throwable) -> {
                    try {
                        Log.d(TAG, "initRequest: onError moreChannels: " + moreChannels.size());

                        sendShowError(parseError(throwable));
                        sendSetProgress(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });


        restartableFirst(REQUEST_ADD_CHAT, () ->
                        ServerMethod.getInstance()
                                .joinChannel(MattermostPreference.getInstance().getTeamId(), mChannelId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (generalRxActivity, channel) -> requestJoinChannel(),
                (generalRxActivity, throwable) -> {
                    throwable.printStackTrace();
                    sendSetProgress(false);
                });

        restartableFirst(REQUEST_JOIN_CHANNEL, () ->
                        ServerMethod.getInstance()
                                .joinChannelName(teamId, mChannelName)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                , (addExistingChannelsActivity, channel) -> makeChannelsTeamRequest(channel)
                , (addExistingChannelsActivity, throwable) -> {
                    throwable.printStackTrace();
                });
    }

    private void makeChannelsTeamRequest(Channel channel) {
        ServerMethod.getInstance()
                .getChannelsTeam(MattermostPreference.getInstance().getTeamId())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(channels -> {
                    RealmList<Channel> channelsList = new RealmList<>();
                    channelsList.addAll(channels);
                    ChannelRepository.remove(new ChannelRepository.ChannelByTypeSpecification("O"));
                    ChannelRepository.prepareChannelAndAdd(channelsList, MattermostPreference.getInstance().getMyUserId());
                    String channelName = Objects.equals(channel.getDisplayName(), "") ? channel.getName() : channel.getDisplayName();
                    sendSetProgress(false);
                    sendFinish(mChannelId, channelName, channel.getType());
                });
    }

    public void requestAddChat(String joinChannelId, String channelName) {
        mChannelName = channelName;
        mChannelId = joinChannelId;
        user.setUserId("");

        sendSetProgress(true);
        sendSetRecycleView(false);
        sendSetNoMoreChannels(false);

        start(REQUEST_ADD_CHAT);
    }

    public void requestChannelsMore() {
        sendSetProgress(true);
        start(REQUEST_CHANNELS_MORE);
    }

    private void requestJoinChannel() {
        start(REQUEST_JOIN_CHANNEL);
    }

    private void sendFinish(String joinChannelId, String channelName, String type) {
        createTemplateObservable(new Object())
                .subscribe(split((addExistingChannelsActivity, o) ->
                        addExistingChannelsActivity.finishActivity(joinChannelId, channelName, type))
                );
    }

    private void sendSetNoMoreChannels(Boolean bool) {
        createTemplateObservable(new Object())
                .subscribe(split((addExistingChannelsActivity, o) -> addExistingChannelsActivity.setNoChannels(bool)));
    }

    private void sendSetProgress(Boolean bool) {
        createTemplateObservable(bool)
                .subscribe(split((addExistingChannelsActivity, aBoolean) -> addExistingChannelsActivity.setProgress(bool)));
    }

    private void sendSetRecycleView(Boolean bool) {
        createTemplateObservable(bool)
                .subscribe(split((addExistingChannelsActivity, aBoolean) -> addExistingChannelsActivity.setRecycleView(bool)));
    }

    private void sendShowError(String error) {
        createTemplateObservable(error).subscribe(split((addExistingChannelsActivity, text) -> addExistingChannelsActivity.showErrorText(text, addExistingChannelsActivity.getCurrentFocus())));
    }
}
