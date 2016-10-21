package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 14.09.16.
 */
public class WholeDirectListPresenter extends BaseRxPresenter<WholeDirectListActivity> {
    private static final String TAG = "WholeDirListPresenter";
    private static final int REQUEST_PROFILE_DM = 1;

    private ApiMethod service;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        restartableFirst(REQUEST_PROFILE_DM,
                () ->
                        service.getProfilesForDMList(MattermostPreference.getInstance().getTeamId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                , (wholeDirectListActivity, stringUserMap) -> {
                    Realm.getDefaultInstance().executeTransaction(realm1 -> {
                        List<User> users = new ArrayList<>();
                        for (User user : stringUserMap.values()) {
                            if (user.getEmail() != null) {
                                users.add(user);
                            }
                        }

                        realm1.insertOrUpdate(users);
                    });
                    sendSetRecyclerView();
                }, (wholeDirectListActivity1, throwable) -> {
                    Log.d(TAG, "Error load profiles for direct messages list");
                    throwable.printStackTrace();
                });

    }

    private void sendSetRecyclerView(){
        createTemplateObservable(new Object())
                .subscribe(split((wholeDirectListActivity, o) -> wholeDirectListActivity.setRecycleView()));
    }

    public void getProfilesForDirectMessage() {
        start(REQUEST_PROFILE_DM);
    }

}