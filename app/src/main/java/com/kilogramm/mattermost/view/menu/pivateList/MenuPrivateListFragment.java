package com.kilogramm.mattermost.view.menu.pivateList;

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
import com.kilogramm.mattermost.viewmodel.menu.FrMenuPrivateViewModel;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 24.08.2016.
 */

public class MenuPrivateListFragment extends Fragment {

    private FragmentMenuPrivateListBinding binding;
    private FrMenuPrivateViewModel viewModel;
    private OnPrivateItemClickListener privateItemClickListener;
    private OnSelectedItemChangeListener selectedItemChangeListener;
    private AdapterMenuPrivateList adapter;
    private Realm realm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_private_list,
                container, false);
        View view = binding.getRoot();
        viewModel = new FrMenuPrivateViewModel(getContext());
        binding.setViewModel(viewModel);
        setupListView();
        return view;
    }


    private void setupListView() {
        RealmResults<Channel> results = new ChannelRepository.ChannelByTypeSpecification("P").toRealmResults(realm);
        binding.recView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterMenuPrivateList(getContext(), results, binding.recView,
                (itemId, name, type) ->  privateItemClickListener.onPrivatelClick(itemId, name, type));
        if(selectedItemChangeListener!=null){
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

    public interface OnPrivateItemClickListener{
        void onPrivatelClick(String itemId, String name, String type);
    }
    public interface OnSelectedItemChangeListener{
        void onChangeSelected(int position);
    }

    public void resetSelectItem(){
        adapter.setSelecteditem(-1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(realm!=null && !realm.isClosed()){
            realm.close();
        }
    }
}

