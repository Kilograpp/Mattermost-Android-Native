package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ChannelActivityBinding;
import com.kilogramm.mattermost.presenter.channel.ChannelPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 01.11.16.
 */
@RequiresPresenter(ChannelPresenter.class)
public class ChannelActivity extends BaseActivity<ChannelPresenter> implements View.OnClickListener{
    ChannelActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.channel_activity);
        setToolbar();
        initClick();
    }

    private void initClick() {
        binding.seeAll.setOnClickListener(this);
        binding.addMembers.setOnClickListener(this);
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.channel_info), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, ChannelActivity.class);
        context.startActivity(starter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.seeAll:
                AllMembersActivity.start(this);
                break;
            case R.id.addMembers:
                AddMembersActivity.start(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
