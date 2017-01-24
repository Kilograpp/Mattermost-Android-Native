package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Patterns;

import com.kilogramm.mattermost.BuildConfig;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.ClientCfg;
import com.kilogramm.mattermost.network.ServerMethod;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmResults;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 03.10.2016.
 */
public class MainRxPresenter extends BaseRxPresenter<MainRxActivity> {
    private final String URI_STRING = "https://mattermost.kilograpp.com";
    private final String URL_NOT_VALID = "Url is not valid https://";

    private static final int REQUEST_CHECK = 1;

    private static Pattern mPatternUrl = Patterns.WEB_URL;

    private MattermostApp mMattermostApp;

    @State
    String url;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mMattermostApp = MattermostApp.getSingleton();

        restartableFirst(REQUEST_CHECK, () -> {
            sendVisibleProgress(true);
            return ServerMethod.getInstance()
                    .initLoad()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io());
        }, (mainActivity, initObject) -> {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(realm1 -> {
                RealmResults<ClientCfg> results = realm.where(ClientCfg.class).findAll();
                results.deleteAllFromRealm();
                realm.copyToRealmOrUpdate(initObject.getClientCfg());
            });
            realm.close();
            sendVisibleProgress(false);
            sendShowLoginActivity();
        }, (mainActivity, throwable) -> {
            sendVisibleProgress(false);
            sendShowError(getError(throwable));
        });
    }

    @Override
    protected void onTakeView(MainRxActivity mainRxActivity) {
        super.onTakeView(mainRxActivity);

        if (MattermostPreference.getInstance().getAuthToken() != null &&
                MattermostPreference.getInstance().getCookies() != null) {
            sendShowChatActivity();
        }

        if (BuildConfig.DEBUG && getView().getStringUrl().length() == 0) {
            sendSetUrlString(URI_STRING);
        }
    }

    public boolean isValidUrl(String url) {
        Matcher m = mPatternUrl.matcher(url);
        return m.matches();
    }

    void request(String url) {
        String authorisedUri = URI.create(url).getAuthority();
        if (authorisedUri == null) {
            authorisedUri = url;
        }

        //TODO FIX logic
        MattermostPreference.getInstance().setBaseUrl(authorisedUri);
        mMattermostApp.refreshMattermostRetrofitService();

        try {
            mMattermostApp.getMattermostRetrofitService();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            sendShowError(URL_NOT_VALID);
            return;
        }

        if (isNetworkAvailable()) {
            start(REQUEST_CHECK);
        } else {
            sendShowError(parceError(null, NO_NETWORK));
        }
    }

    // to view methods
    private void sendVisibleProgress(Boolean visibility) {
        createTemplateObservable(visibility)
                .subscribe(split(MainRxActivity::setShowProgress));
    }

    private void sendShowLoginActivity() {
        createTemplateObservable(new Object())
                .subscribe(split((mainRxActivity, o) -> mainRxActivity.showLoginActivity()));
    }

    private void sendShowError(String error) {
        createTemplateObservable(error)
                .subscribe(split(MainRxActivity::showErrorText));
    }

    private void sendShowChatActivity() {
        createTemplateObservable(new Object())
                .subscribe(split((mainRxActivity, o) -> mainRxActivity.showChatActivity()));

    }

    private void sendSetUrlString(String uri) {
        createTemplateObservable(uri)
                .subscribe(split((mainRxActivity, s) -> mainRxActivity.setTextUrl(uri)));
    }
}
