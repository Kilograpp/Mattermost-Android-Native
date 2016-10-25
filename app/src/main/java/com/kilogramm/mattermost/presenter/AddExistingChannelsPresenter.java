package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.addchat.AddExistingChannelsActivity;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 18.10.16.
 */

public class AddExistingChannelsPresenter extends BaseRxPresenter<AddExistingChannelsActivity> {

    private static final int REQUEST_CHANNELS_MORE = 1;

    private MattermostApp mMattermostApp;
    private ApiMethod service;

    private String teamId;

    private List<ChannelsDontBelong> moreChannels;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
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
                    for (Channel channel : channelsWithMembers.getChannels()) {
                        moreChannels.add(new ChannelsDontBelong(channel));
                    }

                    Realm.getDefaultInstance()
                            .executeTransaction(realm1 -> {
                                realm1.insertOrUpdate(moreChannels);
                            });
                },
                (addExistingChannelsActivity, throwable) -> throwable.printStackTrace());
    }

    public void requestChannelsMore() {
        start(REQUEST_CHANNELS_MORE);
    }
}
