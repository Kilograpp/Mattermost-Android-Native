package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.fromnet.ForgotData;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.authorization.ForgotPasswordActivity;

import rx.schedulers.Schedulers;

/**
 * Created by kraftu on 13.09.16.
 */
public class ForgotPasswordPresenter extends BaseRxPresenter<ForgotPasswordActivity> {

    private static final int REQUEST_SEND_EMAIL = 1;

    private ApiMethod service;

    private String userEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        this.userEmail = "";
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        initSendEmailRequest();
        sendShowProgress(false);
    }

    private void initSendEmailRequest() {
        restartableFirst(REQUEST_SEND_EMAIL,
                () -> service.forgotPassword(new ForgotData(userEmail))
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
                    sendErrorText(throwable.getMessage());
                });
    }

    public void requestSendEmail(String userEmail) {
        this.userEmail = userEmail;
        sendShowProgress(true);
        start(REQUEST_SEND_EMAIL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void sendErrorText(String errorMessage) {
        createTemplateObservable(errorMessage)
                .subscribe(split((forgotPasswordActivity, s) -> forgotPasswordActivity.showErrorText(errorMessage)));
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

    /************* before *********************/
//    @Override
//    protected void onTakeView(ForgotPasswordActivity forgotPasswordActivity) {
//        super.onTakeView(forgotPasswordActivity);
//        getView().showProgress(false);
//    }
//
//    public void sendEmail(String userEmail) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
//            mSubscription.unsubscribe();
//        }
//
//        ApiMethod service;
//        try {
//            service = mMattermostApp.getMattermostRetrofitService();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//            getView().showErrorText("Url is not valid https://");
//            return;
//        }
//
//        getView().showProgress(true);

//        mSubscription = service.forgotPassword(new ForgotData(userEmail))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new MattermostHttpSubscriber<ForgotData>() {
//                    @Override
//                    public void onCompleted() {
//                        getView().showProgress(false);
//                        getView().hideKeyboard();
//                        getView().finishActivity();
//                    }

//                    @Override
//                    public void onErrorMattermost(HttpError httpError, Throwable e) {
//                        getView().showProgress(false);
//                        getView().showErrorText(httpError.getMessage());
//                    }

//                    @Override
//                    public void onNext(ForgotData forgotData) {
//                        getView().showMessage(forgotData.email);
//                    }
//                });

//    }
}
