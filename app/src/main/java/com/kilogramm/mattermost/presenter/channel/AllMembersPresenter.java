package com.kilogramm.mattermost.presenter.channel;

import android.os.Bundle;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.ListPreferences;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.channel.AllMembersActivity;

import icepick.State;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class AllMembersPresenter extends BaseRxPresenter<AllMembersActivity> {
    private static final int REQUEST_DB_GETUSERS = 1;
    private static final int REQUEST_SAVE = 2;
    @State
    ExtraInfo extraInfo;
    @State
    String id;

    private ApiMethod service;
    private LogoutData user;

    @State
    ListPreferences listPreferences = new ListPreferences();

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp application = MattermostApp.getSingleton();
        user = new LogoutData();
        service = application.getMattermostRetrofitService();

        initGetUsers();
        initSaveRequest();
    }

    private void initGetUsers() {
        restartableFirst(REQUEST_DB_GETUSERS,
                () -> ExtroInfoRepository.query(new ExtroInfoRepository.ExtroInfoByIdSpecification(id)).asObservable(),
                (allMembersActivity, o) -> {
                    this.extraInfo = o.first();
                    allMembersActivity.updateDataList(getMembers());
                },(allMembersActivity, throwable) -> sendShowError(parceError(throwable, SAVE_PREFERENCES)));
    }

    private void initSaveRequest() {
        restartableFirst(REQUEST_SAVE, () -> Observable.defer(
                () -> Observable.zip(
                        service.save(listPreferences.getmSaveData())
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                        service.createDirect(MattermostPreference.getInstance().getTeamId(), user)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                        (aBoolean, channel) -> {
                            if (aBoolean == Boolean.FALSE) {
                                return null;
                            }
                            ChannelRepository.prepareDirectChannelAndAdd(channel, user.getUserId());
                            return channel;
                        })), (generalRxActivity, channel) -> {
            listPreferences.getmSaveData().clear();
            MattermostPreference.getInstance().setLastChannelId(channel.getId());
            sendSetFragmentChat();
        }, (generalRxActivity, throwable) -> sendShowError(parceError(throwable, null)));
    }

    public void requestSaveData(Preferences data, String userId) {
        listPreferences.getmSaveData().clear();
        listPreferences.getmSaveData().add(data);
        user.setUserId(userId);
        start(REQUEST_SAVE);
    }

    private void sendSetFragmentChat() {
        createTemplateObservable(new Object())
                .subscribe(split((allMembersActivity, o) ->
                        allMembersActivity.startGeneralActivity()));
    }

    private void sendShowError(String error) {
        createTemplateObservable(error).subscribe(split(BaseActivity::showErrorText));
    }

    public void initPresenter(String id) {
        this.id = id;
        start(REQUEST_DB_GETUSERS);
    }

    public RealmResults<User> getMembers(String name) {
        return extraInfo.getMembers().where().contains("username", name).findAllSorted("username", Sort.ASCENDING);
    }

    public RealmResults<User> getMembers() {
        return extraInfo.getMembers().where().findAllSorted("username", Sort.ASCENDING);
    }
}
