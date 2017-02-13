package com.kilogramm.mattermost.presenter.channel;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.gson.JsonSyntaxException;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.ListPreferences;
import com.kilogramm.mattermost.model.entity.Preference.PreferenceRepository;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.channel.ChannelActivity;

import java.io.IOException;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmList;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 01.11.16.
 */

public class ChannelPresenter extends BaseRxPresenter<ChannelActivity> {
    private static final String TAG = "ChannelPresenter";
    private static final int REQUEST_EXTRA_INFO = 1;
    private static final int REQUEST_LEAVE = 2;
    private static final int REQUEST_SAVE = 3;
    private static final int REQUEST_CHANNEL = 4;
    private static final int REQUEST_DELETE = 5;
    private static final int REQUEST_SAVE_PREFERENCES = 6;

    private String errorLoadingExtraInfo = "Error loading channel info";

    private User mUser = new User();

    @State
    ListPreferences listPreferences = new ListPreferences();
    @State
    String mTeamId;
    @State
    String mChannelId;
    @State
    Channel mChannel;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        initRequests();
    }

    public void initPresenter(String teamId, String channelId) {
        this.mTeamId = teamId;
        this.mChannelId = channelId;
        this.mChannel = ChannelRepository.query(new ChannelRepository.ChannelByIdSpecification(channelId)).first();
    }

    public void requestSaveData(Preferences data, String userId) {
        listPreferences.getmSaveData().clear();
        listPreferences.getmSaveData().add(data);
        mUser.setId(userId);
        start(REQUEST_SAVE);
    }

    public void requestExtraInfo() {
        start(REQUEST_EXTRA_INFO);
    }

    public void requestLeave() {
        start(REQUEST_LEAVE);
    }

    public void requestDelete() {
        start(REQUEST_DELETE);
    }

    public Channel getChannel() {
        return mChannel;
    }

    public int getMemberCount() {
        return Integer.parseInt(ExtroInfoRepository.query(
                new ExtroInfoRepository.ExtroInfoByIdSpecification(mChannelId))
                .first()
                .getMember_count());
    }

    //region Init Requests
    private void initRequests() {
        initExtraInfo();
        leaveChannel();
        deleteChannel();
        initSaveRequest();
    }

    private void initExtraInfo() {
        restartableFirst(REQUEST_EXTRA_INFO,
                () -> ServerMethod.getInstance()
                        .extraInfoChannel(MattermostPreference.getInstance().getTeamId(), mChannelId)
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
                    start(REQUEST_CHANNEL);
                }, (channelActivity, throwable) -> {
                    sendError(parseError(throwable, MattermostApp.getSingleton()
                            .getString(R.string.error_load_channel_info)));
                    sendCloseActivity();
                });

        restartableFirst(REQUEST_CHANNEL,
                () -> ServerMethod.getInstance()
                        .getChannel(this.mTeamId, this.mChannelId)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (channelActivity, channelWithMember) -> {
                    ChannelRepository.update(channelWithMember.getChannel());
                    requestMembers();
                }, (channelActivity, throwable) -> {
                    sendError(parseError(throwable, MattermostApp.getSingleton()
                            .getString(R.string.error_load_channel_info)));
                    sendCloseActivity();
                }
        );
    }

    private void leaveChannel() {
        restartableFirst(REQUEST_LEAVE,
                () -> ServerMethod.getInstance()
                        .leaveChannel(this.mTeamId, this.mChannelId)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (channelActivity, channel) -> requestFinish("leaved"),
                (channelActivity, throwable) -> sendError(parseError(throwable, null)));
    }

    private void deleteChannel() {
        restartableFirst(REQUEST_DELETE,
                () -> ServerMethod.getInstance()
                        .deleteChannel(this.mTeamId, this.mChannelId)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (channelActivity, channel) -> requestFinish("deleted"),
                (channelActivity, throwable) -> sendError(parseError(throwable, null)));
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

    private void initSaveRequest() {
        restartableFirst(REQUEST_SAVE,
                () -> ServerMethod.getInstance()
                        .saveOrCreateDirectChannel(listPreferences.getmSaveData(),
                                MattermostPreference.getInstance().getTeamId(), mUser.getId())
                , (generalRxActivity, channel) -> {
                    ChannelRepository.prepareDirectChannelAndAdd(channel, mUser.getId());
                    listPreferences.getmSaveData().clear();
                    MattermostPreference.getInstance().setLastChannelId(channel.getId());
                    sendSetFragmentChat();
                }, (generalRxActivity, throwable) -> sendShowError(parseError(throwable)));

        restartableFirst(REQUEST_SAVE_PREFERENCES, () ->
                        ServerMethod.getInstance()
                                .save(Realm.getDefaultInstance().copyFromRealm(PreferenceRepository.query()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (wholeDirectListActivity, aBoolean) -> {
                },
                (wholeDirectListActivity, throwable) ->
                        sendShowError(parseError(throwable))
        );
    }

    public void savePreferences() {
        start(REQUEST_SAVE_PREFERENCES);
    }

    private void sendSetFragmentChat() {
        createTemplateObservable(new Object())
                .subscribe(split((channelActivity, o) -> channelActivity.startGeneralActivity()));
    }

    private void requestMembers() {
        createTemplateObservable(new Object()).subscribe(split((channelActivity, o) ->
                channelActivity.initiationData(ExtroInfoRepository.query(
                        new ExtroInfoRepository.ExtroInfoByIdSpecification(mChannelId)).first())));
    }

    private void requestFinish(String s) {
        createTemplateObservable(new Object()).subscribe(split((channelActivity, o) -> {
            Toast.makeText(channelActivity,
                    String.format("You've %s %s %s",
                            s,
                            this.mChannel.getDisplayName(),
                            mChannel.getType().equals(Channel.OPEN) ? "channel" : "private group"),
                    Toast.LENGTH_SHORT).show();
            ChannelRepository.remove(mChannel);
//            channelActivity.finishActivity();
            channelActivity.onBackPressed();
        }));
    }

    private void sendError(String error) {
        createTemplateObservable(error).subscribe(split((channelActivity, s) -> {
            Toast.makeText(channelActivity, s, Toast.LENGTH_SHORT).show();
            channelActivity.errorRequest();
        }));
    }

    private void sendCloseActivity() {
        createTemplateObservable(new Object())
                .subscribe(split((channelActivity, o) -> channelActivity.finish()));
    }

    private void sendShowError(String error) {
        createTemplateObservable(error).subscribe(split(BaseActivity::showErrorText));
    }
}
