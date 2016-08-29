package com.kilogramm.mattermost.view.menu;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.MenuChannelsAdapter;
import com.kilogramm.mattermost.databinding.FragmentMenuChannelListBinding;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.viewmodel.menu.FrMenuChannelViewModel;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 24.08.2016.
 */

public class MenuChannelListFragment extends Fragment {

    private FragmentMenuChannelListBinding binding;
    private FrMenuChannelViewModel viewModel;
    private OnChannelItemClickListener listener;

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
        viewModel = new FrMenuChannelViewModel(getContext());
        binding.setViewModel(viewModel);
        setupListView();
        return view;
    }


    private void setupListView() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Channel> results = realm.where(Channel.class)
                .equalTo("type", "O")
                .findAll();
        MenuChannelsAdapter adapter = new MenuChannelsAdapter(results, getContext());
        binding.listView.setAdapter(adapter);
        binding.listView.setOnItemClickListener((parent, view, position, id) -> {
            Channel item = adapter.getItem(position);
            if(listener!=null){
                listener.onChannelClick(item.getId(), item.getDisplayName());
            }
        });
    }

    public OnChannelItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnChannelItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnChannelItemClickListener{
        void onChannelClick(String itemId, String name);
    }
}

