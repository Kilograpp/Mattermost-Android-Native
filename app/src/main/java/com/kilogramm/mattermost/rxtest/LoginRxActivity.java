package com.kilogramm.mattermost.rxtest;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityRxLoginBinding;
import com.kilogramm.mattermost.view.BaseActivity;

import icepick.Icepick;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 04.10.2016.
 */
@RequiresPresenter(LoginRxPresenter.class)
public class LoginRxActivity extends BaseActivity<LoginRxPresenter> {

    private ActivityRxLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rx_login);
        binding.setLoginPresenter(getPresenter());
        setupToolbar("Sign In", true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }


    public void showChatActivity() {
        GeneralRxActivity.start(this,
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }


    public static void startActivity(Context context, Integer flags) {
        Intent intent = new Intent(context, LoginRxActivity.class);
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
