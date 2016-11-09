package com.kilogramm.mattermost.view.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityChangeEmailBinding;
import com.kilogramm.mattermost.databinding.ActivitySettingsBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.presenter.settings.EmailEditPresenter;
import com.kilogramm.mattermost.rxtest.ProfileRxActivity;
import com.kilogramm.mattermost.rxtest.ProfileRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.squareup.picasso.Picasso;

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
        Intent intent = new Intent();
        intent.putExtra(ProfileRxActivity.EDITED_EMAIL, binding.newEmail.getText().toString());
        setResult(RESULT_OK, intent);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, EmailEditActivity.class);
        context.startActivity(starter);
    }

    public static void startForResult(Activity context, int requestCode) {
        Intent starter = new Intent(context, EmailEditActivity.class);
        context.startActivityForResult(starter, requestCode);
    }
}