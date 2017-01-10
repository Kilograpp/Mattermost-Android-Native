package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.addchat.AddExistingChannelsActivity;

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

    private MattermostApp mMattermostApp;
    private ApiMethod service;

    private String teamId;
    private List<ChannelsDontBelong> moreChannels;
    private LogoutData user;
    private String mChannelId;
    private String mChannelName;

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
                () -> ServerMethod.getInstance()
                        .channelsMore(teamId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (addExistingChannelsActivity, channelsList) -> {
                    if (channelsList.size() == 0) {
                        sendSetNoMoreChannels(true);
                        sendSetProgress(false);
                    } else {
                        for (Channel channel : channelsList) {
                            moreChannels.add(new ChannelsDontBelong(channel));
                        }

                        Realm.getDefaultInstance()
                                .executeTransaction(realm1 -> {
                                    realm1.delete(ChannelsDontBelong.class);
                                    realm1.insertOrUpdate(moreChannels);
                                });
                    }

                    sendSetRecycleView(true);
                    sendSetProgress(false);
                }, (addExistingChannelsActivity, throwable) -> {
                    sendShowError(parceError(throwable, CHANNELS_MORE));
                    sendSetProgress(false);
                });

        restartableFirst(REQUEST_ADD_CHAT, () ->
                        ServerMethod.getInstance()
                                .joinChannel(MattermostPreference.getInstance().getTeamId(), channelId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (generalRxActivity, channel) -> requestJoinChannel(),
                (generalRxActivity, throwable) -> {
                    throwable.printStackTrace();
                    sendSetProgress(false);
                });

        restartableFirst(REQUEST_JOIN_CHANNEL, () ->
                        service.joinChannelName(teamId, mChannelName)
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
        createTemplateObservable(error).subscribe(split(BaseActivity::showErrorText));
    }
}
