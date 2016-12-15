package com.kilogramm.mattermost.view.settings;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityChangeEmailBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.presenter.settings.EmailEditPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 19.10.2016.
 */
@RequiresPresenter(EmailEditPresenter.class)
public class EmailEditActivity extends BaseActivity<EmailEditPresenter> {

    private ActivityChangeEmailBinding mBinding;

    private MenuItem mMenuItem;

    public EmailEditActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_change_email);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.edit_email));

        User editedUser = new User(UserRepository.query(new UserRepository.
                UserByIdSpecification(MattermostPreference.getInstance().getMyUserId())).first());
        mBinding.currentEmail.setText(editedUser.getEmail());
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
                onBackPressed();
                hideKeyboard(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onClickSave() {
        hideKeyboard(this);
        String newEmail = mBinding.newEmail.getText().toString();
        if (newEmail != null && newEmail.length() > 0
                && android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            User editedUser = new User(UserRepository.query(new UserRepository.
                    UserByIdSpecification(MattermostPreference.getInstance().getMyUserId())).first());
            editedUser.setEmail(mBinding.newEmail.getText().toString());
            getPresenter().requestSave(editedUser);
        } else {
            showErrorText(getString(R.string.invalid_email));
            hideProgressBar();
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, EmailEditActivity.class);
        context.startActivity(starter);
    }

    public void showSuccessMessage() {
        hideProgressBar();
        Toast.makeText(this, getString(R.string.verify_email), Toast.LENGTH_SHORT).show();
    }

    public void hideProgressBar(){
        mMenuItem.setVisible(true);
        mBinding.progressBar.setVisibility(View.INVISIBLE);
    }
}