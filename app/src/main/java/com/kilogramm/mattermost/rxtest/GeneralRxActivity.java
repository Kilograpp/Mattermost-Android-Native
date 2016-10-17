package com.kilogramm.mattermost.rxtest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityMenuBinding;
import com.kilogramm.mattermost.model.entity.SaveData;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.chat.ChatFragmentMVP;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;
import com.kilogramm.mattermost.view.menu.channelList.MenuChannelListFragment;
import com.kilogramm.mattermost.view.menu.directList.MenuDirectListFragment;
import com.kilogramm.mattermost.view.search.SearchMessageActivity;

import icepick.Icepick;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 05.10.2016.
 */
@RequiresPresenter(GeneralRxPresenter.class)
public class GeneralRxActivity extends BaseActivity<GeneralRxPresenter> {
    private static final String TAG = "GeneralRxActivity";

    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private static final int SEARCH_CODE = 4;

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
        binding.rightMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                binding.drawerLayout.closeDrawer(GravityCompat.END);

                switch (item.getItemId()){
                    case R.id.switch_team:
                        Toast.makeText(GeneralRxActivity.this, "In Development", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.files:
                        Toast.makeText(GeneralRxActivity.this, "In Development", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.settings:
                        Toast.makeText(GeneralRxActivity.this, "In Development", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.invite_new_member:
                        Toast.makeText(GeneralRxActivity.this, "In Development", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.help:
                        Toast.makeText(GeneralRxActivity.this, "In Development", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.report_a_problem:
                        Toast.makeText(GeneralRxActivity.this, "In Development", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.about_mattermost:
                        Toast.makeText(GeneralRxActivity.this, "In Development", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.logout:
                        getPresenter().requestLogout();
                        break;
                }
                return false;
            }
        });
      //  binding.logout.setOnClickListener(view -> getPresenter().requestLogout());
    }

    private void setupMenu() {
        channelListFragment = new MenuChannelListFragment();
        directListFragment = new MenuDirectListFragment();

        directListFragment.setDirectItemClickListener((itemId, name) -> getPresenter().setSelectedDirect(itemId,name));

        getFragmentManager().beginTransaction()
                .replace(binding.fragmentDirectList.getId(), directListFragment)
                .commit();

        //initChannelList
        channelListFragment.setListener((itemId, name) -> getPresenter().setSelectedChannel(itemId,name));

        getSupportFragmentManager().beginTransaction()
                .replace(binding.fragmentChannelList.getId(), channelListFragment)
                .commit();
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
        MattermostPreference.getInstance().setLastChannelId(channelId);
    }

    private void replaceFragment(String channelId, String channelName){
        if(!channelId.equals(currentChannel)){
            ChatRxFragment rxFragment = ChatRxFragment.createFragment(channelId, channelName);
            currentChannel = channelId;
            getFragmentManager().beginTransaction()
                    .replace(binding.contentFrame.getId(), rxFragment, FRAGMENT_TAG)
                    .commit();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
    }

    public static void start(Context context, Integer flags ) {
        Intent starter = new Intent(context, GeneralRxActivity.class);
        if (flags != null) {
            starter.setFlags(flags);
        }
        context.startActivity(starter);
    }

    public void showMainRxActivity() {
        MainRxAcivity.start(this,
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    public void showErrorText(String text){
        Toast.makeText(this, text,Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MattermostService.Helper.create(this).updateUserStatusNow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (Build.VERSION.SDK_INT >= 23 && fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == MenuDirectListFragment.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                if (data != null && data.hasExtra(WholeDirectListActivity.NAME) && data.hasExtra(WholeDirectListActivity.USER_ID)) {
                    String name = data.getStringExtra(WholeDirectListActivity.NAME);
                    SaveData saveData = new SaveData(name, MattermostPreference.getInstance().getMyUserId(), true);
                    Log.d(TAG, "saveData constructor");
                    getPresenter().requestSaveData(saveData);
                }
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == SEARCH_CODE) {
            String messageId = data.getStringExtra(SearchMessageActivity.MESSAGE_ID);
            String channelId = data.getStringExtra(SearchMessageActivity.CHANNEL_ID);
            String channelName = data.getStringExtra(SearchMessageActivity.CHANNEL_NAME);
            boolean isChannel = data.getBooleanExtra(SearchMessageActivity.IS_CHANNEL, true);
            this.setFragmentChat(channelId, channelName, isChannel);
        }
    }

}
