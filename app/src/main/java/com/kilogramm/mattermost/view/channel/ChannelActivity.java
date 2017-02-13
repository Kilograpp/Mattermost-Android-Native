package com.kilogramm.mattermost.view.channel;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityChannelBinding;
import com.kilogramm.mattermost.model.entity.Preference.PreferenceRepository;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelByNameSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.team.TeamByIdSpecification;
import com.kilogramm.mattermost.model.entity.team.TeamRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.presenter.channel.ChannelPresenter;
import com.kilogramm.mattermost.rxtest.ChatFragmentV2;
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
    private static final String CHANNEL_ID = "CHANNEL_ID";

    public static final int REQUEST_ID = 201;

    ActivityChannelBinding mBinding;
    String mChannelId;
    AllMembersAdapter mAllMembersAdapter;

    private boolean changed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannelId = getIntent().getStringExtra(CHANNEL_ID);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_channel);

        initView();
        initClick();
        setCTollBarTitle(getPresenter().getChannel().getDisplayName());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.TextViewToolbarText:
                if (getPresenter().getChannel().getDisplayName()
                        .equals(mBinding.TextViewToolbarText.getText().toString()))
                    NameChannelActivity.start(this, mChannelId);
                break;
            case R.id.textViewChannelIcon:
                NameChannelActivity.start(this, mChannelId);
                break;
            case R.id.textViewChannelName:
                NameChannelActivity.start(this, mChannelId);
                break;
            case R.id.cardViewHeader:
                HeaderChannelActivity.start(this, getPresenter().getChannel());
                break;
            case R.id.cardViewPurpose:
                PurposeActivity.start(this, getPresenter().getChannel());
                break;
            case R.id.textViewSeeAll:
                AllMembersChannelActivity.start(this, mChannelId);
                break;
            case R.id.cardViewAddMembers:
