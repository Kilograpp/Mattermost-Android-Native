package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.channel.AddMembersActivity;

import java.util.ArrayList;
import java.util.List;

import icepick.State;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class AddMembersPresenter extends BaseRxPresenter<AddMembersActivity> {
    private static final int REQUEST_GET_USERS = 1;
    private static final int REQUEST_ADD_MEMBERS = 2;
    private static final int REQUEST_GET_EXTRA_INFO = 3;

    @State
    ExtraInfo mExtraInfo;
    @State
    String mId;

    private String mTeamId;
    private String mUser_id;
    private String mChannelId;
    private int mOffset;
    private int mLimit;

    List<User> usersNotInChannel = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mTeamId = MattermostPreference.getInstance().getTeamId();
        mId = MattermostPreference.getInstance().getMyUserId();

        mOffset = 0;
        mLimit = 100;

        initRequests();
    }

    public void initPresenter(String channelId) {
        this.mChannelId = channelId;
        start(REQUEST_GET_EXTRA_INFO);
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

    private void initRequests() {
        getExtraInfo();
        requestGetUsersNotInChannel();
        addMembers();
    }

    private void getExtraInfo() {
        restartableFirst(REQUEST_GET_EXTRA_INFO,
                () -> ServerMethod.getInstance()
                        .extraInfoChannel(mTeamId, mChannelId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                , (channelActivity, extraInfoWithOutMember) -> {
                    RealmList<User> results = new RealmList<>();
                    results.addAll(UserRepository.query(
                            new UserRepository.UserByIdsSpecification(
                                    Stream.of(extraInfoWithOutMember.getMembers().values())
                                            .collect(Collectors.toList())
                            )
                    ));
                    extraInfoWithOutMember.getExtraInfo().setMembers(results);
                    ExtroInfoRepository.add(extraInfoWithOutMember.getExtraInfo());
//                    getUsers(extraInfoWithOutMember.getExtraInfo().getMembers());
                    start(REQUEST_GET_USERS);
                }, (channelActivity, throwable) -> sendShowError(throwable.getMessage()));
    }

//    private void getUsers(RealmList<User> members) {
//        createTemplateObservable(members)
//                .subscribe(split((addMembersActivity, usersNotInChannel) -> addMembersActivity
//                        .updateDataList(UserRepository.query(new UserRepository.UserByNotIdsSpecification(
//                                usersNotInChannel, null)))));
//    }

    private void requestGetUsersNotInChannel() {
        restartableFirst(REQUEST_GET_USERS,
                () -> ServerMethod.getInstance()
                        .getUsersNotInChannel(mTeamId, mChannelId, mOffset, mLimit)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                , (addMembersActivity, stringUserMap) -> {
                    if (stringUserMap.keySet().size() == 100) {
                        this.mOffset += 1;
                        start(REQUEST_GET_USERS);
                    } else {
                        usersNotInChannel.addAll(stringUserMap.values());
                        addMembersActivity.refreshAdapter(usersNotInChannel);
//                        UserRepository.add(usersNotInChannel);
                    }
                }, (addMembersActivity, throwable) -> sendShowError(throwable.getMessage()));
    }

    public ArrayList<User> getUsersNotInChannel() {
        return (ArrayList)usersNotInChannel;
    }

    private void addMembers() {
        restartableFirst(REQUEST_ADD_MEMBERS, () ->
                        ServerMethod.getInstance()
                                .addMember(mTeamId, mId, new Members(mUser_id))
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

    private void sendShowError(String error) {
        createTemplateObservable(error).subscribe(split(BaseActivity::showErrorText));
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
