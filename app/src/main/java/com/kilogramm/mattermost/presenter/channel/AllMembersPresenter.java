package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;

import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.channel.AllMembersActivity;

import icepick.State;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by ngers on 01.11.16.
 */

public class AllMembersPresenter extends BaseRxPresenter<AllMembersActivity> {
    private static final int REQUEST_DB_GETUSERS = 1;

    @State
    ExtraInfo extraInfo;
    @State
    String id;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        initGetUsers();
    }


    private void initGetUsers() {
        restartableFirst(REQUEST_DB_GETUSERS,
                () -> ExtroInfoRepository.query(new ExtroInfoRepository.ExtroInfoByIdSpecification(id)).asObservable(),
                (allMembersActivity, o) -> {
                    this.extraInfo = o.first();
                    allMembersActivity.updateDataList(extraInfo.getMembers());
                });
    }

    public void initPresenter(String id) {
        this.id = id;
        start(REQUEST_DB_GETUSERS);
    }

    public RealmResults<User> getMembers(String name) {
        return extraInfo.getMembers().where().contains("username", name).findAll();
    }

    public RealmList<User> getMembers() {
        return extraInfo.getMembers();
    }
}
