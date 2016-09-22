package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.SaveData;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.fromnet.ChannelsWithMembers;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.network.MattermostHttpSubscriber;
import com.kilogramm.mattermost.view.menu.GeneralActivity;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import nucleus.presenter.Presenter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kraftu on 14.09.16.
 */
public class GeneralPresenter extends Presenter<GeneralActivity> {
    private static final String TAG = "GeneralPresenter";

    Realm realm;
    Subscription subscription;

    private boolean setDialogFragment = false;
    private SaveData mSaveData;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onTakeView(GeneralActivity generalActivity) {
        super.onTakeView(generalActivity);
        String teamId = realm.where(Team.class).findFirst().getId();
        loadChannels(teamId);
        Channel channel = realm.where(Channel.class).equalTo("type", "O").findFirst();
        if (channel != null) {
            setSelectedChannel(channel.getId(), channel.getName());
        }
    }

    public void loadChannels(String teamId) {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.getSingleton();
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getChannelsTeam(teamId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MattermostHttpSubscriber<ChannelsWithMembers>() {
                    @Override
                    public void onCompleted() {
                        if (setDialogFragment){
                            setSelectedDirect(mSaveData.getUser_id(), mSaveData.getName());
                            Log.d(TAG, "Must open direct dialog");
                            setDialogFragment = false;
                        }
                    }

                    @Override
                    public void onErrorMattermost(HttpError httpError, Throwable e) {
                        getView().showErrorText(httpError.toString());
                    }

                    @Override
                    public void onNext(ChannelsWithMembers channelsWithMembers) {
                        realm.executeTransaction(realm1 -> {
                            realm1.insertOrUpdate(channelsWithMembers.getChannels());
                            RealmList<User> users = new RealmList<User>();
                            users.addAll(channelsWithMembers.getMembers().values());
                            realm1.insertOrUpdate(users);
                        });
                    }
                });

    }

    public void save(SaveData saveData) {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();

        MattermostApp application = MattermostApp.getSingleton();
        ApiMethod service = application.getMattermostRetrofitService();
        if (saveData != null) {
            List<SaveData> toSend = new ArrayList<>();
            toSend.add(saveData);
            subscription = service.save(toSend)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Boolean>() {
                        @Override
                        public void onCompleted() {
                            Realm realm = Realm.getDefaultInstance();
                            String teamId = realm.where(Team.class).findFirst().getId();
                            realm.close();
                            loadChannels(teamId);
                            setDialogFragment = true;
                            mSaveData = saveData;
                            Log.d(TAG, "mSaveData created");
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            Log.d(TAG, "onNext");
                            if (!aBoolean) {
                                Log.d(TAG, "Save didn`t work out");
                            }
                        }
                    });
        }
    }

    public void setSelectedDirect(String itemId, String name) {
        String myId = realm.where(User.class).findFirst().getId();

        String channelId = realm.where(Channel.class)
                .equalTo("name", myId + "__" + itemId)
                .or()
                .equalTo("name", itemId + "__" + myId)
                .findFirst()
                .getId();
        if (getView() != null) {
            getView().setFragmentChat(channelId, name, false);
        } else {
            Log.d(TAG, "    getView() == null");
        }
    }

    public void setSelectedChannel(String channelId, String name) {
        getView().setFragmentChat(channelId, name, true);
    }
}
