package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Patterns;

import com.kilogramm.mattermost.BuildConfig;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.ClientCfg;
import com.kilogramm.mattermost.network.ApiMethod;

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
public class MainRxPresenter extends BaseRxPresenter<MainRxAcivity> {

    private static final String TAG = "MainRxPresenter";

    private static final int REQUEST_CHECK = 1;
    private static final int REQUEST_ACTIVITY = 2;
    //TODO pattern url null fix
    private static Pattern mPatternUrl = Patterns.WEB_URL;

    private MattermostApp mMattermostApp;

    @State
    String url;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mMattermostApp = MattermostApp.getSingleton();

        restartableFirst(REQUEST_CHECK, () -> {
            ApiMethod service = mMattermostApp.getMattermostRetrofitService();
            sendVisibleProgress(true);
            return service.initLoad()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io());
        }, (mainActivity, initObject) -> {
            Realm.getDefaultInstance().executeTransaction(realm -> {
                RealmResults<ClientCfg> results = realm.where(ClientCfg.class).findAll();
                results.deleteAllFromRealm();
                realm.copyToRealmOrUpdate(initObject.getClientCfg());
            });

            sendVisibleProgress(false);
            sendShowLoginActivity();
        }, (mainActivity, throwable) -> {
            sendVisibleProgress(false);
            sendShowError(getError(throwable));
        });

    }

    @Override
    protected void onTakeView(MainRxAcivity mainRxAcivity) {
        super.onTakeView(mainRxAcivity);

        //TODO FIX logic check login
        if (MattermostPreference.getInstance().getAuthToken() != null &&
                MattermostPreference.getInstance().getCookies() != null) {
            sendShowChatActivity();
        }

        if (BuildConfig.DEBUG && getView().getStringUrl().length() == 0)
            getView().setTextUrl("https://mattermost.kilograpp.com");
    }

    public void checkEnterUrl(String url) {
        //TODO check logic url
        //getView().setShowNextButton(isValidUrl(url));
    }

    private boolean isValidUrl(String url) {
        Matcher m = mPatternUrl.matcher(url);
        return m.matches();
    }

    void request(String url) {
        if (!isValidUrl(url)) {
            sendShowErrorEditText();
            return;
        }

        URI newUrl = URI.create(url);
        String s = newUrl.getAuthority();
        if (s == null) {
            s = url.toString();
        }

        //TODO FIX logic
        MattermostPreference.getInstance().setBaseUrl(s);
        mMattermostApp.refreshMattermostRetrofitService();

        try {
            mMattermostApp.getMattermostRetrofitService();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            sendShowError("Url is not valid https://");
            return;
        }

        this.url = url;
        start(REQUEST_CHECK);
    }

    // to view methods

    private void sendVisibleProgress(Boolean visibility){
        createTemplateObservable(visibility)
                .subscribe(split(MainRxAcivity::setShowProgress));
    }

    private void sendShowLoginActivity(){
        createTemplateObservable(new Object())
                .subscribe(split((mainRxAcivity, o) -> mainRxAcivity.showLoginActivity()));
    }

    private void sendShowError(String error){
        createTemplateObservable(error)
                .subscribe(split(MainRxAcivity::showErrorText));
    }

    private void sendShowErrorEditText(){
        createTemplateObservable(new Object())
                .subscribe(split((mainRxAcivity, o) -> mainRxAcivity.showEditTextErrorMessage()));
    }

    private void sendShowChatActivity(){
        createTemplateObservable(new Object())
                .subscribe(split((mainRxAcivity,o) -> mainRxAcivity.showChatActivity()));

    }
}
