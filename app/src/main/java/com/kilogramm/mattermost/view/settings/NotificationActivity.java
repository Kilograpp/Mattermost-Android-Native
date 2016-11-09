package com.kilogramm.mattermost.view.settings;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityNotificationBinding;
import com.kilogramm.mattermost.presenter.settings.NotificationPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 18.10.16.
 */
@RequiresPresenter(NotificationPresenter.class)
public class NotificationActivity extends BaseActivity<NotificationPresenter> {
    private ActivityNotificationBinding binding;

    private NotificationFragment notificationFragment;
    private NotificationEmailFragment notificationEmailFragment;
    private NotificationPushFragment notificationPushFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification);
        setToolbar();
        openNotification();
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }


    private void setToolbar() {
        setupToolbar(getString(R.string.notification), true);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void openMobilePushNotification() {
        setupToolbar(getString(R.string.notification_mob_push), true);
        if (notificationPushFragment == null)
            notificationPushFragment = new NotificationPushFragment();

        getFragmentManager().beginTransaction()
                .replace(binding.fragmentNotification.getId(), notificationPushFragment)
                .commit();
        binding.fragmentNotification.setTag("NotificationPushFragment");
    }

    public void openWordsTriggerMentions() {
        setupToolbar(getString(R.string.notification_words_trigger), true);
        getFragmentManager().beginTransaction()
                .replace(binding.fragmentNotification.getId(), new WordsTriggerMentionsFragment())
                .commit();
        binding.fragmentNotification.setTag("WordsTriggerMentionsFragment");
    }

    public void openNotification() {
        setToolbar();
        if (notificationFragment == null)
            notificationFragment = new NotificationFragment();
        getFragmentManager().beginTransaction()
                .replace(binding.fragmentNotification.getId(), notificationFragment)
                .commit();
        binding.fragmentNotification.setTag("NotificationFragment");
    }


    public void openEmailNotification() {
        setupToolbar(getString(R.string.notification_email), true);
        if (notificationEmailFragment == null)
            notificationEmailFragment = new NotificationEmailFragment();
        getFragmentManager().beginTransaction()
                .replace(binding.fragmentNotification.getId(), notificationEmailFragment)
                .commit();
        binding.fragmentNotification.setTag("NotificationEmailFragment");
    }


    @Override
    public void onBackPressed() {
        switch (binding.fragmentNotification.getTag().toString()) {
            case "NotificationFragment":
                super.onBackPressed();
                break;
            case "WordsTriggerMentionsFragment":
                ((WordsTriggerMentionsFragment) getFragmentManager().findFragmentById(R.id.fragmentNotification)).setMentionsKeys();
                openNotification();
                break;
            default:
                openNotification();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                BaseActivity.hideKeyboard(this);
                onBackPressed();
                return true;
            case R.id.action_save:
                updateNotification();
                BaseActivity.hideKeyboard(this);
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_toolbar, menu);
        return true;
    }

    public void updateNotification() {
        getPresenter().requestUpdateNotify();
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, NotificationActivity.class);
        context.startActivity(starter);
    }

}
