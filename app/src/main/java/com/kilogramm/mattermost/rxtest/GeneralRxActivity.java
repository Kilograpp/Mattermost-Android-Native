package com.kilogramm.mattermost.rxtest;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityMenuBinding;
import com.kilogramm.mattermost.model.entity.SaveData;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.addchat.AddExistingChannelsActivity;
import com.kilogramm.mattermost.view.authorization.ChooseTeamActivity;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;
import com.kilogramm.mattermost.view.menu.channelList.MenuChannelListFragment;
import com.kilogramm.mattermost.view.menu.directList.MenuDirectListFragment;
import com.kilogramm.mattermost.view.menu.pivateList.MenuPrivateListFragment;
import com.kilogramm.mattermost.view.search.SearchMessageActivity;
import com.kilogramm.mattermost.view.settings.NotificationActivity;
import com.squareup.picasso.Picasso;

import java.util.Enumeration;

import io.realm.Realm;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 05.10.2016.
 */
@RequiresPresenter(GeneralRxPresenter.class)
public class GeneralRxActivity extends BaseActivity<GeneralRxPresenter> {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "GeneralRxActivity";

    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

    private ActivityMenuBinding binding;
    MenuChannelListFragment channelListFragment;
    MenuPrivateListFragment privateListFragment;
    MenuDirectListFragment directListFragment;

    private String currentChannel = "";
    private String searchMessageId;
    private SaveData saveData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu);
        setupMenu();
        setupRightMenu();
        MattermostService.Helper.create(this).startWebSocket();
    }

    public String getAvatarUrl() {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/users/"
                + MattermostPreference.getInstance().getMyUserId()
                + "/image";
    }

    private void setupRightMenu() {
        binding.profile.setOnClickListener(view -> ProfileRxActivity.start(this,
                MattermostPreference.getInstance().getMyUserId()));
        binding.headerUsername.setText(
                UserRepository
                        .query(new UserRepository.UserByIdSpecification(MattermostPreference.getInstance()
                                .getMyUserId()))
                        .first()
                        .getUsername()
        );
        Picasso.with(this)
                .load(getAvatarUrl())
                .error(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                .placeholder(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                .into(binding.headerPicture);
        binding.navView.setNavigationItemSelectedListener(item -> {
            binding.drawerLayout.closeDrawer(GravityCompat.END);

            switch (item.getItemId()) {
                case R.id.switch_team:
                    getPresenter().requestSwitchTeam();
                    break;
                case R.id.files:
                    Toast.makeText(GeneralRxActivity.this, "In Development", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.settings:
                    NotificationActivity.start(this);
                    break;
                case R.id.invite_new_member:
                    InviteUserRxActivity.start(this);
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
        });
    }

    private void setupMenu() {
        channelListFragment = new MenuChannelListFragment();
        privateListFragment = new MenuPrivateListFragment();
        directListFragment = new MenuDirectListFragment();

        directListFragment.setDirectItemClickListener((itemId, name, type) -> getPresenter().setSelectedMenu(itemId, type, name));

        getFragmentManager().beginTransaction()
                .replace(binding.fragmentDirectList.getId(), directListFragment)
                .commit();

        privateListFragment.setPrivateItemClickListener((itemId, name, type) -> getPresenter().setSelectedMenu(itemId, type, name));
        getSupportFragmentManager().beginTransaction()
                .replace(binding.fragmentPrivateList.getId(), privateListFragment)
                .commit();
        //initChannelList
        channelListFragment.setListener((itemId, name, type) -> getPresenter().setSelectedMenu(itemId, type, name));

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

    public void setSelectItemMenu(String id, String typeChannel) {
        switch (typeChannel) {
            case "O":
                channelListFragment.selectItem(id);
                break;
            case "D":
                directListFragment.selectItem(id);
                break;
            case "P":
                privateListFragment.selectItem(id);
                break;
        }
    }

    public void setFragmentChat(String channelId, String channelName, String type) {
        replaceFragment(channelId, channelName);
        switch (type) {
            case "O":
                directListFragment.resetSelectItem();
                privateListFragment.resetSelectItem();
                break;
            case "D":
                channelListFragment.resetSelectItem();
                privateListFragment.resetSelectItem();
                break;
            case "P":
                directListFragment.resetSelectItem();
                channelListFragment.resetSelectItem();
                break;
        }
        MattermostPreference.getInstance().setLastChannelId(channelId);
    }

    private void replaceFragment(String channelId, String channelName) {
        if (!channelId.equals(currentChannel)) {
            ChatRxFragment rxFragment = ChatRxFragment.createFragment(channelId, channelName, searchMessageId);
            currentChannel = channelId;getFragmentManager().beginTransaction()
                    .replace(binding.contentFrame.getId(), rxFragment, FRAGMENT_TAG)
                    .commit();
        } else {
            if(searchMessageId != null){
                ChatRxFragment rxFragment = ChatRxFragment.createFragment(channelId, channelName, searchMessageId);
                currentChannel = channelId;getFragmentManager().beginTransaction()
                        .replace(binding.contentFrame.getId(), rxFragment, FRAGMENT_TAG)
                        .commit();
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START);
    }

    public static void start(Context context, Integer flags) {
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


    public void showTeemChoose() {
        ChooseTeamActivity.start(this);
    }

    public void showErrorText(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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
        if (resultCode == RESULT_OK) {
            if (requestCode == MenuDirectListFragment.REQUEST_CODE) {
                String userTalkToId = data.getStringExtra(WholeDirectListActivity.USER_ID);

                SaveData saveData = new SaveData(
                        userTalkToId,
                        MattermostPreference.getInstance().getMyUserId(),
                        true,
                        "direct_channel_show");

                Realm realm = Realm.getDefaultInstance();
                String myId = MattermostPreference.getInstance().getMyUserId();
                RealmResults<Channel> channels = realm.where(Channel.class)
                        .equalTo("name", myId + "__" + userTalkToId)
                        .or()
                        .equalTo("name", userTalkToId + "__" + myId)
                        .findAll();
                realm.close();

                if (channels.size() == 0) {
                    getPresenter().requestSaveData(saveData, userTalkToId);
                } else {
                    this.setFragmentChat(
                            channels.get(0).getId(),
                            channels.get(0).getUsername(),
                            channels.get(0).getType());
                }
            }
            if (requestCode == ChatRxFragment.SEARCH_CODE) {
                if (data != null) {
                    searchMessageId = data.getStringExtra(SearchMessageActivity.MESSAGE_ID);
                    this.setFragmentChat(
                            data.getStringExtra(SearchMessageActivity.CHANNEL_ID),
                            data.getStringExtra(SearchMessageActivity.CHANNEL_NAME),
                            data.getStringExtra(SearchMessageActivity.TYPE_CHANNEL));
                }
            }
            if (requestCode == MenuChannelListFragment.REQUEST_JOIN_CHANNEL) {
                getPresenter().requestAddChat(data.getStringExtra(AddExistingChannelsActivity.CHANNEL_ID));
            }
        }
    }
}
