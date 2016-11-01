package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ChannelActivityBinding;
import com.kilogramm.mattermost.presenter.ChannelPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 01.11.16.
 */
@RequiresPresenter(ChannelPresenter.class)
public class ChannelActivity extends BaseActivity<ChannelPresenter> {
    ChannelActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.channel_activity);
        setToolbar();
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.channel_info), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ChannelActivity.class);
        context.startActivity(starter);
    }
}
