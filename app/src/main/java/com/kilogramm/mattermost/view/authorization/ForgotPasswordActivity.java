package com.kilogramm.mattermost.view.authorization;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityForgotPasswordBinding;
import com.kilogramm.mattermost.presenter.ForgotPasswordPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 27.07.2016.
 */
@RequiresPresenter(ForgotPasswordPresenter.class)
public class ForgotPasswordActivity extends BaseActivity<ForgotPasswordPresenter> {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password);
        initView();
    }

    private void initView(){
        setupToolbar(getString(R.string.title_forgot_password), true);
        setColorScheme(R.color.colorPrimary,R.color.colorPrimaryDark);
        binding.buttonRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getPresenter().sendEmail(getEmailText());
                getPresenter().requestSendEmail(getEmailText());
            }
        });
    }

    public String getEmailText(){
        return  binding.editEmail.getText().toString();
    }

    public void showProgress(boolean show){
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void showErrorText(String text){
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    public void showMessage(String userEmail){
        Toast.makeText(this,String.format(getString(R.string.text_forgot_password),userEmail),Toast.LENGTH_SHORT).show();
    }

    public void finishActivity(){
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ForgotPasswordActivity.class);
        context.startActivity(starter);
    }
    public void hideKeyboard(){
        hideKeyboard(this);
    }
}
