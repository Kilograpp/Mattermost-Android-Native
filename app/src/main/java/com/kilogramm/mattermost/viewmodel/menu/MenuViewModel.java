package com.kilogramm.mattermost.viewmodel.menu;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApplication;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.model.fromnet.ChannelsWithMembers;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.viewmodel.ViewModel;

import io.realm.Realm;
import io.realm.RealmList;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 03.08.2016.
 */
public class MenuViewModel implements ViewModel {

    private static final String TAG = "MenuViewModel";

    private Realm realm;
    private Context context;
    private Subscription subscription;

    public MenuViewModel(Context context) {
        this.context = context;
        this.realm = Realm.getDefaultInstance();
        loadChannels(realm.where(Team.class).findFirst().getId());
    }

    private void loadChannels(String teamId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApplication application = MattermostApplication.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getChannelsTeam(teamId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ChannelsWithMembers>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete load channel");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error");
                    }

                    @Override
                    public void onNext(ChannelsWithMembers channelsWithMembers) {
                        realm.executeTransaction(realm1 -> {
                            realm1.insertOrUpdate(channelsWithMembers.getChannels());
                            RealmList<User> users = new RealmList<User>();
                            users.addAll(channelsWithMembers.getMembers().values());
                            realm1.insertOrUpdate(users);
                        });
                        Log.d(TAG, "save in data base");
                    }
                });

    }


    @Override
    public void destroy() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    //====================== getter setter ================================================
}
