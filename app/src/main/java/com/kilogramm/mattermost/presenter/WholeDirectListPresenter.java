package com.kilogramm.mattermost.presenter;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;

import java.util.ArrayList;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import nucleus.presenter.Presenter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 14.09.16.
 */
public class WholeDirectListPresenter extends Presenter<WholeDirectListActivity> {
    private static final String TAG = "WholeDirListPresenter";

    private MattermostApp mMattermostApp;
    private Subscription mSubscription;
    private ApiMethod service;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();

    }

    @Override
    protected void onTakeView(WholeDirectListActivity wholeDirectListActivity) {
        super.onTakeView(wholeDirectListActivity);
    }

    public void getProfilesForDirectMessage() {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();

        Realm realm = Realm.getDefaultInstance();
        Team team = realm.where(Team.class).findFirst();
        RealmList<User> users = new RealmList<>();

//        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
        mSubscription = service.getProfilesForDMList(team.getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, User>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete load profiles for direct messages list");
                        getView().setRecycleView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Error load profiles for direct messages list");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Map<String, User> stringUserMap) {
                        Log.d(TAG, "onNext success");
                        realm.executeTransaction(realm1 -> {
                            for (User user : stringUserMap.values()) {
                                users.add(user);
                            }
                            realm1.insertOrUpdate(users);
                            realm1.close();
                        });
                    }
                });
    }

    public ArrayList<String> getUsersStatuses(ArrayList<String> usersIds) {
        ArrayList<String> userStatus = new ArrayList<>();

        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();

//        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
        mSubscription = service.getStatus(usersIds)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, String>>() {
                    @Override
                    public void onCompleted() {
//                        notify();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Map<String, String> stringStringMap) {
                        userStatus.addAll(stringStringMap.values());
                    }
                });

        return userStatus;
    }

    public Drawable drawStatusIcon(String status) {
        return getView().getStatusDrawable(status);
    }

    public String imageUrl(String userId){
        return getView().getImageUrl(userId);
    }

    //========================queries for Direct Message==================================
//    public String postUpdatelastViewdAt(){
//        String channelId;
//
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//
//        service.updatelastViewedAt(String yourId, String channelId)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//
//                    }
//                });
//
//        return
//    }
}
