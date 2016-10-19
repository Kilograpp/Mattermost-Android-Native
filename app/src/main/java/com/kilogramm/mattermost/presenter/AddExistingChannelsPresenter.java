package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.addchat.AddExistingChannelsActivity;

import io.realm.Realm;
import io.realm.RealmList;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 18.10.16.
 */

public class AddExistingChannelsPresenter extends BaseRxPresenter<AddExistingChannelsActivity> {
    private static final String TAG = "AddExistingChannelsPresenter";

    private static final int REQUEST_CHANNELS_MORE = 1;

    private MattermostApp mMattermostApp;
    private ApiMethod service;

    private String teamId;
    private Realm realm;

    private RealmList<ChannelsDontBelong> moreChannels;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();
        realm = Realm.getDefaultInstance();
        teamId = realm.where(Team.class).findFirst().getId();

        moreChannels = new RealmList<>();

        initRequest();
    }

    @Override
    protected void onTakeView(AddExistingChannelsActivity addExistingChannelsActivity) {
        super.onTakeView(addExistingChannelsActivity);
    }

    private void initRequest() {
        restartableFirst(REQUEST_CHANNELS_MORE, () -> {
            return service.channelsMore(teamId)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread());

        }, (addExistingChannelsActivity, channelsWithMembers) -> {
            realm.executeTransaction(realm1 -> {
//                RealmList<ChannelsDontBelong> moreChannels = new RealmList<>();
//                moreChannels.addAll(channelsWithMembers.getChannels());

                //TODO может быть сделать ChannelsDontBelong базовым, а Channel наследовать от него
                for (Channel channel : channelsWithMembers.getChannels()){
                    ChannelsDontBelong mChannel = new ChannelsDontBelong();
                    mChannel.setId(channel.getId());
                    mChannel.setCreateAt(channel.getCreateAt());
                    mChannel.setUpdateAt(channel.getUpdateAt());
                    mChannel.setDeleteAt(channel.getDeleteAt());
                    mChannel.setTeamId(channel.getTeamId());
                    mChannel.setType(channel.getType());
                    mChannel.setDisplayName(channel.getDisplayName());
                    mChannel.setName(channel.getName());
                    mChannel.setHeader(channel.getHeader());
                    mChannel.setPurpose(channel.getPurpose());
                    mChannel.setLastPostAt(channel.getLastPostAt());
                    mChannel.setTotalMsgCount(channel.getTotalMsgCount());
                    mChannel.setExtraUpdateAt(channel.getExtraUpdateAt());
                    mChannel.setCreatorId(channel.getCreatorId());

                    moreChannels.add(mChannel);
                }

                realm1.insertOrUpdate(moreChannels);
                realm1.close();
            });

        }, (addExistingChannelsActivity, throwable) -> {
            throwable.printStackTrace();
        });
    }

    public void requestChannelsMore() {
        start(REQUEST_CHANNELS_MORE);
    }
}
