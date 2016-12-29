package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.channel.AddMembersActivity;

import icepick.State;
import io.realm.RealmResults;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class AddMembersPresenter extends BaseRxPresenter<AddMembersActivity> {
    private static final int REQUEST_DB_GET_USERS = 1;
    private static final int REQUEST_ADD_MEMBERS = 2;

    @State
    ExtraInfo mExtraInfo;
    @State
    String mId;

    private String mTeamId;

    private ApiMethod mService;
    private String mUser_id;
    private String mChannelId;
    private int mOffset;
    private int mLimit;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp mattermostApp = MattermostApp.getSingleton();
        mService = mattermostApp.getMattermostRetrofitService();
        mTeamId = MattermostPreference.getInstance().getTeamId();

        mOffset = 0;
        mLimit = 100;

        initGetUsers();
        addMembers();
    }

    public void initPresenter(String id) {
        this.mId = id;
        start(REQUEST_DB_GET_USERS);
    }

    public void addMember(String id) {
        this.mUser_id = id;
        start(REQUEST_ADD_MEMBERS);
    }

    public RealmResults<User> getMembers(String name) {
        return UserRepository.query(new UserRepository.UserByNotIdsSpecification(
                mExtraInfo.getMembers(), name));
    }

    public RealmResults<User> getMembers() {
        return UserRepository.query(new UserRepository.UserByNotIdsSpecification(
                mExtraInfo.getMembers(), null));
    }

    private void initGetUsers() {
        restartableFirst(REQUEST_DB_GET_USERS, () ->
                ExtroInfoRepository.query(new ExtroInfoRepository.ExtroInfoByIdSpecification(mId)).asObservable(),
                (addMembersActivity, o) -> {
                    this.mExtraInfo = o.first();
                    addMembersActivity.updateDataList(UserRepository.query(
                            new UserRepository.UserByNotIdsSpecification(
                                    mExtraInfo.getMembers(), null)));
                });

//        restartableFirst(REQUEST_DB_GET_USERS, () ->
//                        mService.getUsersNotInChannel(mTeamId, mChannelId, mOffset, mLimit)
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(Schedulers.io()),
//                (addMembersActivity, users) -> updateMembers(users.getUsers().values()),
//                (addMembersActivity, throwable) -> throwable.printStackTrace());
    }

    private void addMembers() {
        restartableFirst(REQUEST_ADD_MEMBERS, () ->
                        mService.addMember(mTeamId, mId, new Members(mUser_id))
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (addMembersActivity, user) -> updateMembers(user.getUser_id()),
                (generalRxActivity1, throwable) -> {
                    throwable.printStackTrace();
                    errorUpdateMembers(throwable.getMessage());
                });
    }

    private void updateMembers(String id) {
        createTemplateObservable(new Object()).subscribe(split((addMembersActivity, openChatObject) -> {
                    ExtroInfoRepository.updateMembers(mExtraInfo, UserRepository.query(
                            new UserRepository.UserByIdSpecification(id)).first());
                    addMembersActivity.requestMember("User added");
                }));
    }

    private void errorUpdateMembers(String s) {
        createTemplateObservable(new Object())
                .subscribe(split((addMembersActivity, openChatObject) ->
                        addMembersActivity.requestMember(s)));
    }

    public class Members {
        @SerializedName("user_id")
        @Expose
        String user_id;

        public String getUser_id() {
            return user_id;
        }

        public Members(String user_id) {
            this.user_id = user_id;
        }
    }
}
