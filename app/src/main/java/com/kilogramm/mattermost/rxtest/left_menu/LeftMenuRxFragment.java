package com.kilogramm.mattermost.rxtest.left_menu;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentLeftMenuBinding;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository.ChannelDirectByIdSpecification;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.member.MemberAll;
import com.kilogramm.mattermost.model.entity.member.MembersRepository;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.model.entity.team.TeamRepository;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.kilogramm.mattermost.rxtest.left_menu.adapters.ChannelListAdapter;
import com.kilogramm.mattermost.rxtest.left_menu.adapters.DirectListAdapter;
import com.kilogramm.mattermost.rxtest.left_menu.adapters.PrivateListAdapter;
import com.kilogramm.mattermost.view.addchat.AddExistingChannelsActivity;
import com.kilogramm.mattermost.view.createChannelGroup.CreateNewChannelActivity;
import com.kilogramm.mattermost.view.createChannelGroup.CreateNewGroupActivity;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;
import com.kilogramm.mattermost.view.fragments.BaseFragment;

import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

import static android.app.Activity.RESULT_OK;
import static com.kilogramm.mattermost.model.entity.channel.Channel.DIRECT;
import static com.kilogramm.mattermost.model.entity.channel.Channel.OPEN;
import static com.kilogramm.mattermost.model.entity.channel.Channel.PRIVATE;

/**
 * Created by Evgeny on 14.11.2016.
 */

@RequiresPresenter(LeftMenuRxPresenter.class)
public class LeftMenuRxFragment extends BaseFragment<LeftMenuRxPresenter> implements OnLeftMenuClickListener {

    private static final String TAG = "LEFT_MENU_RX_FRAGMENT";

    private static final int NOT_SELECTED = -1;

    public static final int REQUEST_CREATE_CHANNEL = 97;
    public static final int REQUEST_CREATE_GROUP = 96;
    public static final int REQUEST_JOIN_CHANNEL = 98;
    public static final int REQUEST_JOIN_DIRECT = 99;

    private FragmentLeftMenuBinding mBinding;

    private ChannelListAdapter channelListAdapter;
    private PrivateListAdapter privateListAdapter;
    private DirectListAdapter directListAdapter;

    private OnChannelChangeListener listener;

