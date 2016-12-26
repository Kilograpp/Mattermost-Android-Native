package com.kilogramm.mattermost.view.settings;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityChangePasswordBinding;
import com.kilogramm.mattermost.presenter.settings.PasswordChangePresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 19.10.2016.
 */
@RequiresPresenter(PasswordChangePresenter.class)
public class PasswordChangeActivity extends BaseActivity<PasswordChangePresenter> {

    private ActivityChangePasswordBinding mBinding;
    private MenuItem mMenuItem;

    public PasswordChangeActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_change_password);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.edit_password));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                mMenuItem = item;
                BaseActivity.hideKeyboard(this);
                item.setVisible(false);
                mBinding.progressBar.setVisibility(View.VISIBLE);
                onClickSave();
                return true;
            case android.R.id.home:
                hideKeyboard(this);
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onClickSave() {
        hideKeyboard(this);
        if(mBinding.newPassword.getText().toString().equals(mBinding.newPasswordConfirm.getText().toString())) {
            getPresenter().requestSave(mBinding.currentPassword.getText().toString(),
                    mBinding.newPassword.getText().toString());
        } else {
            showErrorText(getString(R.string.error_in_confirm_password));
            hideProgressBar();
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, PasswordChangeActivity.class);
        context.startActivity(starter);
    }

    public void showSuccessMessage(){
        Toast.makeText(this, getString(R.string.password_changed), Toast.LENGTH_SHORT).show();
    }

    public void hideProgressBar(){
        mMenuItem.setVisible(true);
        mBinding.progressBar.setVisibility(View.INVISIBLE);
    }
}