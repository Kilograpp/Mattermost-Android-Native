package com.kilogramm.mattermost.view.menu.channelList;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentMenuChannelListBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.view.addchat.AddExistingChannelsActivity;
import com.kilogramm.mattermost.view.createChannelGroup.CreateNewChGrActivity;

import io.realm.OrderedRealmCollection;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 24.08.2016.
 */

public class MenuChannelListFragment extends Fragment {
    public static final int REQUEST_JOIN_CHANNEL = 98;
    public static final int REQUEST_CREATE = 97;

    public final String IS_CHANNEL = "isChannel";

    private FragmentMenuChannelListBinding binding;
    private OnChannelItemClickListener channelItemClickListener;
    private OnSelectedItemChangeListener selectedItemChangeListener;
    private MenuChannelRxAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_channel_list,
                container, false);
        View view = binding.getRoot();
        binding.btnMoreChannel.setOnClickListener(view1 -> goToAddChannelsActivity());
        setupListView();

        binding.addChannel.setOnClickListener(v -> createNewChannel());

        return view;
    }

    private void setupListView() {
        RealmResults<Channel> results = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("O"));
        binding.recView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MenuChannelRxAdapter(getContext(), results,
                (itemId, name, type) -> channelItemClickListener.onChannelClick(itemId, name, type));
        if (selectedItemChangeListener != null) {
            adapter.setSelectedItemChangeListener(selectedItemChangeListener);
        }
        binding.recView.setAdapter(adapter);
    }

    public OnChannelItemClickListener getListener() {
        return channelItemClickListener;
    }

    public void setListener(OnChannelItemClickListener listener) {
        this.channelItemClickListener = listener;
    }

    public void setSelectedItemChangeListener(OnSelectedItemChangeListener selectedItemChangeListener) {
        this.selectedItemChangeListener = selectedItemChangeListener;
    }

    public interface OnChannelItemClickListener {
        void onChannelClick(String itemId, String name, String type);
    }

    //
//    public interface OnAddChannelClickListener() {
//        void onAddChannelClick(String type);
//    }

    public interface OnSelectedItemChangeListener {
        void onChangeSelected(int position);
    }

    public void resetSelectItem() {
        adapter.setSelecteditem(-1);
    }

    public void selectItem(String id) {
        OrderedRealmCollection<Channel> channels = adapter.getData();
        int i = 0;
        for (Channel channel : channels) {
            if (channel.getId().equals(id)) {
                adapter.setSelecteditem(i);
                return;
            }
            i++;
        }
    }

    public void goToAddChannelsActivity() {
        getActivity().startActivityForResult(
                new Intent(getActivity().getApplicationContext(), AddExistingChannelsActivity.class),
                REQUEST_JOIN_CHANNEL);
    }

    private void createNewChannel() {
        getActivity().startActivityForResult(
                new Intent(getActivity().getApplicationContext(), CreateNewChGrActivity.class)
                    .putExtra(IS_CHANNEL, true),
                REQUEST_CREATE);
    }
}

