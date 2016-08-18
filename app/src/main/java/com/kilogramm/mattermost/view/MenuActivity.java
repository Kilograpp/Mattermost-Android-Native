package com.kilogramm.mattermost.view;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.View;

import com.kilogramm.mattermost.MenuChannelsAdapter;
import com.kilogramm.mattermost.MenuDirectionProfileAdapter;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityMenuBinding;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.viewmodel.MenuViewModel;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Evgeny on 28.07.2016.
 */
public class MenuActivity extends BaseActivity {

    private Realm realm;
    private ActivityMenuBinding binding;
    private MenuViewModel menuViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu);
        realm = Realm.getDefaultInstance();
        menuViewModel = new MenuViewModel(this);
        binding.setViewModel(menuViewModel);
        setupRecyclerView();
        setupRecyclerViewDirection();

    }

    private void setupRecyclerView() {
        RealmResults<Channel> results = realm.where(Channel.class)
                .equalTo("type", "O")
                .findAll();
        results.addChangeListener(element1 ->  menuViewModel.showListAndHideProgress());
        binding.recycleView.setAdapter(new MenuChannelsAdapter(results, this));

    }

    private void setupRecyclerViewDirection() {
        RealmResults<Channel> results = realm.where(Channel.class)
                .isNull("type")
                .findAllSorted("username", Sort.ASCENDING);
        //results.addChangeListener(element1 ->  menuViewModel.showListAndHideProgress());
        binding.recycleViewDirect.setAdapter(new MenuDirectionProfileAdapter(results, this));
    }


     public static void start(Context context) {
        Intent starter = new Intent(context, MenuActivity.class);
        context.startActivity(starter);
    }
}
