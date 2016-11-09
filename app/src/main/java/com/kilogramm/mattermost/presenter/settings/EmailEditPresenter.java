package com.kilogramm.mattermost.presenter.settings;

import android.os.Bundle;

import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.rxtest.ProfileRxActivity;
import com.kilogramm.mattermost.view.settings.EmailEditActivity;

/**
 * Created by Evgeny on 19.10.2016.
 */
public class EmailEditPresenter extends BaseRxPresenter<EmailEditActivity> {

    public EmailEditPresenter() {
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    @Override
    protected void onTakeView(EmailEditActivity emailEditActivity) {
        super.onTakeView(emailEditActivity);
    }
}
