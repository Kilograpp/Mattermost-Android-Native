package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ChannelActivityBinding;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.presenter.channel.ChannelPresenter;
import com.kilogramm.mattermost.rxtest.ProfileRxActivity;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 01.11.16.
 */
@RequiresPresenter(ChannelPresenter.class)
public class ChannelActivity extends BaseActivity<ChannelPresenter> implements View.OnClickListener{
    private static final String CHANNEL_ID = "channel_id";

    ChannelActivityBinding binding;
    String channelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channelId = getIntent().getStringExtra(CHANNEL_ID);

        binding = DataBindingUtil.setContentView(this, R.layout.channel_activity);
        setToolbar();
        initClick();
        initiationData();
    }

    private void initiationData() {
        AllMembersAdapter allMembersAdapter = new AllMembersAdapter(this, UserRepository.query(),
                id -> ProfileRxActivity.start(this,id), 5);
        binding.list.setAdapter(allMembersAdapter);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setNestedScrollingEnabled(false);
    }

    private void initClick() {
        binding.header.setOnClickListener(this);
        binding.purpose.setOnClickListener(this);
        binding.url.setOnClickListener(this);
        binding.id.setOnClickListener(this);
        binding.leave.setOnClickListener(this);
        binding.seeAll.setOnClickListener(this);
        binding.addMembers.setOnClickListener(this);
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.channel_info), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public static void start(Context context, String channelId) {
        Intent starter = new Intent(context, ChannelActivity.class);
        starter.putExtra(CHANNEL_ID, channelId);
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
                break;
            case R.id.leave:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
