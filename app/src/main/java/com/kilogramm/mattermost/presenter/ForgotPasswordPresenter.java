package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.model.fromnet.ForgotData;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.authorization.ForgotPasswordActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.schedulers.Schedulers;

/**
 * Created by kraftu on 13.09.16.
 */
public class ForgotPasswordPresenter extends BaseRxPresenter<ForgotPasswordActivity> {

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final int REQUEST_SEND_EMAIL = 1;


    private String userEmail;

    private Pattern pattern;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        this.userEmail = "";
        initSendEmailRequest();
        sendShowProgress(false);
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    public boolean validate(final String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();

    }

    private void initSendEmailRequest() {
        restartableFirst(REQUEST_SEND_EMAIL,
                () -> ServerMethod.getInstance()
                        .forgotPassword(new ForgotData(userEmail))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                , (forgotPasswordActivity, forgotData) -> {
                    sendShowProgress(false);
                    sendHideKeyBoard();
                    sendFinishActivity();
                    sendShowMessage(forgotData.email);

                }, (forgotPasswordActivity, throwable) -> {
                    throwable.printStackTrace();
                    sendShowProgress(false);
                    sendShowErrorText(getError(throwable));
                });
    }

    public void requestSendEmail(String userEmail) {
        this.userEmail = userEmail;
        if(validate(userEmail)) {
            sendShowProgress(true);
            start(REQUEST_SEND_EMAIL);
        }else{
            sendShowErrorText("Please enter a valid e-mail");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void sendShowErrorText(String errorMessage) {
        createTemplateObservable(errorMessage)
                .subscribe(split((forgotPasswordActivity, s) ->
                        forgotPasswordActivity.showErrorText(errorMessage)));
    }

    private void sendShowProgress(Boolean bool) {
        createTemplateObservable(bool)
                .subscribe(split(ForgotPasswordActivity::showProgress));
    }

    private void sendHideKeyBoard() {
        createTemplateObservable(new Object())
                .subscribe(split((forgotPasswordActivity, o) -> forgotPasswordActivity.hideKeyboard()));
    }

    private void sendFinishActivity() {
        createTemplateObservable(new Object())
                .subscribe(split((forgotPasswordActivity, o) -> forgotPasswordActivity.finishActivity()));
    }

    private void sendShowMessage(String message) {
        createTemplateObservable(message)
                .subscribe(split((forgotPasswordActivity, s) -> forgotPasswordActivity.showMessage(message)));
    }
}
