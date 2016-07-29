package com.kilogramm.mattermost.viewmodel.authorization;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;

import com.kilogramm.mattermost.viewmodel.ViewModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Subscription;

/**
 * Created by Evgeny on 27.07.2016.
 */
public class ForgotPasswordViewModel implements ViewModel {

    private static final String TAG = "ForgotPasswordViewModel";
    private static final String SAVE_EMAIL_EDIT = "save_email_edit";

    private Context context;
    private Subscription subscription;

    private String mEditEmail = "";
    private ObservableBoolean isEnabledRecoveryButton;

    public ForgotPasswordViewModel(Context context) {
        this.context = context;
        isEnabledRecoveryButton = new ObservableBoolean(false);
    }

    public void onClickRecovery(View v){
        //Recovery method
        int a= 1;
    }

    public TextWatcher getEmailTextWatcher(){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEditEmail = s.toString();
                isEnabledRecoveryButton.set(canClickRecovery());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    private boolean canClickRecovery() {
        return mEditEmail.length()!= 0 && isValidEmail(mEditEmail);
    }

    private boolean isValidEmail(String email) {
        Pattern p = Patterns.EMAIL_ADDRESS;
        Matcher m = p.matcher(email);
        return m.matches();
    }


    @Override
    public void destroy() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        subscription = null;
        context = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVE_EMAIL_EDIT, mEditEmail);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mEditEmail = savedInstanceState.getString(SAVE_EMAIL_EDIT);
        isEnabledRecoveryButton.set(canClickRecovery());
    }

    public ObservableBoolean getIsEnabledRecoveryButton() {
        return isEnabledRecoveryButton;
    }
}
