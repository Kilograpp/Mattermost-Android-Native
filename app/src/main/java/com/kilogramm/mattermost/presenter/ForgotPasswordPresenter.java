package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.error.HttpError;
import com.kilogramm.mattermost.model.fromnet.ForgotData;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.network.MattermostHttpSubscriber;
import com.kilogramm.mattermost.view.authorization.ForgotPasswordActivity;

import nucleus.presenter.Presenter;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kraftu on 13.09.16.
 */
public class ForgotPasswordPresenter extends Presenter<ForgotPasswordActivity> {

    private MattermostApp mMattermostApp;

    private Subscription mSubscription;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mMattermostApp = MattermostApp.getSingleton();

    }

    @Override
    protected void onTakeView(ForgotPasswordActivity forgotPasswordActivity) {
        super.onTakeView(forgotPasswordActivity);
        getView().showProgress(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void sendEmail(String userEmail){

        if(mSubscription!=null && !mSubscription.isUnsubscribed()){
            mSubscription.unsubscribe();
        }

        //TODO FIX logic
        ApiMethod service = null;
        try{
            service = mMattermostApp.getMattermostRetrofitService();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            getView().showErrorText("Url is not valid https://");
            return;
        }

        getView().showProgress(true);

        mSubscription = service.forgotPassword(new ForgotData(userEmail))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MattermostHttpSubscriber<ForgotData>() {
                    @Override
                    public void onCompleted() {
                        getView().showProgress(false);
                        getView().finishActivity();
                    }

                    @Override
                    public void onErrorMattermost(HttpError httpError, Throwable e) {
                        getView().showProgress(false);
                        getView().showErrorText(httpError.getMessage());
                    }

                    @Override
                    public void onNext(ForgotData forgotData) {
                        getView().showMessage(forgotData.email);
                    }
                });

    }

}
