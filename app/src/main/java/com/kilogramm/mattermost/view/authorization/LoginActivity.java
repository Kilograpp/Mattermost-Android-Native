package com.kilogramm.mattermost.view.authorization;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MenuItem;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityLoginBinding;
import com.kilogramm.mattermost.presenter.LoginPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 26.07.2016.
 */
@RequiresPresenter(LoginPresenter.class)
public class LoginActivity extends BaseActivity<LoginPresenter> {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setLoginPresenter(getPresenter());
        setupToolbar("Sign In", true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public static void startActivity(Context context, Integer flags) {
        Intent intent = new Intent(context, LoginActivity.class);
        if (flags != null) {
            intent.setFlags(flags);
        }
        context.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
