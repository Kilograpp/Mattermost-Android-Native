package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.channel.AddMembersActivity;

import icepick.State;
import io.realm.RealmResults;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class AddMembersPresenter extends BaseRxPresenter<AddMembersActivity> {
    private static final int REQUEST_DB_GETUSERS = 1;
    private static final int REQUEST_ADD_MEMBERS = 2;

    @State
    ExtraInfo mExtraInfo;
    @State
    String mId;

    private String mUser_id;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        initGetUsers();
        addMembers();
    }

    public void initPresenter(String id) {
        this.mId = id;
        start(REQUEST_DB_GETUSERS);
    }

    public void addMember(String id) {
        this.mUser_id = id;
        start(REQUEST_ADD_MEMBERS);
    }

    public RealmResults<User> getMembers(String name) {
        return UserRepository
                .query(new UserRepository.UserByNotIdsSpecification(
                        mExtraInfo.getMembers(), name));
    }

    public RealmResults<User> getMembers() {
        return UserRepository
                .query(new UserRepository.UserByNotIdsSpecification(
                        mExtraInfo.getMembers(), null));
    }


    public class Members {
        @SerializedName("mUser_id")
        @Expose
        String user_id;

        public String getUser_id() {
            return user_id;
        }

        public Members(String user_id) {
            this.user_id = user_id;
        }
    }

    private void initGetUsers() {
        restartableFirst(REQUEST_DB_GETUSERS,
                () -> ExtroInfoRepository.query(
                        new ExtroInfoRepository.ExtroInfoByIdSpecification(mId)).asObservable(),
                (addMembersActivity, o) -> {
                    this.mExtraInfo = o.first();
                    addMembersActivity.updateDataList(
                            UserRepository
                                    .query(new UserRepository.UserByNotIdsSpecification(
                                            mExtraInfo.getMembers(), null)));
                });
    }

    private void addMembers() {
        restartableFirst(REQUEST_ADD_MEMBERS,
                () -> ServerMethod.getInstance().addMember(MattermostPreference.getInstance().getTeamId(),
                        mId, new Members(mUser_id))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (addMembersActivity, user) -> updateMembers(user.getUser_id()),
                (generalRxActivity1, throwable) -> {
                    throwable.printStackTrace();
                    errorUpdateMembers(throwable.getMessage());
                });
    }

    private void updateMembers(String id) {
        createTemplateObservable(new Object())
                .subscribe(split((addMembersActivity, openChatObject) -> {
                    ExtroInfoRepository.updateMembers(mExtraInfo, UserRepository.query(
                            new UserRepository.UserByIdSpecification(id))
                            .first());
                    addMembersActivity.requestMember("User added");
                }));
    }

    private void errorUpdateMembers(String s) {
        createTemplateObservable(new Object())
                .subscribe(split((addMembersActivity, openChatObject)
                        -> addMembersActivity.requestMember(s)));
    }

}
