package com.kilogramm.mattermost.presenter.settings;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.NotifyProps;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.settings.NotificationActivity;

import icepick.State;
import io.realm.Realm;
import nucleus.presenter.delivery.Delivery;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 18.10.16.
 */

public class NotificationPresenter extends BaseRxPresenter<NotificationActivity> {

    private static final String channelMentions = "\"@channel\",\"@all\"";

    private static final String TAG = "NotificationPresenter";
    private static final int REQUEST_UPDATE_NOTIFY = 1;

    @State
    NotifyProps notifyProps;

    private ApiMethod service;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        Realm realm = Realm.getDefaultInstance();
        this.notifyProps = realm.where(NotifyProps.class).findFirst();
        realm.close();
        service = mMattermostApp.getMattermostRetrofitService();
        userRepository = new UserRepository();
        initRequests();
    }

    private void initRequests() {
        initDeletePost();
    }

    private void initDeletePost() {
        restartableFirst(REQUEST_UPDATE_NOTIFY, () ->
                        service.updateNotify(notifyProps)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread()),
                (settingsActivity, user) ->
                        userRepository.update(user),
                (settingsActivity, throwable) -> {
                    createTemplateObservable(throwable.getMessage()).subscribe(split((chatRxFragment, s) ->
                            Toast.makeText(settingsActivity, s, Toast.LENGTH_SHORT).show()));
                    Log.d(TAG, "Error update notification " + throwable.getMessage());
                });
    }

    public void requestUpdateNotify() {
        start(REQUEST_UPDATE_NOTIFY);
    }

    public String getMentions() {
        String result = "";
        if (notifyProps != null) {
            String[] mentions = notifyProps.getMentionKeys().split(",");
            for (String s : mentions) {
                result = result + "\"" + s + "\",";
            }
            if (notifyProps.getChannel().equals("true"))
                return result  + channelMentions;
            return result;
        }
        return null;
    }

    public <T> Observable<Delivery<NotificationActivity, T>> createTemplateObservable(T obj) {
        return Observable.just(obj)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .compose(deliverFirst());
    }
}
