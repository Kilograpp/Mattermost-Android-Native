package com.kilogramm.mattermost.view.authorization;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityForgotPasswordBinding;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.viewmodel.authorization.ForgotPasswordViewModel;

/**
 * Created by Evgeny on 27.07.2016.
 */
public class ForgotPasswordActivity extends BaseActivity {

    private ActivityForgotPasswordBinding binding;
    private ForgotPasswordViewModel forgotPasswordViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password);
        forgotPasswordViewModel = new ForgotPasswordViewModel(this);
        binding.setViewModel(forgotPasswordViewModel);
        setupToolbar("Password recovery", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setColorScheme(R.color.colorPrimary,R.color.colorPrimaryDark);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ForgotPasswordActivity.class);
        context.startActivity(starter);
    }
}
