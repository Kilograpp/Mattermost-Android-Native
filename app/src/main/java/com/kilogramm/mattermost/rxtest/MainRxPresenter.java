package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Patterns;

import com.kilogramm.mattermost.BuildConfig;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.network.ServerMethod;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icepick.State;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 03.10.2016.
 */
public class MainRxPresenter extends BaseRxPresenter<MainRxActivity> {

    private static final int REQUEST_CHECK = 1;

    private static Pattern mPatternUrl = Patterns.WEB_URL;

    private MattermostApp mMattermostApp;

    @State
    String url;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mMattermostApp = MattermostApp.getSingleton();

        // Check given url. If it's valid, open login activity
        restartableFirst(REQUEST_CHECK, () -> {
            sendVisibleProgress(true);
            return ServerMethod.getInstance()
                    .initLoad()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io());
        }, (mainActivity, initObject) -> {
            MattermostPreference.getInstance().setSiteName(initObject.getClientCfg().getSiteName());
            sendVisibleProgress(false);
            sendShowLoginActivity();
            sendShowNextButton(true);
        }, (mainActivity, throwable) -> {
            sendVisibleProgress(false);
            sendShowError(parseError(throwable, MattermostApp.getSingleton().getString(R.string.error_no_team)));
            sendShowNextButton(true);
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
            sendSetUrlString("https://mattermost.kilograpp.com");
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

        MattermostPreference.getInstance().setBaseUrl(authorisedUri);
        mMattermostApp.refreshMattermostRetrofitService();

        if (isNetworkAvailable()) {
            start(REQUEST_CHECK);
        } else {
            sendShowNextButton(true);
            sendShowError(MattermostApp.getSingleton().getString(R.string.error_network_connection));
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

    private void sendShowNextButton(boolean b) {
        createTemplateObservable(b)
                .subscribe(split((mainRxActivity, aBoolean) -> mainRxActivity.setShowNextButton(b)));
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
