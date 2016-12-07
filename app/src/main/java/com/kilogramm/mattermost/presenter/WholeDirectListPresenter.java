package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.Preference.PreferenceRepository;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelByHadleSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icepick.State;
import io.realm.Sort;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 14.09.16.
 */
public class WholeDirectListPresenter extends BaseRxPresenter<WholeDirectListActivity> {
    private static final String TAG = "WholeDirListPresenter";
    private static final int REQUEST_SAVE_PREFERENCES = 1;

    private ApiMethod service;
    @State
    String name;
    @State
    ExtraInfo defaultChannelInfo;
    @State
    String id;
    @State
    String currentUserId;

    List<Preferences> preferenceList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        currentUserId = MattermostPreference.getInstance().getMyUserId();
        initGetUsers();
    }


    public Observable<Channel> addParticipantRx(String name) {
        LogoutData logoutData = new LogoutData(name);
        return service.createDirect(MattermostPreference.getInstance().getTeamId(), logoutData);

    }

    private void sendChanges(boolean aBoolean) {
        Iterable<Observable<LogoutData>> list = new ArrayList<>();
        for (Preferences preferences : preferenceList) {
            if (PreferenceRepository
                    .query(new PreferenceRepository
                            .PreferenceByNameSpecification(preferences.getName()))
                    .size() > 0)
                ((ArrayList) list).add(addParticipantRx(preferences.getName()));
        }

        Observable.zip(list, args -> {
            List<Channel> jsonObjects = new ArrayList<>();
            for (Object arg : args) {
                jsonObjects.add((Channel) arg);
                Log.d(TAG, "sendChanges: arg: " + arg);
            }
            return jsonObjects;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(channels -> {
                    ChannelRepository.prepareDirectAndChannelAdd(channels);
                    requestSave(aBoolean);
                }, throwable -> {
                    throwable.printStackTrace();
                    requestSave(aBoolean);
                });
    }

    private void initGetUsers() {
        restartableFirst(REQUEST_SAVE_PREFERENCES, () ->
                        service.save(preferenceList)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (wholeDirectListActivity, aBoolean) -> {
                    PreferenceRepository.update(preferenceList);
                    sendChanges(aBoolean);
                }, (wholeDirectListActivity, throwable) -> {
                    throwable.printStackTrace();
                    requestSave(false);
                }
        );
    }


    public void savePreferences(Map<String, Boolean> chouseUser) {
        for (Map.Entry<String, Boolean> user : chouseUser.entrySet()) {
            preferenceList.add(new Preferences(
                    user.getKey(),
                    currentUserId,
                    user.getValue(),
                    "direct_channel_show"));
        }
        start(REQUEST_SAVE_PREFERENCES);
    }

    private void requestSave(boolean isSuccess) {
        createTemplateObservable(isSuccess).subscribe(split((wholeDirectListActivity, aBoolean) -> {
            if (aBoolean) {
                Toast.makeText(wholeDirectListActivity, createToast(), Toast.LENGTH_SHORT).show();
                wholeDirectListActivity.onBackPressed();
            } else
                wholeDirectListActivity.failSave();
        }));
    }

    private String createToast() {
        int countAdd = 0;
        int countDelete = 0;
        if (preferenceList.size() == 1) {
            return String.format("%s %s %s", "Conversation with",
                    UserRepository.query(
                            new UserRepository.UserByIdSpecification(preferenceList.get(0).getName()))
                            .first()
                            .getUsername(),
                    preferenceList.get(0).getValue().equals("true") ? "added" : "removed");
        }
        for (Preferences preference : preferenceList) {
            if (preference.getValue().equals("true"))
                countAdd++;
            else
                countDelete++;
        }
        return String.format("%s%s",
                countAdd > 0 ? countAdd + " conversations have been added\n" : "",
                countDelete > 0 ? countDelete + " conversations have been removed" : "");
    }

    public void getUsers() {
        this.id = ChannelRepository.query(new ChannelByHadleSpecification("town-square")).first().getId();
        createTemplateObservable(new Object())
                .subscribe(split((wholeDirectListActivity, o) -> {
                            this.defaultChannelInfo = ExtroInfoRepository.query(
                                    new ExtroInfoRepository.ExtroInfoByIdSpecification(id)).first();
                            wholeDirectListActivity.updateDataList(defaultChannelInfo
                                    .getMembers().where().isNotNull("id").notEqualTo("id", currentUserId).findAllSorted("username", Sort.ASCENDING));
                        }
                ));
    }

    public void getSearchUsers(String name) {
        this.name = name;
        createTemplateObservable(defaultChannelInfo.getMembers())
                .subscribe(split((wholeDirectListActivity, users) -> {
                    if (name == null)
                        wholeDirectListActivity.updateDataList(
                                users.where()
                                        .isNotNull("id")
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