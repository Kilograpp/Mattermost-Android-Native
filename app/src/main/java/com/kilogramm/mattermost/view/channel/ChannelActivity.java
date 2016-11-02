package com.kilogramm.mattermost.view.channel;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ChannelActivityBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.team.TeamByIdSpecification;
import com.kilogramm.mattermost.model.entity.team.TeamRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.presenter.channel.ChannelPresenter;
import com.kilogramm.mattermost.rxtest.ProfileRxActivity;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 01.11.16.
 */
@RequiresPresenter(ChannelPresenter.class)
public class ChannelActivity extends BaseActivity<ChannelPresenter> implements View.OnClickListener {
    private static final String CHANNEL_ID = "channel_id";
    public static final int REQUEST_ID = 201;
    ChannelActivityBinding binding;
    String channelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channelId = getIntent().getStringExtra(CHANNEL_ID);

        binding = DataBindingUtil.setContentView(this, R.layout.channel_activity);
        initView();
        setToolbar();
        initClick();


    }

    private void initView() {
        getPresenter().initPresenter(MattermostPreference.getInstance().getTeamId(), channelId);
        getPresenter().requestExtraInfo();
    }

    public void initiationData(ExtraInfo extraInfo) {
        setAdapter(extraInfo);

        binding.countMembers.setText(String.format("%s %s",
                extraInfo.getMember_count(),
                getString(R.string.channel_info_count_members)));

        Channel channel = getPresenter().getChannel();

        binding.channelName.setText(channel.getDisplayName());
        binding.channelIcon.setText(String.valueOf(channel.getDisplayName().charAt(0)));
        binding.headerDescription.setText(channel.getHeader());
        binding.purposeDescription.setText(channel.getPurpose());
        binding.urlDescription.setText(getMessageLink(channel.getName()));
        binding.idDescription.setText(channel.getId());

        binding.progressBar.setVisibility(View.GONE);
        binding.layoutData.setVisibility(View.VISIBLE);
    }

    private String getMessageLink(String name) {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/"
                + TeamRepository.query(
                new TeamByIdSpecification(MattermostPreference.getInstance().getTeamId()))
                .first()
                .getName()
                + "/channels/"
                + name;
    }

    public void setAdapter(ExtraInfo extraInfo) {
        TopMembersAdapter topMembersAdapter = new TopMembersAdapter(
                this,
                id -> ProfileRxActivity.start(this, id),
                extraInfo.getMembers());
        binding.list.setAdapter(topMembersAdapter);
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

    public static void start(Activity activity, String channelId) {
        Intent starter = new Intent(activity, ChannelActivity.class);
        starter.putExtra(CHANNEL_ID, channelId);
        activity.startActivityForResult(starter, REQUEST_ID);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.seeAll:
                AllMembersActivity.start(this);
                break;
            case R.id.addMembers:
                AddMembersActivity.start(this);
                break;
            case R.id.leave:
                getPresenter().requestLeave();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
