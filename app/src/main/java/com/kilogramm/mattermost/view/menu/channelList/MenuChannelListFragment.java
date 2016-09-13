package com.kilogramm.mattermost.view.menu.channelList;

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
    private OnChannelItemClickListener channelItemClickListener;
    private OnSelectedItemChangeListener selectedItemChangeListener;
    private AdapterMenuChannelList adapter;

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
        binding.recView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterMenuChannelList(getContext(), results, binding.recView,
                (itemId, name) -> channelItemClickListener.onChannelClick(itemId, name));
        if(selectedItemChangeListener!=null){
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

    public interface OnChannelItemClickListener{
        void onChannelClick(String itemId, String name);
    }
    public interface OnSelectedItemChangeListener{
        void onChangeSelected(int position);
    }

    public void resetSelectItem(){
        adapter.setSelecteditem(-1);
    }

}

