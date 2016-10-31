package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.addchat.AddExistingChannelsActivity;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 18.10.16.
 */

public class AddExistingChannelsPresenter extends BaseRxPresenter<AddExistingChannelsActivity> {
    public static final String TAG = "AddChannelsPresenter";

    private static final int REQUEST_CHANNELS_MORE = 1;
    private static final int REQUEST_ADD_CHAT = 2;

    private MattermostApp mMattermostApp;
    private ApiMethod service;

    private String teamId;

    private List<ChannelsDontBelong> moreChannels;


    private LogoutData user;
    private String channelId;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        user = new LogoutData();

        mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();
        teamId = MattermostPreference.getInstance().getTeamId();
        moreChannels = new ArrayList<>();
        initRequest();
    }

    @Override
    protected void onTakeView(AddExistingChannelsActivity addExistingChannelsActivity) {
        super.onTakeView(addExistingChannelsActivity);
    }

    private void initRequest() {
        restartableFirst(REQUEST_CHANNELS_MORE,
                () -> service.channelsMore(teamId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (addExistingChannelsActivity, channelsWithMembers) -> {
                    sendSetProgress(false);

                    if (channelsWithMembers.getChannels().size() == 0) {
                        sendSetNoMoreChannels(true);
                    } else {

                        for (Channel channel : channelsWithMembers.getChannels()) {
                            moreChannels.add(new ChannelsDontBelong(channel));
                        }
                        sendSetRecycleView(true);

                        Realm.getDefaultInstance()
                                .executeTransaction(realm1 -> {
                                    realm1.delete(ChannelsDontBelong.class);
                                    realm1.insertOrUpdate(moreChannels);
                                });
                    }},
                (addExistingChannelsActivity, throwable) -> {
                    throwable.printStackTrace();
                    sendSetProgress(false);
                });

        restartableFirst(REQUEST_ADD_CHAT, () -> Observable.defer(
                () -> Observable.zip(
                        service.joinChannel(MattermostPreference.getInstance().getTeamId(), channelId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                        service.getChannelsTeam(MattermostPreference.getInstance().getTeamId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                        (channel, channelsWithMembers) -> {
                            RealmList<Channel> channelsList = new RealmList<>();
                            channelsList.addAll(channelsWithMembers.getChannels());
                            ChannelRepository.remove(new ChannelRepository.ChannelByTypeSpecification("O"));
                            ChannelRepository.prepareChannelAndAdd(channelsList, MattermostPreference.getInstance().getMyUserId());
                            return channel;
         })), (generalRxActivity, channel) -> {
            sendSetProgress(false);
            sendFinish();
        }, (generalRxActivity, throwable) -> {
            throwable.printStackTrace();
            Log.d(TAG, throwable.getMessage());
            sendSetProgress(false);
        });
    }

    public void requestAddChat(String joinChannelId) {
        channelId = joinChannelId;
        user.setUserId("");
        start(REQUEST_ADD_CHAT);

        sendSetProgress(true);
        sendSetRecycleView(false);
    }

    public void requestChannelsMore() {
        start(REQUEST_CHANNELS_MORE);

        sendSetProgress(true);
    }

    private void sendFinish() {
        createTemplateObservable(new Object())
                .subscribe(split((addExistingChannelsActivity, o) -> addExistingChannelsActivity.finishActivity()));
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
}
