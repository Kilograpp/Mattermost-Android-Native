package com.kilogramm.mattermost.view.channel;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ChannelActivityBinding;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelByNameSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.team.TeamByIdSpecification;
import com.kilogramm.mattermost.model.entity.team.TeamRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.presenter.channel.ChannelPresenter;
import com.kilogramm.mattermost.rxtest.GeneralRxActivity;
import com.kilogramm.mattermost.utils.ColorGenerator;
import com.kilogramm.mattermost.view.BaseActivity;

import io.realm.RealmResults;
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
    AllMembersAdapter allMembersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channelId = getIntent().getStringExtra(CHANNEL_ID);

        binding = DataBindingUtil.setContentView(this, R.layout.channel_activity);
        initView();
        setToolbar();
        initClick();
        setCTollBarTitle(getPresenter().getChannel().getDisplayName());
    }

    public void setCTollBarTitle(final String name) {
        binding.layoutAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            boolean isNotColapsed = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    if (isNotColapsed) {
                        binding.toolbarText.startAnimation(AnimationUtils.loadAnimation(
                                binding.getRoot().getContext(), R.anim.visible_anim));
                        binding.toolbarText.setText(name);
                        binding.layoutName.setVisibility(View.INVISIBLE);
                        isNotColapsed = false;
                        isShow = true;
                    }
                } else if (isShow) {
                    binding.toolbarText.startAnimation(AnimationUtils.loadAnimation(
                            binding.getRoot().getContext(), R.anim.visible_anim));
                    binding.toolbarText.setText(getString(R.string.channel_info));
                    binding.layoutName.setVisibility(View.VISIBLE);
                    isNotColapsed = true;
                    isShow = false;
                }
            }
        });
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

        channel.addChangeListener(element -> {
            binding.channelName.setText(channel.getDisplayName());
            binding.channelIcon.setText(String.valueOf(channel.getDisplayName().charAt(0)));
            binding.headerDescription.setText(channel.getHeader());
            binding.purposeDescription.setText(channel.getPurpose());
            binding.countMembers.setText(String.format("%s %s",
                    extraInfo.getMember_count(),
                    getString(R.string.channel_info_count_members)));
        });

        binding.channelName.setText(channel.getDisplayName());
        binding.channelIcon.setText(String.valueOf(channel.getDisplayName().charAt(0)));
        binding.channelIcon.getBackground()
                .setColorFilter(ColorGenerator.MATERIAL.getRandomColor(), PorterDuff.Mode.MULTIPLY);
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
        allMembersAdapter = new AllMembersAdapter(
                this,
                id -> openDirect(id),
                extraInfo.getMembers(), true);
        binding.list.setAdapter(allMembersAdapter);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.list.setNestedScrollingEnabled(false);
    }


    private void openDirect(String id) {
        String userId = MattermostPreference.getInstance().getMyUserId();
        if (!userId.equals(id)) {
            RealmResults<Channel> channels = ChannelRepository.query(new ChannelByNameSpecification(null, id));
            if (channels.size() > 0) {
                MattermostPreference.getInstance().setLastChannelId(
                        channels.first().getId()
                );
                startGeneralActivity();
            } else startDialog(id);
        }
    }

    public void startGeneralActivity() {
        GeneralRxActivity.start(this, null);
    }

    private void startDialog(String userTalkToId) {
        Preferences preferences = new Preferences(
                userTalkToId,
                MattermostPreference.getInstance().getMyUserId(),
                true,
                "direct_channel_show");

        getPresenter().requestSaveData(preferences, userTalkToId);

    }

    private void initClick() {
        binding.header.setOnClickListener(this);
        binding.purpose.setOnClickListener(this);
        binding.channelName.setOnClickListener(this);
        binding.channelIcon.setOnClickListener(this);
        binding.url.setOnClickListener(this);
        binding.id.setOnClickListener(this);
        binding.leave.setOnClickListener(this);
        binding.seeAll.setOnClickListener(this);
        binding.addMembers.setOnClickListener(this);
        binding.toolbarText.setOnClickListener(this);
    }

    private void setToolbar() {
        binding.toolbarText.setText(getString(R.string.channel_info));
        setupToolbar(null, true);
    }

    public static void start(Activity activity, String channelId) {
        Intent starter = new Intent(activity, ChannelActivity.class);
        starter.putExtra(CHANNEL_ID, channelId);
        activity.startActivityForResult(starter, REQUEST_ID);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbarText:
                if (getPresenter().getChannel().getDisplayName().equals(binding.toolbarText.getText())
                        && binding.progressBar.getVisibility() == View.VISIBLE)
                    NameActivity.start(this, channelId);
                break;
            case R.id.channel_icon:
                NameActivity.start(this, channelId);
                break;
            case R.id.channelName:
                NameActivity.start(this, channelId);
                break;
            case R.id.header:
                HeaderActivity.start(this, getPresenter().getChannel().getHeader(), channelId);
                break;
            case R.id.purpose:
                PurposeActivity.start(this, getPresenter().getChannel().getPurpose(), channelId);
                break;
            case R.id.seeAll:
                AllMembersActivity.start(this, channelId);
                break;
            case R.id.addMembers:
                AddMembersActivity.start(this, channelId);
                break;
            case R.id.leave:
                getPresenter().requestLeave();
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.layoutData.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(RESULT_CANCELED, new Intent());
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void errorRequest() {
        binding.progressBar.setVisibility(View.GONE);
        binding.layoutData.setVisibility(View.VISIBLE);
    }

    public void finishActivity() {
        Intent intent = new Intent(this, ChannelActivity.class);
        setResult(Activity.RESULT_OK, intent);
        this.finish();
    }
}
