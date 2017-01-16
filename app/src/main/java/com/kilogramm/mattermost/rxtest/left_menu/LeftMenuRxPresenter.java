package com.kilogramm.mattermost.rxtest.left_menu;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.ListPreferences;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.rxtest.GeneralRxActivity;
import com.kilogramm.mattermost.rxtest.LoginRxActivity;

import rx.schedulers.Schedulers;


public class LeftMenuRxPresenter extends BaseRxPresenter<LeftMenuRxFragment> {
    private static final String TAG = LeftMenuRxPresenter.class.getName();

    private static final int REQUEST_SAVE = 1;
    private static final int REQUEST_UPDATE = 2;

    private ListPreferences mListPreferences = new ListPreferences();
    private LogoutData mUser;

    private String mTeamId;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mUser = new LogoutData();
        mTeamId = MattermostPreference.getInstance().getTeamId();
        initRequest();
    }


    public void requestSaveData(Preferences data, String userId) {
        mListPreferences.getmSaveData().clear();
        mListPreferences.getmSaveData().add(data);
        mUser.setUserId(userId);
        start(REQUEST_SAVE);
    }

    public void requestUpdate() {
        start(REQUEST_UPDATE);
    }

    private void initRequest() {
        initSaveRequest();
        initChannelUpdateRequest();
    }

    private void initChannelUpdateRequest() {
        restartableFirst(REQUEST_UPDATE,
                () -> ServerMethod.getInstance()
                        .getChannelsTeam(mTeamId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (leftMenuRxFragment, channels) -> {
                    ChannelRepository.prepareChannelAndAdd(channels, MattermostPreference.getInstance().getMyUserId());
                    sendSetRefreshAnimation(false);
                    sendUpdateMenuView();
                    sendSelectLastChannel();
                }, (leftMenuRxFragment, throwable) -> {
                    throwable.printStackTrace();
                    sendSetRefreshAnimation(false);
                    Log.d(TAG, throwable.getMessage());
                });

    }

    private void initSaveRequest() {
        restartableFirst(REQUEST_SAVE,
                () -> ServerMethod.getInstance()
                        .saveOrCreateDirectChannel(mListPreferences.getmSaveData(),
                                mTeamId,
                                mUser.getUserId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (leftMenuRxFragment, channel) -> {
                    if (channel != null) {
                        ChannelRepository.prepareDirectChannelAndAdd(channel, mUser.getUserId());
                        mListPreferences.getmSaveData().clear();
                        if (channel.getId() != null)
                            sendSetFragmentChat(channel.getId(), channel.getUsername(), channel.getType());
                    }
                }, (leftMenuRxFragment, throwable) -> throwable.printStackTrace());
    }

    private void sendSetFragmentChat(String channelId, String name, String type) {
        createTemplateObservable(new Channel(channelId, name, type))
                .subscribe(split((leftMenuRxFragment, channel) -> {
                    leftMenuRxFragment.onChannelClick(channel.getId(),
                            channel.getName(),
                            channel.getType());
                    leftMenuRxFragment.setSelectItemMenu(channel.getId(),
                            channel.getType());

                }));
    }

    private void sendSetRefreshAnimation(Boolean bool) {
        createTemplateObservable(bool).subscribe(split(
                (leftMenuRxFragment, aBoolean) -> leftMenuRxFragment.setRefreshAnimation(bool)));
    }

    private void sendUpdateMenuView() {
        createTemplateObservable(new Object()).subscribe(split(
                (leftMenuRxFragment, o) -> leftMenuRxFragment.initView()));
    }

    private void sendSelectLastChannel() {
        createTemplateObservable(new Object()).subscribe(split(
                (leftMenuRxFragment, o) -> leftMenuRxFragment.selectLastChannel()));
    }
}
