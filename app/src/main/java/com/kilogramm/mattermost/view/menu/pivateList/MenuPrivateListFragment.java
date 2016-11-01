package com.kilogramm.mattermost.view.menu.pivateList;

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
import com.kilogramm.mattermost.databinding.FragmentMenuPrivateListBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.view.createChannelGroup.CreateNewChGrActivity;

import io.realm.OrderedRealmCollection;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 24.08.2016.
 */

public class MenuPrivateListFragment extends Fragment {
    public static final int REQUEST_CREATE = 96;
    public final String IS_CHANNEL = "isChannel";

    private FragmentMenuPrivateListBinding binding;
    private OnPrivateItemClickListener privateItemClickListener;
    private OnSelectedItemChangeListener selectedItemChangeListener;
    private AdapterMenuPrivateList adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_private_list,
                container, false);
        View view = binding.getRoot();
        setupListView();
        binding.addGroup.setOnClickListener(v -> privateItemClickListener.onCreateGroupClick());

        return view;
    }

    private void setupListView() {
        RealmResults<Channel> results = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("P"));
        binding.recView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdapterMenuPrivateList(getContext(), results, binding.recView,
                new OnPrivateItemClickListener() {
                    @Override
                    public void onPrivatelClick(String itemId, String name, String type) {
                        privateItemClickListener.onPrivatelClick(itemId, name, type);
                    }

                    @Override
                    public void onCreateGroupClick() {
                    }
                });

        if (selectedItemChangeListener != null) {
            adapter.setSelectedItemChangeListener(selectedItemChangeListener);
        }
        binding.recView.setAdapter(adapter);
    }

    public OnPrivateItemClickListener getListener() {
        return privateItemClickListener;
    }

    public void setPrivateItemClickListener(OnPrivateItemClickListener listener) {
        this.privateItemClickListener = listener;
    }

    public void setSelectedItemChangeListener(OnSelectedItemChangeListener selectedItemChangeListener) {
        this.selectedItemChangeListener = selectedItemChangeListener;
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

    public interface OnPrivateItemClickListener {
        void onPrivatelClick(String itemId, String name, String type);
        void onCreateGroupClick();
    }

    public interface OnSelectedItemChangeListener {
        void onChangeSelected(int position);
    }

    public void resetSelectItem() {
        adapter.setSelecteditem(-1);
    }
}