    private RealmResults<Member> members;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_left_menu, container, false);
        View view = mBinding.getRoot();
        members = MembersRepository.query(new MemberAll());
        members.addChangeListener(element -> {
            if (channelListAdapter != null) {
                channelListAdapter.notifyDataSetChanged();
            }
        });
        initView();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CREATE_CHANNEL) {
                Log.d(TAG, "onActivityResult: REQUEST_CREATE_CHANNEL");
                handleRequestCreateChannel(data);
            }
            if (requestCode == REQUEST_CREATE_GROUP) {
                Log.d(TAG, "onActivityResult: REQUEST_CREATE_GROUP");
                handleRequestCreateGroup(data);
            }
            if (requestCode == REQUEST_JOIN_DIRECT) {
                Log.d(TAG, "onActivityResult: REQUEST_JOIN_DIRECT");
                handleRequestJoinDirect(data);
            }
            if (requestCode == REQUEST_JOIN_CHANNEL) {
                Log.d(TAG, "onActivityResult: REQUEST_JOIN_CHANNEL");
                handleRequestJoinChannel(data);
            }
        }
    }

    private void handleRequestJoinChannel(Intent data) {
        onChannelClick(data.getStringExtra(AddExistingChannelsActivity.CHANNEL_ID),
                data.getStringExtra(AddExistingChannelsActivity.CHANNEL_NAME),
                data.getStringExtra(AddExistingChannelsActivity.TYPE));
    }

    private void handleRequestJoinDirect(Intent data) {
        String userTalkToId = data.getStringExtra(WholeDirectListActivity.USER_ID);
        Preferences saveData = new Preferences(userTalkToId,
                MattermostPreference.getInstance().getMyUserId(),
                true,
                "direct_channel_show");
        RealmResults<Channel> channels = ChannelRepository.query(new ChannelDirectByIdSpecification(userTalkToId));
        if (channels.size() == 0) {
            getPresenter().requestSaveData(saveData, userTalkToId);
        } else {
            onChannelClick(channels.get(0).getId(), channels.get(0).getUsername(), channels.get(0).getType());
        }
    }

    private void handleRequestCreateGroup(Intent data) {
        privateListAdapter.setSelectedItem(
                privateListAdapter.getPositionById(data.getStringExtra(CreateNewGroupActivity.CREATED_GROUP_ID)));
        onChannelClick(data.getStringExtra(CreateNewGroupActivity.CREATED_GROUP_ID),
                data.getStringExtra(CreateNewGroupActivity.GROUP_NAME),
                data.getStringExtra(CreateNewGroupActivity.TYPE));
    }

    private void handleRequestCreateChannel(Intent data) {
        channelListAdapter.setSelectedItem(
                channelListAdapter.getPositionById(data.getStringExtra(CreateNewChannelActivity.CREATED_CHANNEL_ID)));
        onChannelClick(data.getStringExtra(CreateNewChannelActivity.CREATED_CHANNEL_ID),
                data.getStringExtra(CreateNewChannelActivity.CHANNEL_NAME),
                data.getStringExtra(CreateNewChannelActivity.TYPE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        members.removeChangeListeners();
    }

    @Override
    public void onChannelClick(String itemId, String name, String type) {
        Log.d(TAG, "Click listener : channelId = " + itemId + "\n" +
                "name = " + name + "\n" +
                "type = " + type + "\n");
        switch (type) {
            case OPEN:
                directListAdapter.setSelectedItem(NOT_SELECTED);
                privateListAdapter.setSelectedItem(NOT_SELECTED);
                break;
            case PRIVATE:
                channelListAdapter.setSelectedItem(NOT_SELECTED);
                directListAdapter.setSelectedItem(NOT_SELECTED);
                break;
            case DIRECT:
                channelListAdapter.setSelectedItem(NOT_SELECTED);
                privateListAdapter.setSelectedItem(NOT_SELECTED);
                break;
        }
        if (!itemId.equals(MattermostPreference.getInstance().getLastChannelId())) {
            sendOnChange(itemId, name);
            MattermostPreference.getInstance().setLastChannelId(itemId);
        }
    }

    private void sendOnChange(String itemId, String name) {
        if (listener != null) {
            listener.onChange(itemId, name);
        }
    }

    @Override
    public void onCreateChannelClick(View view) {
        Log.d(TAG, "OnCreate listener ");
        switch (view.getId()) {
            case R.id.addChannel:
                CreateNewChannelActivity.startActivityForResult(this, REQUEST_CREATE_CHANNEL);
                break;
            case R.id.addGroup:
                CreateNewGroupActivity.startActivityForResult(this, REQUEST_CREATE_GROUP);
                break;
        }
    }

    private void initView() {
        initTeamHeader();
        initChannelList();
        initPrivateList();
        initDirectList();
    }

    private void initTeamHeader() {
        RealmResults<Team> teams = TeamRepository.query();
        for (Team item : teams) {
            if (item.getId().equals(MattermostPreference.getInstance().getTeamId())) {
                mBinding.leftMenuHeader.teamHeaderText.setText(item.getDisplayName().toUpperCase());
            }
        }
    }

    private void initChannelList() {
        Log.d(TAG, "initChannelList");
        RealmResults<Channel> channels = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("O"));
        mBinding.frChannel.recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.frChannel.addChannel.setOnClickListener(this::onCreateChannelClick);
        mBinding.frChannel.btnMoreChannel.setOnClickListener(this::openMore);
        channelListAdapter = new ChannelListAdapter(channels, getActivity(), members, this);
        mBinding.frChannel.recView.setAdapter(channelListAdapter);
    }

    private void initPrivateList() {
        Log.d(TAG, "initPrivateList");
        RealmResults<Channel> channels = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("P"));
        mBinding.frPrivate.recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.frPrivate.addGroup.setOnClickListener(this::onCreateChannelClick);
        privateListAdapter = new PrivateListAdapter(channels, getActivity(), members, this);
        mBinding.frPrivate.recView.setAdapter(privateListAdapter);
    }

    private void initDirectList() {
        Log.d(TAG, "initDirectList");
        RealmResults<Channel> channels = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("D"));
        RealmResults<UserStatus> statusRealmResults = UserStatusRepository.query(new UserStatusRepository.UserStatusAllSpecification());
        mBinding.frDirect.recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.frDirect.btnMore.setOnClickListener(this::openMore);
        directListAdapter = new DirectListAdapter(channels, getActivity(), this, members, statusRealmResults);
        mBinding.frDirect.recView.setAdapter(directListAdapter);
    }

    public void setOnChannelChangeListener(OnChannelChangeListener listener) {
        this.listener = listener;
    }

    private void openMore(View view) {
        switch (view.getId()) {
            case R.id.btnMoreChannel:
                AddExistingChannelsActivity.startActivityForResult(this, REQUEST_JOIN_CHANNEL);
                break;
            case R.id.btnMore:
                WholeDirectListActivity.startActivityForResult(this, REQUEST_JOIN_DIRECT);
                break;
        }
    }

    public void setSelectItemMenu(String id, String typeChannel) {
        switch (typeChannel) {
            case Channel.OPEN:
                channelListAdapter.setSelectedItem(channelListAdapter.getPositionById(id));
                break;
            case Channel.PRIVATE:
                privateListAdapter.setSelectedItem(privateListAdapter.getPositionById(id));
                break;
            case Channel.DIRECT:
                directListAdapter.setSelectedItem(directListAdapter.getPositionById(id));
                break;
        }
    }
}
