package com.kilogramm.mattermost.rxtest;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityMenuBinding;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.rxtest.left_menu.LeftMenuRxFragment;
import com.kilogramm.mattermost.rxtest.left_menu.OnChannelChangeListener;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.authorization.ChooseTeamActivity;
import com.kilogramm.mattermost.view.channel.ChannelActivity;
import com.kilogramm.mattermost.view.menu.RightMenuAboutAppActivity;
import com.kilogramm.mattermost.view.search.SearchMessageActivity;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import icepick.State;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

import static com.kilogramm.mattermost.service.ManagerBroadcast.CHANNEL_ID;
import static com.kilogramm.mattermost.service.ManagerBroadcast.CHANNEL_NAME;
import static com.kilogramm.mattermost.service.ManagerBroadcast.CHANNEL_TYPE;

/**
 * Created by Evgeny on 05.10.2016.
 */
@RequiresPresenter(GeneralRxPresenter.class)
public class GeneralRxActivity extends BaseActivity<GeneralRxPresenter> implements OnChannelChangeListener {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "GeneralRxActivity";
    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

    private ActivityMenuBinding binding;

    private LeftMenuRxFragment leftMenuRxFragment;

    @State
    String currentChannel = "";

    private String searchMessageId;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu);
        setupImageLoaderConfiguration();
        setupMenu();
        setupRightMenu();
        showProgressBar();
        MattermostService.Helper.create(this).startWebSocket();
    }

    @Override
    protected void onStart() {
        super.onStart();
        parceIntent(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                binding.drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.openMenu:
                binding.drawerLayout.openDrawer(GravityCompat.END);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MattermostService.Helper.create(this).updateUserStatusNow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        user.removeChangeListeners();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED)
            if (requestCode == ChannelActivity.REQUEST_ID)
                ((ChatRxFragment) getFragmentManager().findFragmentById(binding.contentFrame.getId()))
                        .setChannelName(ChannelRepository
                                .query(new ChannelRepository.ChannelByIdSpecification(currentChannel))
                                .first()
                                .getDisplayName());

        if (resultCode == RESULT_OK) {
            if (requestCode == ChannelActivity.REQUEST_ID) {
                binding.progressBar.setVisibility(View.VISIBLE);
                getPresenter().setFirstChannelBeforeLeave();
            }
            if (requestCode == ChatRxFragment.SEARCH_CODE) {
                if (data != null) {
                    searchMessageId = data.getStringExtra(SearchMessageActivity.MESSAGE_ID);

                    setFragmentChat(data.getStringExtra(SearchMessageActivity.CHANNEL_ID),
                            data.getStringExtra(SearchMessageActivity.CHANNEL_NAME),
                            data.getStringExtra(SearchMessageActivity.TYPE_CHANNEL));
                }
            }
        }
    }

    private void setupImageLoaderConfiguration(){
        final int memoryCacheSize = 1024 * 1024 * 2;
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this.getApplicationContext())
                .memoryCache(new UsingFreqLimitedMemoryCache(memoryCacheSize)) // 2 Mb
