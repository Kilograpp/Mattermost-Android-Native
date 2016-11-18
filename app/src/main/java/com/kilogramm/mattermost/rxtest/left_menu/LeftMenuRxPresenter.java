package com.kilogramm.mattermost.rxtest.left_menu;

import android.os.Bundle;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.ListPreferences;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 14.11.2016.
 */

public class LeftMenuRxPresenter extends BaseRxPresenter<LeftMenuRxFragment> {


    private static final int REQUEST_SAVE = 1;

    private ApiMethod service;
    private ListPreferences mListPreferences = new ListPreferences();
    private LogoutData user;


    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        user = new LogoutData();
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        initRequest();
    }

    private void initRequest() {
        initSaveRequest();
    }

    private void initSaveRequest() {
        restartableFirst(REQUEST_SAVE,
                () -> Observable.defer(() -> Observable.zip(
                        service.save(mListPreferences.getmSaveData())
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                        service.createDirect(MattermostPreference.getInstance().getTeamId(), user)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                        (aBoolean, channel) -> {
                            if (!aBoolean)
                                return null;
                            ChannelRepository.prepareDirectChannelAndAdd(channel, user.getUserId());
                            return channel;
                        })), (generalRxActivity, channel) -> {
                    mListPreferences.getmSaveData().clear();
                    sendSetFragmentChat(channel.getId(), channel.getUsername(), channel.getType());
                }, (generalRxActivity, throwable) -> throwable.printStackTrace());
    }

    public void requestSaveData(Preferences data, String userId) {
        mListPreferences.getmSaveData().clear();
        mListPreferences.getmSaveData().add(data);
        user.setUserId(userId);
        start(REQUEST_SAVE);
    }

    private void sendSetFragmentChat(String channelId, String name, String type) {
        createTemplateObservable(new Channel(channelId, name, type))
                .subscribe(split((leftMenuRxFragment, channel) ->
                        leftMenuRxFragment.onChannelClick(channel.getId(),
                                channel.getName(),
                                channel.getType())));
    }

}
