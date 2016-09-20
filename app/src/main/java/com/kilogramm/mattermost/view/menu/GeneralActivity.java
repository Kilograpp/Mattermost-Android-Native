package com.kilogramm.mattermost.view.menu;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.view.MenuItem;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityMenuBinding;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.service.websocket.WebSocketManager;
import com.kilogramm.mattermost.presenter.GeneralPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.chat.ChatFragmentMVP;
import com.kilogramm.mattermost.view.menu.channelList.MenuChannelListFragment;
import com.kilogramm.mattermost.view.menu.directList.MenuDirectListFragment;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 28.07.2016.
 */
@RequiresPresenter(GeneralPresenter.class)
public class GeneralActivity extends BaseActivity<GeneralPresenter> {

    private ActivityMenuBinding binding;
    MenuChannelListFragment channelListFragment;
    MenuDirectListFragment directListFragment;
    private String currentChannel = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu);
        setupMenu();
        setupRightMenu();

        MattermostService.Helper.create(this).startWebSocket();
    }

    private void setupRightMenu() {
        binding.logout.setOnClickListener(view -> getPresenter().logout());
    }

    private void setupMenu() {
        channelListFragment = new MenuChannelListFragment();
        directListFragment = new MenuDirectListFragment();

        directListFragment.setDirectItemClickListener((itemId, name) -> getPresenter().setSelectedDirect(itemId,name));

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentDirectList.getId(), directListFragment);
        fragmentTransaction.commit();

        //initChannelList
        channelListFragment.setListener((itemId, name) -> getPresenter().setSelectedChannel(itemId,name));

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

    public void setFragmentChat(String channelId, String channelName,boolean isChannel){
        replaceFragment(channelId,channelName);
        if(isChannel){
            directListFragment.resetSelectItem();
        }else{
            channelListFragment.resetSelectItem();
        }
    }

    private void replaceFragment(String channelId, String channelName){
        if(!channelId.equals(currentChannel)){
            ChatFragmentMVP fragmentMVP = ChatFragmentMVP.createFragment(channelId, channelName);
            currentChannel = channelId;
            getFragmentManager().beginTransaction()
                    .replace(binding.contentFrame.getId(), fragmentMVP)
                    .commit();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void start(Context context, Integer flags ) {
         Intent starter = new Intent(context, GeneralActivity.class);
         if (flags != null) {
             starter.setFlags(flags);
         }
         context.startActivity(starter);
    }
    public void showErrorText(String text){
        Toast.makeText(this, text,Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MattermostService.Helper.create(this).updateUserStatusNow();
    }
}
