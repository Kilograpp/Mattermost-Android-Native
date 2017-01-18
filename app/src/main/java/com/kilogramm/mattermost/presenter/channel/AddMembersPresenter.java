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
import io.realm.Realm;
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
    private static final String TAG = AddMembersPresenter.class.getSimpleName();

//    @State
//    ExtraInfo mExtraInfo;
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

//    public RealmResults<User> getMembers(String name) {
//        ExtraInfo mExtraInfo = ExtroInfoRepository.query(
//                new ExtroInfoRepository.ExtroInfoByIdSpecification(mChannelId)).first();
//        RealmResults<User> members = UserRepository.query(new UserRepository.UserByNotIdsSpecification(mExtraInfo.getMembers(), name));
//        return members;
//    }

    public void getFoundUsers(String name){
        createTemplateObservable(usersNotInChannel)
                .subscribe(split((addMembersActivity, members) -> {
                    List<User> findedUsers = new ArrayList<>();
                    if (name == null || name.equals("")) {
                        findedUsers = Stream.of(members)
                                .filter(value -> !value.getId().equals(mId))
                                .sorted((o1, o2) -> o1.getUsername().compareTo(o2.getUsername()))
                                .collect(Collectors.toList());

                        addMembersActivity.refreshAdapter(findedUsers);
                    } else {
                        findedUsers = Stream.of(members)
                                .filter(value -> (!value.getId().equals(mId)
                                        && (value.getUsername().contains(name.toLowerCase())
                                        || value.getFirstName().contains(name.substring(0, 1).toUpperCase() + name.substring(1))
                                        || value.getLastName().contains(name.substring(0, 1).toUpperCase() + name.substring(1)))))
                                .sorted((o1, o2) -> o1.getUsername().compareTo(o2.getUsername()))
                                .collect(Collectors.toList());
                            addMembersActivity.refreshAdapter(findedUsers);

                    }

                }));
    }

    public RealmResults<User> getMembers() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<User> members = UserRepository.query(
//                new UserRepository.UserByNotIdsSpecification(mExtraInfo.getMembers(), null));
//                new UserRepository.UserByNotIdsSpecification(realm.where(ExtraInfo.class).contains("id", mChannelId).findFirst().getMembers(), null));
                new UserRepository.UserByNotIdsSpecification(realm.where(ExtraInfo.class).contains("id", mChannelId).findFirst().getMembers(), null));
        return members;
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
                    start(REQUEST_GET_USERS);
                }, (channelActivity, throwable) -> sendShowError(parceError(throwable, null)));
    }

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
                        usersNotInChannel.clear();
                        usersNotInChannel.addAll(stringUserMap.values());
                        addMembersActivity.refreshAdapter(usersNotInChannel);
                    }
                }, (addMembersActivity, throwable) -> sendShowError(parceError(throwable, null)));
    }

    private void addMembers() {
        restartableFirst(REQUEST_ADD_MEMBERS, () ->
                        ServerMethod.getInstance()
                                .addMember(mTeamId, mChannelId, new Members(mUser_id))
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (addMembersActivity, user) -> updateMembers(user.getUser_id()),
                (generalRxActivity1, throwable) -> {
                    throwable.printStackTrace();
                    errorUpdateMembers(parceError(throwable, null));
                });
    }

    private void updateMembers(String id) {
        createTemplateObservable(new Object()).subscribe(split((addMembersActivity, openChatObject) -> {
            ExtroInfoRepository.updateMembers(ExtroInfoRepository.query(
                    new ExtroInfoRepository.ExtroInfoByIdSpecification(mChannelId)).first(),
                    UserRepository.query(new UserRepository.UserByIdSpecification(id)).first());
            start(REQUEST_GET_USERS);
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
