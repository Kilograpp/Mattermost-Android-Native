package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.Preference.PreferenceRepository;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelByHadleSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icepick.State;
import io.realm.RealmList;
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
    private static final int REQUEST_GET_DIRECT_USERS = 2;

    private int mOffset;
    private int mLimit;

    @State
    String name;
    @State
    ExtraInfo defaultChannelInfo;
    @State
    String id;
    @State
    String currentUserId;
    @State
    String teamId;
    @State
    User directUsers;

    private List<Preferences> preferenceList = new ArrayList<>();
    private List<User> thisTeamDirects = new RealmList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        currentUserId = MattermostPreference.getInstance().getMyUserId();
        teamId = MattermostPreference.getInstance().getTeamId();
        mOffset = 0;
        mLimit = 100;
        initRequests();
    }

    public Observable<Channel> addParticipantRx(String name) {
        User user = new User();
        user.setId(name);
        return ServerMethod.getInstance()
                .createDirect(MattermostPreference.getInstance().getTeamId(), user);
    }

    private void initRequests() {
        restartableFirst(REQUEST_SAVE_PREFERENCES, () ->
                        ServerMethod.getInstance()
                                .save(preferenceList)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (wholeDirectListActivity, aBoolean) -> {
                    PreferenceRepository.update(preferenceList);
                    sendChanges(aBoolean);
                }, (wholeDirectListActivity, throwable) -> {
                    sendShowError(parceError(throwable, SAVE_PREFERENCES));
                    requestSave(false);
                }
        );

        restartableFirst(REQUEST_GET_DIRECT_USERS, () ->
                        ServerMethod.getInstance()
                                .getAllUsers(teamId, mOffset, mLimit)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (wholeDirectListActivity, stringUserMap) -> {
                    thisTeamDirects.addAll(stringUserMap.values());
                    if (stringUserMap.keySet().size() == 100) {
                        this.mOffset += 100;
                        requestGetDirectUsers();
                    }
                    sendUpdateDataList(thisTeamDirects);
                }, (wholeDirectListActivity, throwable) -> sendShowError(parceError(throwable, null)));
    }


    private void sendChanges(boolean aBoolean) {
        Iterable<Observable<LogoutData>> list = new ArrayList<>();
        for (Preferences preferences : preferenceList) {
            if (PreferenceRepository.query(
                    new PreferenceRepository
                            .PreferenceByNameSpecification(preferences.getName())).size() > 0)
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

    public void requestSavePreferences(Map<String, Boolean> chooseUser) {
        for (Map.Entry<String, Boolean> user : chooseUser.entrySet()) {
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

    public void requestGetDirectUsers() {
        start(REQUEST_GET_DIRECT_USERS);
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
        createTemplateObservable(thisTeamDirects)
                .subscribe(split((wholeDirectListActivity, users) -> {
                    List<User> findedUsers = new ArrayList<>();
                    if (name == null || name.equals("")) {
                        findedUsers = Stream.of(users)
                                .filter(value -> !value.getId().equals(currentUserId))
                                .sorted((o1, o2) -> o1.getUsername().compareTo(o2.getUsername()))
                                .collect(Collectors.toList());
                        wholeDirectListActivity.updateDataList(findedUsers);
                    } else {
                        findedUsers = Stream.of(users)
                                .filter(value -> (!value.getId().equals(currentUserId)
                                        && (value.getUsername().contains(name.toLowerCase())
                                        || value.getFirstName().contains(name.substring(0, 1).toUpperCase() + name.substring(1))
                                        || value.getLastName().contains(name.substring(0, 1).toUpperCase() + name.substring(1)))))
                                .sorted((o1, o2) -> o1.getUsername().compareTo(o2.getUsername()))
                                .collect(Collectors.toList());
                        if(findedUsers.size()!=0){
                            wholeDirectListActivity.updateDataList(findedUsers);
                        }
                    }

                }));
    }

    private void sendShowError(String error) {
        createTemplateObservable(error).subscribe(split(BaseActivity::showErrorText));
    }

    private void sendUpdateDataList(List<User> directUsers) {
        createTemplateObservable(directUsers).subscribe(split((WholeDirectListActivity::updateDataList)));
    }
}