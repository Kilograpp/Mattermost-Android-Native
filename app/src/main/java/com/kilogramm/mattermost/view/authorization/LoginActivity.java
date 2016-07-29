package com.kilogramm.mattermost.view.authorization;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityLoginBinding;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.viewmodel.authorization.LoginViewModel;

/**
 * Created by Evgeny on 26.07.2016.
 */
public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        loginViewModel = new LoginViewModel(this);
        binding.setLoginViewModel(loginViewModel);
        setupToolbar("SignIn", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setColorScheme(R.color.colorPrimary,R.color.colorPrimaryDark);
    }

    @Override
    protected void onDestroy() {
        loginViewModel.destroy();
        super.onDestroy();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        loginViewModel.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        loginViewModel.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    public static void startActivity(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }
}
