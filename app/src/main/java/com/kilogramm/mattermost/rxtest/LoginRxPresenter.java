package com.kilogramm.mattermost.rxtest;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.ClientCfg;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.fromnet.LoginData;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.authorization.ForgotPasswordActivity;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 04.10.2016.
 */
public class LoginRxPresenter extends BaseRxPresenter<LoginRxActivity> {

    private static final String TAG = "LoginRxPresenter";

    private static final int REQUEST_LOGIN = 1;
    private static final int REQUEST_INITLOAD = 2;

    Realm realm;

    @State
    String mEditEmail = "";
    @State
    String mEditPassword = "";
    @State
    boolean firstLoginBad = false;

    private ObservableBoolean isEnabledSignInButton;
    private ObservableField<String> siteName;
    private ObservableInt isVisibleProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        realm = Realm.getDefaultInstance();
        isEnabledSignInButton = new ObservableBoolean(false);
        siteName = new ObservableField<>(MattermostPreference.getInstance().getSiteName());
        isVisibleProgress = new ObservableInt(View.GONE);

        initRequestLogin();
        initRequestInitLoad();
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    // This method must have View argument for data binding
    public void onClickSignIn(View v) {
        requestLogin();
    }

    // This method must have View argument for data binding
    public void onForgotButtonClick(View v) {
        ForgotPasswordActivity.start(getView());
    }

    private boolean canClickSignIn() {
        return mEditEmail.length() != 0 && mEditPassword.length() != 0 && isValidEmail(mEditEmail);
    }

    public boolean isValidEmail(String email) { //is now public to use, just checks a string
        Pattern p = Patterns.EMAIL_ADDRESS;
        Matcher m = p.matcher(email);
        return m.matches();
    }

    @Override
    protected void onTakeView(LoginRxActivity loginRxActivity) {
        super.onTakeView(loginRxActivity);
        setRedTextForgotPassword(firstLoginBad);
    }
    //======================== Network ============================================================

    private List<Team> saveDataAfterLogin(InitObject initObject) {
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        RealmResults<ClientCfg> results = realm.where(ClientCfg.class).findAll();
        results.deleteAllFromRealm();

        realm.copyToRealmOrUpdate(initObject.getClientCfg());

        realm.insertOrUpdate(initObject);

        MattermostPreference.getInstance().setSiteName(initObject.getClientCfg().getSiteName());

        realm.copyToRealmOrUpdate(initObject.getPreferences());

        List<Team> teams = realm.copyToRealmOrUpdate(initObject.getTeams());

        realm.commitTransaction();
        return teams;
    }

    //======================== Getters for binding ================================================

    public ObservableField<String> getSiteName() {
        return siteName;
    }

    public ObservableBoolean getIsEnabledSignInButton() {
        return isEnabledSignInButton;
    }

    public ObservableInt getIsVisibleProgress() {
        return isVisibleProgress;
    }

    public TextWatcher getEmailTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEditEmail = s.toString();
                isEnabledSignInButton.set(canClickSignIn());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    public TextWatcher getPasswordTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEditPassword = s.toString();
                isEnabledSignInButton.set(canClickSignIn());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    //======================== Utils ==============================================================

    private void requestLogin() {
        isEnabledSignInButton.set(false);
        start(REQUEST_LOGIN);
    }

    private void requestInitLoad() {
        start(REQUEST_INITLOAD);
    }

    private void initRequestLogin() {
        restartableFirst(REQUEST_LOGIN, () -> {
            sendHideKeyboard();
            isVisibleProgress.set(View.VISIBLE);
            return ServerMethod.getInstance()
                    .login(new LoginData(mEditEmail, mEditPassword, ""))
                    .cache()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }, (loginRxActivity, user) -> {
            MattermostPreference.getInstance().setMyUserId(user.getId());
            MattermostPreference.getInstance().setMyEMail(user.getEmail());
            realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(user));
            requestInitLoad();
        }, (loginRxActivity1, throwable) -> {
            isEnabledSignInButton.set(true);
            isVisibleProgress.set(View.GONE);
            firstLoginBad = true;
            setRedTextForgotPassword(true);
            if (isNetworkAvailable()) {
                sendShowError(parceError(throwable, LOGIN));
            } else {
                sendShowError(parceError(null, NO_NETWORK));
            }
        });
    }

    private void initRequestInitLoad() {
        restartableFirst(REQUEST_INITLOAD, () -> {
            isVisibleProgress.set(View.VISIBLE);
            return ServerMethod.getInstance()
                    .initLoad()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }, (loginRxActivity, initObject) -> {
            List<Team> teams = saveDataAfterLogin(initObject);
            Boolean isOpenChatScreen = (teams.size() == 1);
            isVisibleProgress.set(View.GONE);
            isEnabledSignInButton.set(true);
            if (isOpenChatScreen) {
                loginRxActivity.showChatActivity();
                MattermostPreference.getInstance().setTeamId(teams.get(0).getId());
            } else {
                loginRxActivity.showTeamChoose();
            }
        }, (loginRxActivity1, throwable) -> {
            isEnabledSignInButton.set(true);
            isVisibleProgress.set(View.GONE);
            sendShowError(parceError(throwable, null));
        });
    }

    private void sendShowError(String error) {
        createTemplateObservable(error).subscribe(split(LoginRxActivity::showErrorText));
    }

    private void setRedTextForgotPassword(boolean isRed) {
        createTemplateObservable(isRed)
                .subscribe(split(LoginRxActivity::setRedColorForgotPasswordText));
    }

    private void sendHideKeyboard() {
        createTemplateObservable(new Object())
                .subscribe(split((loginRxActivity, o) -> loginRxActivity.hideKeyboard()));
    }
}

