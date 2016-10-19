package com.kilogramm.mattermost.view.settings;

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
        getFragmentManager().beginTransaction()
                .replace(binding.fragmentNotification.getId(), new NotificationMobilePusheFragment())
                .commit();
    }

    public void openWordsTriggerMentions() {
        setupToolbar(getString(R.string.notification_words_trigger), true);
        getFragmentManager().beginTransaction()
                .replace(binding.fragmentNotification.getId(), new WordsTriggerMentionsFragment())
                .commit();
    }

    public void openNotification() {
        setToolbar();
        getFragmentManager().beginTransaction()
                .replace(binding.fragmentNotification.getId(), new NotificationFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openNotification();
                return true;
            case R.id.action_save:
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notification_toolbar, menu);
        return true;
    }

    public void updateNotification() {
        getPresenter().requestUpdateNotify();
    }
}
