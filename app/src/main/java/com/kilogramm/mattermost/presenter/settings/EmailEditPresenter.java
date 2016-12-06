package com.kilogramm.mattermost.presenter.settings;

import android.os.Bundle;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.settings.EmailEditActivity;

import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 19.10.2016.
 */
public class EmailEditPresenter extends BaseRxPresenter<EmailEditActivity> {

    public EmailEditPresenter() {
    }

    private static final int REQUEST_SAVE = 1;

    private ApiMethod service;

    private User editedUser;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        initSave();
    }

    private void initSave() {
        restartableFirst(REQUEST_SAVE,
                () -> service.updateUser(editedUser)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                ,(editProfileRxActivity, user) -> {
                    if (user != null){
                        UserRepository.updateUserAfterSaveSettings(user);
                        sendGood(editProfileRxActivity.getString(R.string.email_updated));
                    }
                },(editProfileRxActivity, throwable) -> sendError(getError(throwable)));
    }

    @Override
    protected void onTakeView(EmailEditActivity emailEditActivity) {
        super.onTakeView(emailEditActivity);
    }

    public void requestSave(User editedUser) {
        this.editedUser = editedUser;
        start(REQUEST_SAVE);
    }

    private void sendGood(String good){
        createTemplateObservable(good)
                .subscribe(split((emailEditActivity, s) -> {
                    emailEditActivity.showSuccessMessage();
                    emailEditActivity.finish();
                }));
    }

    private void sendError(String error){
        createTemplateObservable(error)
                .subscribe(split(BaseActivity::showErrorText));
    }
}
