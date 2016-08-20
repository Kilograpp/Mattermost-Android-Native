package com.kilogramm.mattermost.viewmodel.menu;

import android.content.Context;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kilogramm.mattermost.MattermostApplication;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.fromnet.Channels;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.viewmodel.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 03.08.2016.
 */
public class MenuViewModel implements ViewModel {

    private static final String TAG = "MenuViewModel";

    private ObservableInt listChannelsVisibility;
    private ObservableInt listDirectVisibility;
    private ObservableInt progressVisibility;

    private Realm realm;
    private Context context;
    private Subscription subscription;

    public MenuViewModel(Context context) {
        this.context = context;
        this.progressVisibility = new ObservableInt(View.VISIBLE);
        this.listChannelsVisibility = new ObservableInt(View.GONE);
        this.listDirectVisibility = new ObservableInt(View.GONE);
        this.realm = Realm.getDefaultInstance();
        loadChannels(realm.where(Team.class).findFirst().getId());
    }



    public void showListAndHideProgress(){
        setProgressVisibility(View.GONE);
        setListChannelsVisibility(View.VISIBLE);
        setListDirectVisibility(View.VISIBLE);
    }

    private void loadChannels(String teamId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApplication application = MattermostApplication.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getChannelsTeam(teamId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Channels>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete load channel");
                        refreshStatus();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error");
                    }

                    @Override
                    public void onNext(Channels channels) {
                        realm.executeTransaction(realm1 -> {
                            realm1.copyToRealmOrUpdate(channels.getChannels());
                        });
                        Log.d(TAG, "save in data base");
                    }
                });

    }

    private void refreshStatus(){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApplication application = MattermostApplication.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        List<String> list = new ArrayList<>();
        realm.where(Channel.class)
                .isNull("type")
                .findAll()
                .asObservable()
                .subscribe(channels1 ->{
                    for (Channel channel : channels1) {
                        list.add(channel.getId());
                    }
                });
        subscription = service.getStatus(list)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String,String>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete load status");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error");
                    }

                    @Override
                    public void onNext(Map<String,String> stringStringMap) {
                        realm.beginTransaction();
                        RealmResults<Channel> channels = realm.where(Channel.class)
                                .isNull("type")
                                .findAll();
                        for (Channel channel : channels) {
                            channel.setStatus(stringStringMap.get(channel.getId()));
                        }
                        realm.commitTransaction();
                    }
                });
    }

    public void onClickRefresh(View v){
        refreshStatus();
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

    public ObservableInt getListChannelsVisibility() {
        return listChannelsVisibility;
    }

    public void setListChannelsVisibility(Integer listChannelsVisibility) {
        this.listChannelsVisibility.set(listChannelsVisibility);
    }

    public ObservableInt getProgressVisibility() {
        return progressVisibility;
    }

    public void setProgressVisibility(Integer progressVisibility) {
        this.progressVisibility.set(progressVisibility);
    }

    public ObservableInt getListDirectVisibility() {
        return listDirectVisibility;
    }

    public void setListDirectVisibility(Integer listDirectVisibility) {
        this.listDirectVisibility.set(listDirectVisibility);
    }
}
