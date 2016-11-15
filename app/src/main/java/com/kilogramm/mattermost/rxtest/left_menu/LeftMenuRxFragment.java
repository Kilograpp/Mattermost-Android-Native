package com.kilogramm.mattermost.rxtest.left_menu;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentLeftMenuBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.member.MemberAll;
import com.kilogramm.mattermost.model.entity.member.MembersRepository;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.kilogramm.mattermost.rxtest.left_menu.adapters.ChannelListAdapter;
import com.kilogramm.mattermost.rxtest.left_menu.adapters.DirectListAdapter;
import com.kilogramm.mattermost.rxtest.left_menu.adapters.PrivateListAdapter;
import com.kilogramm.mattermost.view.fragments.BaseFragment;

import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 14.11.2016.
 */

@RequiresPresenter(LeftMenuRxPresenter.class)
public class LeftMenuRxFragment extends BaseFragment<LeftMenuRxPresenter> implements OnLeftMenuClickListener{

    private static final String TAG = "LEFT_MENU_RX_FRAGMENT";
    private FragmentLeftMenuBinding mBinding;

    private ChannelListAdapter channelListAdapter;
    private PrivateListAdapter privateListAdapter;
    private DirectListAdapter directListAdapter;

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
    public void onStart() {
        super.onStart();
    }

    private void initView() {
        initChannelList();
        initPrivateList();
        initDirectList();
    }

    private void initChannelList() {
        Log.d(TAG, "initChannelList");
        RealmResults<Channel> channels = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("O"));
        mBinding.frChannel.recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.frChannel.addChannel.setOnClickListener(v -> onCreateChannelClick());
        channelListAdapter = new ChannelListAdapter(channels, getActivity(), members, this);
        mBinding.frChannel.recView.setAdapter(channelListAdapter);
    }

    private void initPrivateList() {
        Log.d(TAG, "initPrivateList");
        RealmResults<Channel> channels = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("P"));
        mBinding.frPrivate.recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.frPrivate.addGroup.setOnClickListener(v -> onCreateChannelClick());
        privateListAdapter = new PrivateListAdapter(channels, getActivity(), members, this);
        mBinding.frPrivate.recView.setAdapter(privateListAdapter);
    }

    private void initDirectList() {
        Log.d(TAG, "initPrivateList");
        RealmResults<Channel> channels = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("D"));
        RealmResults<UserStatus> statusRealmResults = UserStatusRepository.query(new UserStatusRepository.UserStatusAllSpecification());

        mBinding.frDirect.recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        directListAdapter = new DirectListAdapter(channels, getActivity(), this, members, statusRealmResults);
        mBinding.frDirect.recView.setAdapter(directListAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onChannelClick(String itemId, String name, String type) {
        Log.d(TAG, "Click listener : channelId = " + itemId + "\n" +
                "name = " + name+ "\n" +
                "type = " + type + "\n");
    }

    @Override
    public void onCreateChannelClick() {
        Log.d(TAG, "OnCreate listener ");
    }
}
