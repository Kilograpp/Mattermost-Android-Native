package com.kilogramm.mattermost.view.menu.directList;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentMenuDirectListBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.viewmodel.menu.FrMenuDirectViewModel;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Channel> results = realm.where(Channel.class)
                .isNull("type")
                .findAllSorted("username", Sort.ASCENDING);
        binding.recView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterMenuDirectList(getContext(), results, binding.recView,
                (itemId, name) -> {
                    directItemClickListener.onDirectClick(itemId, name);
                });

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
}