//                AddMembersActivity.start(this, mChannelId);
                AddMembersActivity.startForResult(this, mChannelId, ChatFragmentV2.ADD_MEMBER_CODE);
                break;
            case R.id.cardViewLeave:
                if (getPresenter().getMemberCount() == 1) {
                    getPresenter().requestDelete();
                } else {
                    getPresenter().requestLeave();
                }
                mBinding.progressBar.setVisibility(View.VISIBLE);
                mBinding.nestedScrollViewLayoutData.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && requestCode == ChatFragmentV2.ADD_MEMBER_CODE)
            changed = true;
    }

    @Override
    public void onBackPressed() {
        if(changed){
            Intent intent = new Intent(this, ChannelActivity.class);
            setResult(Activity.RESULT_OK, intent);
        } else
            setResult(Activity.RESULT_CANCELED, new Intent());

        finish();
    }

    public void setCTollBarTitle(final String name) {
        mBinding.layoutAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
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
                        mBinding.TextViewToolbarText.startAnimation(AnimationUtils.loadAnimation(
                                mBinding.getRoot().getContext(), R.anim.visible_anim));
                        mBinding.TextViewToolbarText.setText(name);
                        mBinding.linearLayoutName.setVisibility(View.INVISIBLE);
                        isNotColapsed = false;
                        isShow = true;
                    }
                } else if (isShow) {
                    mBinding.TextViewToolbarText.startAnimation(AnimationUtils.loadAnimation(
                            mBinding.getRoot().getContext(), R.anim.visible_anim));
                    mBinding.TextViewToolbarText.setText(getString(R.string.channel_info));
                    mBinding.linearLayoutName.setVisibility(View.VISIBLE);
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

        mBinding.textViewChannelIcon.getBackground()
                .setColorFilter(ColorGenerator.MATERIAL.getRandomColor(), PorterDuff.Mode.MULTIPLY);
        mBinding.textViewUrlDescription.setText(getMessageLink(channel.getName()));
        mBinding.textViewIdDescription.setText(channel.getId());

        setMutableData(extraInfo, channel);

        mBinding.progressBar.setVisibility(View.GONE);
        mBinding.nestedScrollViewLayoutData.setVisibility(View.VISIBLE);
    }

    public void setAdapter(ExtraInfo extraInfo) {
        mAllMembersAdapter = new AllMembersAdapter(
                this,
                this::openDirect,
                extraInfo.getMembers(), true);
        mBinding.recyclerViewList.setAdapter(mAllMembersAdapter);
        mBinding.recyclerViewList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerViewList.setNestedScrollingEnabled(false);
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
        mBinding.progressBar.setVisibility(View.GONE);
        mBinding.nestedScrollViewLayoutData.setVisibility(View.VISIBLE);
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
        getPresenter().initPresenter(MattermostPreference.getInstance().getTeamId(), mChannelId);
        getPresenter().requestExtraInfo();
    }

    private void setMutableData(ExtraInfo extraInfo, Channel channel) {
        mBinding.textViewChannelName.setText(channel.getDisplayName());
        mBinding.textViewChannelIcon.setText(EmojiParser.removeAllEmojis(channel.getDisplayName()));
        mBinding.textViewHeaderDescription.setText(channel.getHeader());
        mBinding.textViewPurposeDescription.setText(channel.getPurpose());
        mBinding.textViewCountMembers.setText(String.format("%s %s",
                extraInfo.getMember_count(),
                getString(R.string.channel_info_count_members)));

        if (Integer.parseInt(extraInfo.getMember_count()) > 5)
            mBinding.textViewSeeAll.setVisibility(View.VISIBLE);
        else
            mBinding.textViewSeeAll.setVisibility(View.INVISIBLE);

        if (extraInfo.getMember_count().equals("1")) {
            mBinding.textViewLeaveDelete.setText(channel.getType().equals(Channel.OPEN)
                    ? getString(R.string.channel_info_delete_channel)
                    : getString(R.string.channel_info_delete_group));
        } else {
            mBinding.textViewLeaveDelete.setText(channel.getType().equals(Channel.OPEN)
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
        String myId = MattermostPreference.getInstance().getMyUserId();
        if (!myId.equals(id)) {
            RealmResults<Channel> channels = ChannelRepository.query(new ChannelByNameSpecification(null, id));
            if (channels.size() > 0) {
                MattermostPreference.getInstance().setLastChannelId(channels.first().getId());
                RealmResults<User> rUser = UserRepository.query(new UserRepository.UserByIdSpecification(id));

                User user =  channels.first().getUser();

                if(user == null && rUser.size()!=0) user = rUser.first();

                if(user != null) {
                    PreferenceRepository.update(new PreferenceRepository.PreferenceByNameSpecification(
                            user.getId()), "true");
                    getPresenter().savePreferences();
                    showJumpDialog(user.getUsername());
                }else{
                    Toast.makeText(this,"ResponsedUser is null, refer to the developers",Toast.LENGTH_LONG).show();
                }
            } else {
                startDialog(id);
            }
        }
    }

    private void showJumpDialog(String userName){
        View view = getLayoutInflater().inflate(R.layout.dialog_custom_jump_on_conversation, null);

        TextView wholeJumpMessage = (TextView) view.findViewById(R.id.dialog_question);
        wholeJumpMessage.setText(String.format(
                getResources().getString(R.string.jump_title), userName));

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        dialogBuilder.setView(view);

        Dialog dialog = dialogBuilder.show();

        Button jumpButton = (Button) view.findViewById(R.id.jump);
        if (jumpButton != null) {
            jumpButton.setOnClickListener(v -> {
                startGeneralActivity();
                dialog.cancel();
            });

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
        mBinding.cardViewHeader.setOnClickListener(this);
        mBinding.cardViewPurpose.setOnClickListener(this);
        mBinding.textViewChannelName.setOnClickListener(this);
        mBinding.textViewChannelIcon.setOnClickListener(this);
        mBinding.cardViewUrl.setOnClickListener(this);
        mBinding.cardViewId.setOnClickListener(this);
        mBinding.cardViewLeave.setOnClickListener(this);
        mBinding.textViewSeeAll.setOnClickListener(this);
        mBinding.cardViewAddMembers.setOnClickListener(this);
        mBinding.TextViewToolbarText.setOnClickListener(this);
        mBinding.cardViewUrl.setOnLongClickListener(view -> {
            copyText(getMessageLink(getPresenter().getChannel().getName()));
            Toast.makeText(this, "Сopied to the clipboard", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setToolbar() {
        mBinding.TextViewToolbarText.setText(getPresenter().getChannel().getType().equals("O") ?
                R.string.channel_info : R.string.group_info);
        setupToolbar(null, true);
    }
}
