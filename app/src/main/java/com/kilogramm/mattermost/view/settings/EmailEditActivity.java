package com.kilogramm.mattermost.view.settings;

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

    ActivityChangeEmailBinding binding;

    public EmailEditActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_email);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.edit_email));

        User editedUser = new User(UserRepository.query(new UserRepository.
                UserByIdSpecification(MattermostPreference.getInstance().getMyUserId())).first());
        binding.currentEmail.setText(editedUser.getEmail());
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
        String newEmail = binding.newEmail.getText().toString();
        if (newEmail != null && newEmail.length() > 0
                && android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            User editedUser = new User(UserRepository.query(new UserRepository.
                    UserByIdSpecification(MattermostPreference.getInstance().getMyUserId())).first());
            editedUser.setEmail(binding.newEmail.getText().toString());
            getPresenter().requestSave(editedUser);
        } else {
            showErrorText(getString(R.string.invalid_email));
        }
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, EmailEditActivity.class);
        context.startActivity(starter);
    }

    public void showSuccessMessage() {
        Toast.makeText(this, getString(R.string.verify_email), Toast.LENGTH_SHORT).show();
    }
}