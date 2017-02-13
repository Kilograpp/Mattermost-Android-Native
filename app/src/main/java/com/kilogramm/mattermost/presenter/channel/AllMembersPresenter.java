package com.kilogramm.mattermost.presenter.channel;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.JsonSyntaxException;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.ListPreferences;
import com.kilogramm.mattermost.model.entity.Preference.PreferenceRepository;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.channel.AllMembersChannelActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class AllMembersPresenter extends BaseRxPresenter<AllMembersChannelActivity> {
    private static final int REQUEST_DB_GETUSERS = 1;
    private static final int REQUEST_SAVE = 2;
    private static final int REQUEST_SAVE_PREFERENCES = 3;

    ExtraInfo mExtraInfo;

    String mId;

    private User mUser;
    ListPreferences listPreferences = new ListPreferences();

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mUser = new User();

        initGetUsers();
        initSavPreferences();
        initSaveRequest();
    }

    @Override
    public String parseError(Throwable e) {
        try {
            HttpError httpError = getErrorFromResponse(e);
            Context context = MattermostApp.getSingleton().getApplicationContext();
            switch (httpError.getStatusCode()){
                case 400:
                    return context.getString(R.string.error_save_data);
                case 401:
                    return context.getString(R.string.error_auth);
                case 403:
                    return context.getString(R.string.error_preferences_not_match);
                case 500:
                    return context.getString(R.string.error_no_preferences);
                default:
                    return httpError.getMessage();

            }
        } catch (IOException | JsonSyntaxException e1) {
            e1.printStackTrace();
            return super.parseError(e);
        }
    }

    public void requestSaveData(Preferences data, String userId) {
        listPreferences.getmSaveData().clear();
        listPreferences.getmSaveData().add(data);
        mUser.setId(userId);
        start(REQUEST_SAVE);
    }

    private void sendShowError(String error) {
        createTemplateObservable(error).subscribe(split(BaseActivity::showErrorText));
    }

    public void initPresenter(String id) {
        this.mId = id;
        start(REQUEST_DB_GETUSERS);
    }

    public void savePreferences(String name) {
        Preferences preferences =
                new Preferences(PreferenceRepository.query(
                        new PreferenceRepository.PreferenceByNameSpecification(name)).first());
        preferences.setValue("true");
        listPreferences.getmSaveData().add(preferences);
        start(REQUEST_SAVE_PREFERENCES);
    }

    public RealmResults<User> getMembers(String name) {
        return mExtraInfo.getMembers().where().contains("username", name).findAllSorted("username", Sort.ASCENDING);
    }

    public RealmResults<User> getMembers() {
        return mExtraInfo.getMembers().where().findAllSorted("username", Sort.ASCENDING);
    }

    private void initGetUsers() {
        restartableFirst(REQUEST_DB_GETUSERS,
                () -> ExtroInfoRepository.query(new ExtroInfoRepository.ExtroInfoByIdSpecification(mId)).asObservable(),
                (allMembersActivity, o) -> {
                    this.mExtraInfo = o.first();
                    allMembersActivity.updateDataList(getMembers());
                }, (allMembersActivity, throwable) -> sendShowError(parseError(throwable)));
    }

    private void initSavPreferences() {
        restartableFirst(REQUEST_SAVE_PREFERENCES, () ->
                       ServerMethod.getInstance()
                               .save(listPreferences.getmSaveData())
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (allMembersChannelActivity, aBoolean) -> {
                    PreferenceRepository.update(listPreferences.getmSaveData());
//                    sendSetFragmentChat();
                },
                (allMembersChannelActivity, throwable) ->
                        sendShowError(parseError(throwable))
        );

    }

    /*private void initGetChannels() {
        restartableFirst(REQUEST_GET_CHANNEL, () ->
                mService.getChannelsTeam(MattermostPreference.getInstance().getTeamId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (allMembersChannelActivity, channelsWithMembers) -> {
                   ChannelRepository.add(channelsWithMembers.getChannels());
                    MembersRepository.update(channelsWithMembers.getMembers());
                },
                (allMembersChannelActivity, throwable) ->
                        sendShowError(parceError(throwable, SAVE_PREFERENCES)));
    }*/

    private void initSaveRequest() {
        restartableFirst(REQUEST_SAVE, () ->
                ServerMethod.getInstance()
                        .saveOrCreateDirectChannel(listPreferences.getmSaveData(),
                            MattermostPreference.getInstance().getTeamId(),
                            mUser.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                , (generalRxActivity, channel) -> {
                    ChannelRepository.prepareDirectChannelAndAdd(channel, mUser.getId());
                    PreferenceRepository.update(listPreferences.getmSaveData());
                    listPreferences.getmSaveData().clear();
                    MattermostPreference.getInstance().setLastChannelId(channel.getId());
                    sendSetFragmentChat();
                }, (generalRxActivity, throwable) -> sendShowError(parseError(throwable, null)));
    }

    private void sendSetFragmentChat() {
        createTemplateObservable(new Object())
                .subscribe(split((allMembersActivity, o) ->
                        allMembersActivity.startGeneralActivity()));
    }
}

