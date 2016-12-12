package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;
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
                    if (channelsWithMembers.getChannels().size() == 0) {
                        sendSetNoMoreChannels(true);
                        sendSetProgress(false);
                    } else {
                        for (Channel channel : channelsWithMembers.getChannels()) {
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
                },
                (addExistingChannelsActivity, throwable) -> {
                    sendShowError(parceError(throwable, CHANNELS_MORE));
                    sendSetProgress(false);
                });

        restartableFirst(REQUEST_ADD_CHAT, () ->
                        service.joinChannel(MattermostPreference.getInstance().getTeamId(), channelId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (generalRxActivity, channel) -> makeChannelsTeamRequest(channel),
                (generalRxActivity, throwable) -> {
                    throwable.printStackTrace();
                    sendSetProgress(false);
                }
        );
    }

    private void makeChannelsTeamRequest(Channel channel) {
        service.getChannelsTeam(MattermostPreference.getInstance().getTeamId())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(channelsWithMembers -> {
                    RealmList<Channel> channelsList = new RealmList<>();
                    channelsList.addAll(channelsWithMembers.getChannels());
                    ChannelRepository.remove(new ChannelRepository.ChannelByTypeSpecification("O"));
                    ChannelRepository.prepareChannelAndAdd(channelsList, MattermostPreference.getInstance().getMyUserId());
                    String channelName = Objects.equals(channel.getDisplayName(), "") ? channel.getName() : channel.getDisplayName();
                    sendSetProgress(false);
                    sendFinish(channelId, channelName, channel.getType());
                });
    }

    public void requestAddChat(String joinChannelId) {
        channelId = joinChannelId;
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
