package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.ChannelByHadleSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;

import icepick.State;
import io.realm.Sort;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 14.09.16.
 */
public class WholeDirectListPresenter extends BaseRxPresenter<WholeDirectListActivity> {
    private static final String TAG = "WholeDirListPresenter";
    private static final int REQUEST_PROFILE_DM = 1;
    private static final int REQUEST_DB_USERS = 2;

    private ApiMethod service;
    @State
    String name;

    @State
    ExtraInfo defaultChannel;

    @State
    String id;
    @State
    String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        currentUserId = MattermostPreference.getInstance().getMyUserId();
        initGetUsers();
    }


    private void initGetUsers() {

        restartableFirst(REQUEST_PROFILE_DM,
                () -> service.getProfilesForDMList(MattermostPreference.getInstance().getTeamId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                , (wholeDirectListActivity, stringUserMap) -> {
                    UserRepository.add(stringUserMap.values());
                    sendSetRecyclerView();
                }, (wholeDirectListActivity1, throwable) -> {
                    Log.d(TAG, "Error load profiles for direct messages list");
                    throwable.printStackTrace();
                });

    }

    private void sendSetRecyclerView() {
        createTemplateObservable(new Object())
                .subscribe(split((wholeDirectListActivity, o) -> {
                            this.defaultChannel = ExtroInfoRepository.query(
                                    new ExtroInfoRepository.ExtroInfoByIdSpecification(id)).first();
                            wholeDirectListActivity.updateDataList(defaultChannel
                                    .getMembers().where().notEqualTo("id", currentUserId).findAllSorted("username", Sort.ASCENDING));
                        }
                ));
    }


    public void getUsers() {
        this.id = ChannelRepository.query(new ChannelByHadleSpecification("town-square")).first().getId();
        start(REQUEST_PROFILE_DM);
    }

    public void getSearchUsers(String name) {
        this.name = name;
        createTemplateObservable(defaultChannel.getMembers())
                .subscribe(split((wholeDirectListActivity, users) -> {
                    if (name == null)
                        wholeDirectListActivity.updateDataList(
                                users.where()
                                        .notEqualTo("id", currentUserId)
                                        .findAllSorted("username", Sort.ASCENDING));
                    else
                        wholeDirectListActivity.updateDataList(
                                users.where()
                                        .notEqualTo("id", currentUserId)
                                        .contains("username", name.toLowerCase())
                                        .or()
                                        .notEqualTo("id", currentUserId)
                                        .contains("firstName", name.substring(0, 1).toUpperCase()
                                                + name.substring(1))
                                        .or()
                                        .notEqualTo("id", currentUserId)
                                        .contains("lastName", name.substring(0, 1).toUpperCase()
                                                + name.substring(1))
                                        .findAllSorted("username", Sort.ASCENDING));
                }));
    }

}