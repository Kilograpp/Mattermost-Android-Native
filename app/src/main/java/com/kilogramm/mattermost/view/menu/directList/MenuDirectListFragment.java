package com.kilogramm.mattermost.view.menu.directList;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentMenuDirectListBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelByTypeSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusAllSpecification;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.viewmodel.menu.FrMenuDirectViewModel;

import io.realm.RealmResults;

/**
 * Created by Evgeny on 23.08.2016.
 */
public class MenuDirectListFragment extends Fragment {

    private FragmentMenuDirectListBinding binding;
    private FrMenuDirectViewModel viewModel;
    private OnDirectItemClickListener directItemClickListener;
    private OnSelectedItemChangeListener selectedItemChangeListener;
    AdapterMenuDirectList adapter;
    private int mSelectedItem;

    private UserRepository userRepository;
    private ChannelRepository channelRepository;
    private UserStatusRepository userStatusRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = new UserRepository();
        channelRepository = new ChannelRepository();
        userStatusRepository = new UserStatusRepository();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_direct_list,
                container, false);
        View view = binding.getRoot();
        viewModel = new FrMenuDirectViewModel(getContext());
        binding.setViewModel(viewModel);
        setupRecyclerViewDirection();
        return view;
    }

    private void setupRecyclerViewDirection() {
        RealmResults<UserStatus> statusRealmResults = userStatusRepository.query(new UserStatusAllSpecification());
        RealmResults<Channel> results = channelRepository.query(new ChannelByTypeSpecification(Channel.DIRECT));
        binding.recView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterMenuDirectList(getContext(), results, binding.recView,
                (itemId, name) -> {
                    directItemClickListener.onDirectClick(itemId, name);
                }, statusRealmResults);

        if(selectedItemChangeListener!=null){
            adapter.setSelectedItemChangeListener(selectedItemChangeListener);
        }
        binding.recView.setAdapter(adapter);

    }

    public OnDirectItemClickListener getDirectItemClickListener() {
        return directItemClickListener;
    }

    public void setDirectItemClickListener(OnDirectItemClickListener listener) {
        this.directItemClickListener = listener;
    }

    public OnSelectedItemChangeListener getSelectedItemChangeListener() {
        return selectedItemChangeListener;
    }

    public void setSelectedItemChangeListener(OnSelectedItemChangeListener selectedItemChangeListener) {
        this.selectedItemChangeListener = selectedItemChangeListener;
        if(adapter!=null) {
            adapter.setSelectedItemChangeListener(selectedItemChangeListener);
        }

    }


    public interface OnDirectItemClickListener{
        void onDirectClick(String itemId, String name);
    }

    public interface OnSelectedItemChangeListener{
        void onChangeSelected(int position);
    }

    public void resetSelectItem(){
        adapter.setSelecteditem(-1);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("UPDATE STATUS","");
        MattermostService.Helper.create(getContext()).updateUserStatusNow();
    }
}
