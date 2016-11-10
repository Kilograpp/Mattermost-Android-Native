package com.kilogramm.mattermost.view.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityChangeEmailBinding;
import com.kilogramm.mattermost.databinding.ActivityChangePasswordBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.presenter.settings.EmailEditPresenter;
import com.kilogramm.mattermost.presenter.settings.PasswordChangePresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 19.10.2016.
 */
@RequiresPresenter(PasswordChangePresenter.class)
public class PasswordChangeActivity extends BaseActivity<PasswordChangePresenter> {

    ActivityChangePasswordBinding binding;

    ProgressDialog progressDialog;

    public PasswordChangeActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.edit_email));
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
                onClickSave();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onClickSave() {
        hideKeyboard(this);
        if(binding.newPassword.getText().toString().equals(binding.newPasswordConfirm.getText().toString())) {
            progressDialog = new ProgressDialog(this);
            progressDialog.show();
            progressDialog.setContentView(R.layout.data_processing_progress_layout);
            getPresenter().requestSave(binding.currentPassword.getText().toString(),
                    binding.newPassword.getText().toString());
        } else {
            showErrorText(getString(R.string.error_in_confirm_password));
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, PasswordChangeActivity.class);
        context.startActivity(starter);
    }

    public void showSuccessMessage(){
        Toast.makeText(this, getString(R.string.password_changed), Toast.LENGTH_SHORT).show();
    }

    public void dissmisProgressDialog(){
        if(progressDialog != null) progressDialog.dismiss();
    }
}