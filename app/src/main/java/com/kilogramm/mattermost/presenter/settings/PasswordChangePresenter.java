package com.kilogramm.mattermost.presenter.settings;

import android.os.Bundle;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.settings.EmailEditActivity;
import com.kilogramm.mattermost.view.settings.PasswordChangeActivity;

import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 19.10.2016.
 */
public class PasswordChangePresenter extends BaseRxPresenter<PasswordChangeActivity> {

    public PasswordChangePresenter() {
    }

    private static final int REQUEST_SAVE = 1;

    private ApiMethod service;

    private NewPassword newPassword;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        initSave();
    }

    private void initSave() {
        restartableFirst(REQUEST_SAVE,
                () -> service.changePassword(newPassword)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                ,(editProfileRxActivity, user) -> {
                    if (user != null){
                        sendGood(editProfileRxActivity.getString(R.string.email_updated));
                    }
                },(editProfileRxActivity, throwable) -> sendError(getError(throwable)));
    }

    @Override
    protected void onTakeView(PasswordChangeActivity emailEditActivity) {
        super.onTakeView(emailEditActivity);
    }

    public void requestSave(String currentPussword, String newPassword) {
        this.newPassword = new NewPassword(MattermostPreference.getInstance().getMyUserId(),
                currentPussword, newPassword);
        start(REQUEST_SAVE);
    }

    private void sendGood(String good){
        createTemplateObservable(good)
                .subscribe(split((passwordChangeActivity, s) -> {
                    passwordChangeActivity.hideProgressBar();
                    passwordChangeActivity.showSuccessMessage();
                    passwordChangeActivity.finish();
                }));
    }

    private void sendError(String error){
        createTemplateObservable(error)
                .subscribe(split((passwordChangeActivity, s) -> {
                    passwordChangeActivity.showErrorText(s);
                    passwordChangeActivity.hideProgressBar();
                }));
    }

    public class NewPassword{
        @SerializedName("user_id")
        @Expose
        String userId;
        @SerializedName("current_password")
        @Expose
        String currentPassword;
        @SerializedName("new_password")
        @Expose
        String newPassword;

        public NewPassword(String userId, String currentPassword, String newPassword) {
            this.userId = userId;
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }
    }
}
