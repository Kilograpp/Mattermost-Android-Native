package com.kilogramm.mattermost.view;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApplication;
import com.kilogramm.mattermost.adapters.MenuChannelsAdapter;
import com.kilogramm.mattermost.adapters.MenuDirectionProfileAdapter;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityMenuBinding;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.view.fragments.ChatFragment;
import com.kilogramm.mattermost.viewmodel.menu.MenuViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 28.07.2016.
 */
public class MenuActivity extends BaseActivity {

    private static final String TAG = "MenuActivity";

    private Realm realm;
    private ActivityMenuBinding binding;
    private MenuViewModel menuViewModel;
    private String myId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu);
        realm = Realm.getDefaultInstance();
        realm.setAutoRefresh(true);
        menuViewModel = new MenuViewModel(this);
        binding.setViewModel(menuViewModel);
        myId = realm.where(User.class).findFirst().getId();
        setupRecyclerView();
        setupRecyclerViewDirection();
        runBackgroundRefreshStatus();
    }

    private void runBackgroundRefreshStatus() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Realm realm = Realm.getDefaultInstance();
                MattermostApplication application = MattermostApplication.get(getApplicationContext());
                ApiMethod service = application.getMattermostRetrofitService();
                List<String> list = new ArrayList<>();
                RealmResults<Channel> channels = realm.where(Channel.class)
                        .isNull("type")
                        .findAll();
                for (Channel channel : channels) {
                    list.add(channel.getId());
                }
                realm.close();
                service.getStatus(list)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(Schedulers.io())
                        .subscribe(new Subscriber<Map<String,String>>() {
                            @Override
                            public void onCompleted() {
                                    Log.d("BACKGROUND_TASK", "Complete load status");
                                }
                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                Log.d("BACKGROUND_TASK", "Error");
                            }
                            @Override
                            public void onNext(Map<String,String> stringStringMap) {
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                RealmResults<Channel> channels = realm.getDefaultInstance()
                                        .where(Channel.class)
                                        .isNull("type")
                                        .findAll();
                                for (Channel channel : channels) {
                                    channel.setStatus(stringStringMap.get(channel.getId()));
                                }
                                realm.commitTransaction();
                                realm.close();
                            }
                        });
            }
        }, 0L, 60L*100);
    }

    private void setupRecyclerView() {
        RealmResults<Channel> results = realm.where(Channel.class)
                .equalTo("type", "O")
                .findAll();
        results.addChangeListener(element1 ->  menuViewModel.showListAndHideProgress());
        MenuChannelsAdapter adapter = new MenuChannelsAdapter(results, this);
        binding.recycleView.setAdapter(adapter);
        binding.recycleView.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(this, "ClickItem", Toast.LENGTH_SHORT).show();
            Channel item = adapter.getItem(position);
            ChatFragment fragment = ChatFragment.createFragment(item.getId(),item.getDisplayName());
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.contentFrame.getId(), fragment)
                    .commit();

        });
    }

    private void setupRecyclerViewDirection() {
        RealmResults<Channel> results = realm.where(Channel.class)
                .isNull("type")
                .findAllSorted("username", Sort.ASCENDING);
        MenuDirectionProfileAdapter adapter = new MenuDirectionProfileAdapter(results, this);
        binding.recycleViewDirect.setAdapter(adapter);
        binding.recycleViewDirect.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(this, "ClickItem", Toast.LENGTH_SHORT).show();
            try {
                Channel item = adapter.getItem(position);
                ChatFragment fragment = ChatFragment.createFragment(
                        realm.where(Channel.class)
                                .equalTo("name", myId + "__" + item.getId())
                                .or()
                                .equalTo("name", item.getId() + "__" + myId)
                                .findFirst()
                                .getId(), item.getUsername());
                getSupportFragmentManager().beginTransaction()
                        .replace(binding.contentFrame.getId(), fragment)
                        .commit();
            } catch (NullPointerException e){
                e.printStackTrace();
            }

        });
    }


     public static void start(Context context, Integer flags ) {
         Intent starter = new Intent(context, MenuActivity.class);
         if (flags != null) {
             starter.setFlags(flags);
         }
         context.startActivity(starter);
     }
}
