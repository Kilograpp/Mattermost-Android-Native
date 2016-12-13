package com.kilogramm.mattermost.view.settings;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
    private ActivityNotificationBinding mBinding;

    private NotificationFragment mNotificationFragment;
    private NotificationEmailFragment mNotificationEmailFragment;
    private NotificationPushFragment mNotificationPushFragment;

    MenuItem mSaveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_notification);
        setToolbar();
        openNotification();
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        switch (mBinding.fragmentLayoutNotification.getTag().toString()) {
            case "NotificationFragment":
                super.onBackPressed();
                break;
            case "WordsTriggerMentionsFragment":
                ((WordsTriggerMentionsFragment) getFragmentManager().findFragmentById(R.id.fragmentLayoutNotification)).setMentionsKeys();
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
                mSaveItem = item;
                updateNotification();
                BaseActivity.hideKeyboard(this);
                item.setVisible(false);
                mBinding.progressBar.setVisibility(View.VISIBLE);
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
    
    public void openMobilePushNotification() {
        setupToolbar(getString(R.string.notification_mob_push), true);
        if (mNotificationPushFragment == null)
            mNotificationPushFragment = new NotificationPushFragment();

        getFragmentManager().beginTransaction()
                .replace(mBinding.fragmentLayoutNotification.getId(), mNotificationPushFragment)
                .commit();
        mBinding.fragmentLayoutNotification.setTag("NotificationPushFragment");
    }

    public void openWordsTriggerMentions() {
        setupToolbar(getString(R.string.notification_words_trigger), true);
        getFragmentManager().beginTransaction()
                .replace(mBinding.fragmentLayoutNotification.getId(), new WordsTriggerMentionsFragment())
                .commit();
        mBinding.fragmentLayoutNotification.setTag("WordsTriggerMentionsFragment");
    }

    public void openNotification() {
        setToolbar();
        if (mNotificationFragment == null)
            mNotificationFragment = new NotificationFragment();
        getFragmentManager().beginTransaction()
                .replace(mBinding.fragmentLayoutNotification.getId(), mNotificationFragment)
                .commit();
        mBinding.fragmentLayoutNotification.setTag("NotificationFragment");
    }


    public void openEmailNotification() {
        setupToolbar(getString(R.string.notification_email), true);
        if (mNotificationEmailFragment == null)
            mNotificationEmailFragment = new NotificationEmailFragment();
        getFragmentManager().beginTransaction()
                .replace(mBinding.fragmentLayoutNotification.getId(), mNotificationEmailFragment)
                .commit();
        mBinding.fragmentLayoutNotification.setTag("NotificationEmailFragment");
    }

    public void requestSave(String s) {
        mSaveItem.setVisible(true);
        mBinding.progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void updateNotification() {
        getPresenter().requestUpdateNotify();
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, NotificationActivity.class);
        context.startActivity(starter);
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.notification), true);
    }
}
