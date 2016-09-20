package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.fromnet.ChannelsWithMembers;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.network.MattermostHttpSubscriber;
import com.kilogramm.mattermost.view.menu.GeneralActivity;

import io.realm.Realm;
import io.realm.RealmList;
import nucleus.presenter.Presenter;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kraftu on 14.09.16.
 */
public class GeneralPresenter extends Presenter<GeneralActivity> {

    Realm realm;
    Subscription subscription;

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
        if(channel!=null){
            setSelectedChannel(channel.getId(),channel.getName());
        }




        //loadChannels(realm.where(Team.class).findFirst().getId());
    }

    private void loadChannels(String teamId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.getSingleton();
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getChannelsTeam(teamId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MattermostHttpSubscriber<ChannelsWithMembers>() {
                    @Override
                    public void onCompleted() {

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

    public void setSelectedDirect(String itemId,String name){
        String myId = realm.where(User.class).findFirst().getId();

        String channelId = realm.where(Channel.class)
                .equalTo("name", myId + "__" + itemId)
                .or()
                .equalTo("name", itemId + "__" + myId)
                .findFirst()
                .getId();
        getView().setFragmentChat(channelId,name,false);
    }

    public void setSelectedChannel(String channelId,String name){
        getView().setFragmentChat(channelId,name,true);
    }
}
