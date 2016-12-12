package com.kilogramm.mattermost.view.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityChannelBinding;
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
import com.vdurmont.emoji.EmojiParser;

import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 01.11.16.
 */
@RequiresPresenter(ChannelPresenter.class)
public class ChannelActivity extends BaseActivity<ChannelPresenter> implements View.OnClickListener {
    private static final String CHANNEL_ID = "channel_id";
    public static final int REQUEST_ID = 201;

    ActivityChannelBinding binding;
    String channelId;
    AllMembersAdapter allMembersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channelId = getIntent().getStringExtra(CHANNEL_ID);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_channel);

        initView();
        initClick();
        setCTollBarTitle(getPresenter().getChannel().getDisplayName());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.TextViewToolbarText:
                if (getPresenter().getChannel().getDisplayName()
                        .equals(binding.TextViewToolbarText.getText().toString()))
                    NameChannelActivity.start(this, channelId);
                break;
            case R.id.textViewChannelIcon:
                NameChannelActivity.start(this, channelId);
                break;
            case R.id.textViewChannelName:
                NameChannelActivity.start(this, channelId);
                break;
            case R.id.cardViewHeader:
                HeaderChannelActivity.start(this, getPresenter().getChannel().getHeader(), channelId);
                break;
            case R.id.cardViewPurpose:
                PurposeActivity.start(this, getPresenter().getChannel().getPurpose(), channelId);
                break;
            case R.id.textViewSeeAll:
                AllMembersChannelActivity.start(this, channelId);
                break;
            case R.id.cardViewAddMembers:
                AddMembersActivity.start(this, channelId);
                break;
            case R.id.cardViewLeave:
                if (getPresenter().getMemberCount() == 1) {
                    getPresenter().requestDelete();
                } else {
                    getPresenter().requestLeave();
                }
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.nestedScrollViewLayoutData.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(RESULT_CANCELED, new Intent());
        finish();
        return super.onOptionsItemSelected(item);
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
                        binding.TextViewToolbarText.startAnimation(AnimationUtils.loadAnimation(
                                binding.getRoot().getContext(), R.anim.visible_anim));
                        binding.TextViewToolbarText.setText(name);
                        binding.linearLayoutName.setVisibility(View.INVISIBLE);
                        isNotColapsed = false;
                        isShow = true;
                    }
                } else if (isShow) {
                    binding.TextViewToolbarText.startAnimation(AnimationUtils.loadAnimation(
                            binding.getRoot().getContext(), R.anim.visible_anim));
                    binding.TextViewToolbarText.setText(getString(R.string.channel_info));
                    binding.linearLayoutName.setVisibility(View.VISIBLE);
                    isNotColapsed = true;
                    isShow = false;
                }
            }
        });
    }


    public void initiationData(ExtraInfo extraInfo) {
        setToolbar();
        setAdapter(extraInfo);

        Channel channel = getPresenter().getChannel();
        channel.addChangeListener(element -> setMutableData(extraInfo, channel));

        binding.textViewChannelIcon.getBackground()
                .setColorFilter(ColorGenerator.MATERIAL.getRandomColor(), PorterDuff.Mode.MULTIPLY);
        binding.textViewUrlDescription.setText(getMessageLink(channel.getName()));
        binding.textViewIdDescription.setText(channel.getId());

        setMutableData(extraInfo, channel);

        binding.progressBar.setVisibility(View.GONE);
        binding.nestedScrollViewLayoutData.setVisibility(View.VISIBLE);
    }

    public void setAdapter(ExtraInfo extraInfo) {
        allMembersAdapter = new AllMembersAdapter(
                this,
                this::openDirect,
                extraInfo.getMembers(), true);
        binding.recyclerViewList.setAdapter(allMembersAdapter);
        binding.recyclerViewList.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewList.setNestedScrollingEnabled(false);
    }

    public void startGeneralActivity() {
        GeneralRxActivity.start(this, null);
    }

    public static void start(Activity activity, String channelId) {
        Intent starter = new Intent(activity, ChannelActivity.class);
        starter.putExtra(CHANNEL_ID, channelId);
        activity.startActivityForResult(starter, REQUEST_ID);
    }


    public void errorRequest() {
        binding.progressBar.setVisibility(View.GONE);
        binding.nestedScrollViewLayoutData.setVisibility(View.VISIBLE);
    }

    public void finishActivity() {
        Intent intent = new Intent(this, ChannelActivity.class);
        setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    private void copyText(String s) {
        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(s);
    }

    private void initView() {
        getPresenter().initPresenter(MattermostPreference.getInstance().getTeamId(), channelId);
        getPresenter().requestExtraInfo();
    }

    private void setMutableData(ExtraInfo extraInfo, Channel channel) {
        binding.textViewChannelName.setText(channel.getDisplayName());
        binding.textViewChannelIcon.setText(EmojiParser.removeAllEmojis(channel.getDisplayName()));
        binding.textViewHeaderDescription.setText(channel.getHeader());
        binding.textViewPurposeDescription.setText(channel.getPurpose());
        binding.textViewCountMembers.setText(String.format("%s %s",
                extraInfo.getMember_count(),
                getString(R.string.channel_info_count_members)));

        if (Integer.parseInt(extraInfo.getMember_count()) > 5)
            binding.textViewSeeAll.setVisibility(View.VISIBLE);
        else
            binding.textViewSeeAll.setVisibility(View.INVISIBLE);

        if (extraInfo.getMember_count().equals("1")) {
            binding.textViewLeaveDelete.setText(channel.getType().equals(Channel.OPEN)
                    ? getString(R.string.channel_info_delete_channel)
                    : getString(R.string.channel_info_delete_group));
        } else {
            binding.textViewLeaveDelete.setText(channel.getType().equals(Channel.OPEN)
                    ? getResources().getString(R.string.channel_info_leave_channel)
                    : getResources().getString(R.string.channel_info_leave_group));
        }
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


    private void startDialog(String userTalkToId) {
        Preferences preferences = new Preferences(
                userTalkToId,
                MattermostPreference.getInstance().getMyUserId(),
                true,
                "direct_channel_show");

        getPresenter().requestSaveData(preferences, userTalkToId);

    }

    private void initClick() {
        binding.cardViewHeader.setOnClickListener(this);
        binding.cardViewPurpose.setOnClickListener(this);
        binding.textViewChannelName.setOnClickListener(this);
        binding.textViewChannelIcon.setOnClickListener(this);
        binding.cardViewUrl.setOnClickListener(this);
        binding.cardViewId.setOnClickListener(this);
        binding.cardViewLeave.setOnClickListener(this);
        binding.textViewSeeAll.setOnClickListener(this);
        binding.cardViewAddMembers.setOnClickListener(this);
        binding.TextViewToolbarText.setOnClickListener(this);
        binding.cardViewUrl.setOnLongClickListener(view -> {
            copyText(getMessageLink(getPresenter().getChannel().getName()));
            Toast.makeText(this, "Сopied to the clipboard", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setToolbar() {
        binding.TextViewToolbarText.setText(getPresenter().getChannel().getType().equals("O") ?
                R.string.channel_info : R.string.group_info);
        setupToolbar(null, true);
    }
}