//                .imageDownloader(new BaseImageDownloader(this, 15 * 1000, 30 * 1000)) // connectTimeout (15 s), readTimeout (30 s)
                .build();
        ImageLoader.getInstance().init(config);
    }

    public String getAvatarUrl() {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/users/"
                + MattermostPreference.getInstance().getMyUserId()
                + "/image?time="
                + user.getLastPictureUpdate();
    }

    private void setupRightMenu() {
        binding.profile.setOnClickListener(view -> ProfileRxActivity.start(this,
                MattermostPreference.getInstance().getMyUserId()));
        RealmResults users = UserRepository.query(
                new UserRepository.UserByIdSpecification(MattermostPreference.getInstance().getMyUserId()));
        if (users != null) {
            user = UserRepository.query(new UserRepository.UserByIdSpecification(MattermostPreference.getInstance().getMyUserId())).first();
            user.addChangeListener(element -> {
                Log.d(TAG, "OnChange users");
                updateHeaderUserName((User) element);
                setAvatar();
            });
            updateHeaderUserName(user);
            setAvatar();
        }
        binding.navView.setNavigationItemSelectedListener(item -> {
            binding.drawerLayout.closeDrawer(GravityCompat.END);

            switch (item.getItemId()) {
                case R.id.switch_team:
                    showTeemChoose();
                    break;
                case R.id.files:
                    showFiles();
                    break;
                case R.id.settings:
                    EditProfileRxActivity.start(this);
                    break;
                case R.id.invite_new_member:
                    InviteUserRxActivity.start(this);
                    break;
                // TODO раскомментировать когда появится дизайн
//                case R.id.help:
//                    Toast.makeText(GeneralRxActivity.this, getString(R.string.in_development), Toast.LENGTH_SHORT).show();
//                    break;
//                case R.id.report_a_problem:
//                    Toast.makeText(GeneralRxActivity.this, getString(R.string.in_development), Toast.LENGTH_SHORT).show();
//                    break;
                case R.id.about_mattermost:
                    RightMenuAboutAppActivity.start(this);
                    break;
                case R.id.logout:
                    showDialog(1);
                    break;
            }
            return false;
        });
    }

    private void setAvatar() {
        Map<String, String> headers = new HashMap();
        headers.put("Authorization", "Bearer " + MattermostPreference.getInstance().getAuthToken());
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                .showImageOnFail(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .extraForDownloader(headers)
                .considerExifParams(true)
                .build();

        ImageLoader.getInstance().displayImage(getAvatarUrl(), binding.headerPicture, options);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_custom_exit, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        dialogBuilder.setView(dialogView);

        return dialogBuilder.create();
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case 1:
                final AlertDialog alertDialog = (AlertDialog) dialog;
                Button cancelButton = (Button) alertDialog.findViewById(R.id.log_out);
                if (cancelButton != null) {
                    cancelButton.setOnClickListener(v -> getPresenter().requestLogout());
                    alertDialog.cancel();
                }
                break;
        }
    }

    private void updateHeaderUserName(User user) {
        binding.headerUsername.setText(String.format("@ %s", user.getUsername()));
    }

    private void showFiles() {
        Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        if (intent.resolveActivityInfo(MattermostApp.getSingleton()
                .getApplicationContext().getPackageManager(), 0) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this,
                    getString(R.string.no_suitable_app),
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void setupMenu() {
        leftMenuRxFragment = new LeftMenuRxFragment();
        getFragmentManager().beginTransaction()
                .replace(binding.leftContainer.getId(), leftMenuRxFragment)
                .commit();
        leftMenuRxFragment.setOnChannelChangeListener(this);
    }

    public void closeProgressBar() {
        binding.progressBar.setVisibility(View.GONE);
    }

    public void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    public void setFragmentChat(String channelId, String channelName, String type) {
        replaceFragment(channelId, channelName);
        Log.d(TAG, "setFragmentChat");
        closeProgressBar();
        leftMenuRxFragment.onChannelClick(channelId, channelName, type);
        leftMenuRxFragment.setSelectItemMenu(channelId, type);
    }

    private void replaceFragment(String channelId, String channelName) {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        closeProgressBar();
        if (MattermostPreference.getInstance().getLastChannelId() != null &&
                !MattermostPreference.getInstance().getLastChannelId().equals(channelId)) {
            FileToAttachRepository.getInstance().deleteUploadedFiles();
        }
        if (!channelId.equals(currentChannel) || searchMessageId != null) {
            ChatRxFragment rxFragment = ChatRxFragment.createFragment(channelId, channelName, searchMessageId);
            currentChannel = channelId;
            getFragmentManager().beginTransaction()
                    .replace(binding.contentFrame.getId(), rxFragment, FRAGMENT_TAG)
                    .commit();
            MattermostPreference.getInstance().setLastChannelId(channelId);
        }
        if (searchMessageId != null) {
            searchMessageId = null;
        }
    }

    public static void start(Context context, Integer flags) {
        Intent starter = new Intent(context, GeneralRxActivity.class);
        if (flags != null) {
            starter.setFlags(flags);
        }
        context.startActivity(starter);
    }

    private boolean parceIntent(Intent intent) {
        if (intent.getExtras() != null) {
            String openChannelId = intent.getStringExtra(CHANNEL_ID);
            String openChannelName = intent.getStringExtra(CHANNEL_NAME);
            String openChannelType = intent.getStringExtra(CHANNEL_TYPE);
            this.setFragmentChat(openChannelId, openChannelName, openChannelType);
            return true;
        }
        return false;
    }

    public void showMainRxActivity() {
        MainRxActivity.start(this,
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    public void showTeemChoose() {
        FileToAttachRepository.getInstance().deleteUploadedFiles();
        ChooseTeamActivity.start(this);
    }

    public void showErrorText(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_general, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (Build.VERSION.SDK_INT >= 23 && fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onChange(String channelId, String name) {
        replaceFragment(channelId, name);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}