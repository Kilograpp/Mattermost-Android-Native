package com.kilogramm.mattermost.view.menu.directList;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentMenuDirectListBinding;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.presenter.MenuDirectListPresenter;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;
import com.kilogramm.mattermost.view.fragments.BaseFragment;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 23.08.2016.
 */

@RequiresPresenter(MenuDirectListPresenter.class)
public class MenuDirectListFragment extends BaseFragment<MenuDirectListPresenter> {

    private static final String TAG = "MenuDirectListFragment";

    private FragmentMenuDirectListBinding binding;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_menu_direct_list, container, false);
        View view = binding.getRoot();

        binding.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPresenter().onMoreClick();
            }
        });

        setupRecyclerViewDirection();

        return view;
    }

    public void goToDirectListActivity() {
        startActivity(new Intent(getActivity(), WholeDirectListActivity.class));
    }

    private void setupRecyclerViewDirection() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Channel> results = realm.where(Channel.class)
                .isNull("type")
                .findAllSorted("username", Sort.ASCENDING);

        binding.recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new AdapterMenuDirectList(getActivity(), results, binding.recView,
                (itemId, name) -> {
                    directItemClickListener.onDirectClick(itemId, name);
                });

        if (selectedItemChangeListener != null) {
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
        if (adapter != null) {
            adapter.setSelectedItemChangeListener(selectedItemChangeListener);
        }
    }

    public interface OnDirectItemClickListener {
        void onDirectClick(String itemId, String name);
    }

    public interface OnSelectedItemChangeListener {
        void onChangeSelected(int position);
    }

    public void resetSelectItem() {
        adapter.setSelecteditem(-1);
    }
}
