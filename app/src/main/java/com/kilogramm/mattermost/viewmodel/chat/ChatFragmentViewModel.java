package com.kilogramm.mattermost.viewmodel.chat;

import android.content.Context;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kilogramm.mattermost.MattermostApplication;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.viewmodel.ViewModel;

import io.realm.Realm;
import io.realm.RealmList;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class ChatFragmentViewModel implements ViewModel {

    private static final String TAG = "ChatFragmentViewModel";

    private Realm realm;
    private Context context;
    private String channelId;
    private Subscription subscription;
    private ObservableInt listVisibility;
    private ObservableInt isVisibleProgress;
    private ObservableInt newMessageVis;

    public ChatFragmentViewModel(Context context, String channelId) {
        this.context = context;
        this.channelId = channelId;
        this.realm = Realm.getDefaultInstance();
        this.listVisibility = new ObservableInt(View.GONE);
        this.isVisibleProgress = new ObservableInt(View.VISIBLE);
        this.newMessageVis = new ObservableInt(View.GONE);
        Team teamId = realm.where(Team.class).findFirst();
        getExtraInfo(teamId.getId(),
                channelId);
    }

    private void getExtraInfo(String teamId, String channelId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApplication application = MattermostApplication.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getExtraInfoChannel(teamId,channelId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<ExtraInfo>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete load extra_info");
                        loadPosts(teamId, channelId);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error extra_info");
                    }

                    @Override
                    public void onNext(ExtraInfo extraInfo) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                            RealmList<User> list = new RealmList<User>();
                            list.addAll(extraInfo.getMembers());
                            realm.insertOrUpdate(list);
                        realm.commitTransaction();
                        realm.close();
                    }
                });
    }

    private void loadPosts(String teamId, String channelId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApplication application = MattermostApplication.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getPosts(teamId,channelId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .flatMap(posts -> Observable.from(posts.getPosts().values()))
                .subscribe(new Subscriber<Post>() {
                    @Override
                    public void onCompleted() {
                        isVisibleProgress.set(View.GONE);
                        listVisibility.set(View.VISIBLE);
                        newMessageVis.set(View.VISIBLE);
                        Log.d(TAG, "Complete load post");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error");
                    }

                    @Override
                    public void onNext(Post post) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                            post.setUser(realm.where(User.class)
                                    .equalTo("id", post.getUserId())
                                    .findFirst());
                            realm.copyToRealmOrUpdate(post);
                        realm.commitTransaction();
                        realm.close();
                    }
                });
    }

    @Override
    public void destroy() {
        Log.d(TAG, "OnDestroy()");
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        subscription = null;
        context = null;
        realm.close();
    }




    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }


    public String getChannelId() {
        return channelId;
    }

    public Context getContext() {
        return context;
    }


    public ObservableInt getListVisibility() {
        return listVisibility;
    }

    public void setListVisibility(Integer listVisibility) {
        this.listVisibility.set(listVisibility);
    }

    public ObservableInt getIsVisibleProgress() {
        return isVisibleProgress;
    }

    public void setIsVisibleProgress(Integer isVisibleProgress) {
        this.isVisibleProgress.set(isVisibleProgress);
    }

    public ObservableInt getNewMessageVis() {
        return newMessageVis;
    }

    public void setNewMessageVis(int newMessageVis) {
        this.newMessageVis.set(newMessageVis);
    }
}
