package com.kilogramm.mattermost.presenter;

import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.ClientCfg;
import com.kilogramm.mattermost.model.entity.InitObject;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.fromnet.LoginData;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.view.authorization.ForgotPasswordActivity;
import com.kilogramm.mattermost.view.authorization.LoginActivity;
import com.kilogramm.mattermost.view.menu.GeneralActivity;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import nucleus.presenter.Presenter;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ngers on 13.09.16.
 */
public class LoginPresenter extends Presenter<LoginActivity> {

    private static final String TAG = "LoginPresenter";

    private Realm mRealm;

    private String mEditEmail = "";
    private String mEditPassword = "";
    private ObservableBoolean isEnabledSignInButton;
    private ObservableField<String> siteName;
    private ObservableInt isVisibleProgress;

    private Subscription subscription;


    public void onClickSignIn(View v) {
        login(mEditEmail, mEditPassword);
    }

    public void onForgotButtonClick(View v) {
        ForgotPasswordActivity.start(getView());
    }


    private void handleErrorLogin(Throwable e) {
        if (e instanceof HttpException) {
            HttpError error;
            try {
                error = new Gson()
                        .fromJson((((HttpException) e)
                                .response()
                                .errorBody()
                                .string()), HttpError.class);
                Log.d(TAG, error.getMessage());
                Toast.makeText(getView(), error.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (IOException e1) {
                Log.d(TAG, "Message not has body.");
                e1.printStackTrace();
            }
        } else {
            Toast.makeText(getView(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SystemException, stackTrace: \n");
            e.printStackTrace();
        }
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

    private boolean canClickSignIn() {
        return mEditEmail.length() != 0 &&
                mEditPassword.length() != 0 && isValidEmail(mEditEmail);
    }

    private boolean isValidEmail(String email) {
        Pattern p = Patterns.EMAIL_ADDRESS;
        Matcher m = p.matcher(email);
        return m.matches();
    }

    //======================== Network ============================================================

    private void login(String email, String password) {
        Log.d(TAG, "login()");
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        MattermostApp application = MattermostApp.get(getView());
        ApiMethod service = application.getMattermostRetrofitService();

        getView().hideKeyboard(getView());
        isVisibleProgress.set(View.VISIBLE);
        Observable<User> observable = service.login(new LoginData(email, password, "")).cache()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
        subscription = observable.subscribe(new Subscriber<User>() {
            @Override
            public void onCompleted() {
                isVisibleProgress.set(View.GONE);
                initLoad();
                Log.d(TAG, "completed login");
                Toast.makeText(getView(), "login successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                isVisibleProgress.set(View.GONE);
                handleErrorLogin(e);
            }

            @Override
            public void onNext(User user) {
                MattermostPreference.getInstance().setMyUserId(user.getId());
                mRealm.executeTransaction(realm -> realm.copyToRealmOrUpdate(user));
                Log.d(TAG, "save In Database");
            }
        });
    }

    private void initLoad() {

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        MattermostApp application = MattermostApp.get(getView());
        ApiMethod service = application.getMattermostRetrofitService();
        isVisibleProgress.set(View.VISIBLE);
        subscription = service.initLoad()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<InitObject>() {

                    private Boolean isOpenChatScreen = false;

                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete");
                        isVisibleProgress.set(View.GONE);
                        if (isOpenChatScreen) {
                            GeneralActivity.start(getView(),
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        } else {
                            //TODO start team chose activity
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        isVisibleProgress.set(View.GONE);
                        handleErrorLogin(e);
                    }

                    @Override
                    public void onNext(InitObject initObject) {
                        List<Team> teams = saveDataAfterLogin(initObject);
                        isOpenChatScreen = (teams.size() == 1);
                    }
                });
    }

    private List<Team> saveDataAfterLogin(InitObject initObject) {
        mRealm.beginTransaction();
        RealmResults<ClientCfg> results = mRealm.where(ClientCfg.class).findAll();
        results.deleteAllFromRealm();
        mRealm.copyToRealmOrUpdate(initObject.getClientCfg());
        RealmList<Channel> directionProfiles = new RealmList<>();
        directionProfiles.addAll(initObject.getMapDerectProfile().values());
        mRealm.copyToRealmOrUpdate(directionProfiles);
        List<Team> teams = mRealm.copyToRealmOrUpdate(initObject.getTeams());
        mRealm.commitTransaction();
        return teams;
    }

    //======================== Getters for binding ================================================

    public ObservableField<String> getSiteName() {
        return siteName;
    }

    public String getmEditEmail() {
        return mEditEmail;
    }

    public String getmEditPassword() {
        return mEditPassword;
    }

    public ObservableBoolean getIsEnabledSignInButton() {
        return isEnabledSignInButton;
    }

    public ObservableInt getIsVisibleProgress() {
        return isVisibleProgress;
    }

    //======================== Utils ==============================================================

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        Log.d(TAG, "OnCreate()");
        mRealm = Realm.getDefaultInstance();
        isEnabledSignInButton = new ObservableBoolean(false);
        siteName = new ObservableField<String>(mRealm.where(ClientCfg.class).findFirst().getSiteName());
        isVisibleProgress = new ObservableInt(View.GONE);

    }

}
