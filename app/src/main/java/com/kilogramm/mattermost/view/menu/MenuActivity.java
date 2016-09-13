package com.kilogramm.mattermost.view.menu;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.MenuItem;

import com.kilogramm.mattermost.MattermostApplication;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityMenuBinding;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.network.websocket.WebSocketService;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.chat.ChatFragment;
import com.kilogramm.mattermost.view.menu.channelList.MenuChannelListFragment;
import com.kilogramm.mattermost.view.menu.directList.MenuDirectListFragment;
import com.kilogramm.mattermost.viewmodel.menu.MenuViewModel;
import com.neovisionaries.ws.client.WebSocketException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;
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

    private String currentChannel = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu);
        realm = Realm.getDefaultInstance();
        realm.setAutoRefresh(true);
        menuViewModel = new MenuViewModel(this);
        binding.setViewModel(menuViewModel);
        myId = realm.where(User.class).findFirst().getId();
        setupMenu();
        runBackgroundRefreshStatus();
        WebSocketService.with(getApplicationContext()).run();


    }

    private void setupMenu() {
        MenuChannelListFragment channelListFragment = new MenuChannelListFragment();
        MenuDirectListFragment directListFragment = new MenuDirectListFragment();

        //initDirectList
        directListFragment.setDirectItemClickListener((itemId, name) -> replaceFragment(realm.where(Channel.class)
                .equalTo("name", myId + "__" + itemId)
                .or()
                .equalTo("name", itemId + "__" + myId)
                .findFirst()
                .getId(), name));
        directListFragment.setSelectedItemChangeListener(position -> {if(position != -1) channelListFragment.resetSelectItem();});

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentDirectList.getId(), directListFragment);
        fragmentTransaction.commit();

        //initChannelList
        channelListFragment.setListener((itemId, name) -> replaceFragment(itemId, name));

        channelListFragment.setSelectedItemChangeListener(position -> {if(position != -1) directListFragment.resetSelectItem();});

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentChannelList.getId(), channelListFragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
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
                        .observeOn(Schedulers.newThread())
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
        }, 0L, 10L*1000);
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            WebSocketService.with(getApplicationContext()).reconnect();
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    private void replaceFragment(String channelId, String channelName){
        if(!channelId.equals(currentChannel)){
            ChatFragment fragment = ChatFragment.createFragment(channelId,channelName);
            currentChannel = channelId;
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.contentFrame.getId(), fragment)
                    .commit();
        }
    }

     public static void start(Context context, Integer flags ) {
         Intent starter = new Intent(context, MenuActivity.class);
         if (flags != null) {
             starter.setFlags(flags);
         }
         context.startActivity(starter);
     }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
