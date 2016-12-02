package com.kilogramm.mattermost.rxtest;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.squareup.picasso.Picasso;

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
    private RealmChangeListener<User> userRealmChangeListener;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu);
        setupMenu();
        setupRightMenu();
        showProgressBar();
        MattermostService.Helper.create(this).startWebSocket();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        parceIntent(getIntent());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
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
                ((ChatRxFragment) getFragmentManager()
                        .findFragmentById(binding.contentFrame.getId()))
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
                            data.getStringExtra(SearchMessageActivity.TYPE_CHANNEL));/*
                    leftMenuRxFragment.setSelectItemMenu(data.getStringExtra(SearchMessageActivity.CHANNEL_ID),
                            data.getStringExtra(SearchMessageActivity.TYPE_CHANNEL));
                    leftMenuRxFragment.onChannelClick(data.getStringExtra(SearchMessageActivity.CHANNEL_ID),
                            data.getStringExtra(SearchMessageActivity.CHANNEL_NAME),
                            data.getStringExtra(SearchMessageActivity.TYPE_CHANNEL));*/
                }
            }
        }
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
            user.addChangeListener(userRealmChangeListener = element -> {
                Log.d(TAG, "OnChange users");
                updateHeaderUserName(element);
                Picasso.with(this)
                        .load(getAvatarUrl())
                        .error(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                        .placeholder(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                        .into(binding.headerPicture);
            });
            updateHeaderUserName(user);
            Picasso.with(this)
                    .load(getAvatarUrl())
                    .error(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                    .placeholder(this.getResources().getDrawable(R.drawable.ic_person_grey_24dp))
                    .into(binding.headerPicture);
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
                    //getPresenter().requestLogout();

                    showDialog(1);

//                    LayoutInflater layoutInflater = LayoutInflater.from(this);
//                    View customView = layoutInflater.inflate(R.layout.dialog_custom_exit, null);
//
//                    TextView exit = (TextView) customView.findViewById(R.id.log_out);
//                    exit.setOnClickListener(v -> getPresenter().requestLogout());
//
//                    android.support.v7.app.AlertDialog.Builder exitDialog =
//                            new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
//                    exitDialog.setView(R.layout.dialog_custom_exit);
//                    exitDialog.show();

                    break;
            }
            return false;
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialogDetails = null;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_custom_exit, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);
        dialogDetails = dialogBuilder.create();

        return dialogDetails;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case 1:
                final AlertDialog alertDialog = (AlertDialog) dialog;
                TextView cancelbutton = (TextView) alertDialog.findViewById(R.id.log_out);
                if (cancelbutton != null) {
                    cancelbutton.setOnClickListener(v -> Toast.makeText(getApplicationContext(),
                            "fdhglidfhsngtvhdsfugh", Toast.LENGTH_SHORT).show());
                }
                break;
        }
    }

        private void updateHeaderUserName (User user){
            binding.headerUsername.setText(String.format("@ %s", user.getUsername()));
        }

        private void showFiles () {
            Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            startActivity(intent);
        }

        private void setupMenu () {
            leftMenuRxFragment = new LeftMenuRxFragment();
            getFragmentManager().beginTransaction()
                    .replace(binding.leftContainer.getId(), leftMenuRxFragment)
                    .commit();
            leftMenuRxFragment.setOnChannelChangeListener(this);
        }

        public void closeProgressBar () {
            binding.progressBar.setVisibility(View.GONE);
        }

        public void showProgressBar () {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        public void setFragmentChat (String channelId, String channelName, String type){
            if (currentChannel.equals("")) {
                replaceFragment(channelId, channelName);
                leftMenuRxFragment.setSelectItemMenu(channelId, type);
            }
            Log.d(TAG, "setFragmentChat");
            closeProgressBar();
            leftMenuRxFragment.onChannelClick(channelId, channelName, type);
            leftMenuRxFragment.setSelectItemMenu(channelId, type);
        }

        private void replaceFragment (String channelId, String channelName){
            closeProgressBar();
            if (MattermostPreference.getInstance().getLastChannelId() != null &&
                    !MattermostPreference.getInstance().getLastChannelId().equals(channelId)) {
                // For clearing attached files on channel change
                FileToAttachRepository.getInstance().deleteUploadedFiles();
            }

            if (!channelId.equals(currentChannel)) {
                ChatRxFragment rxFragment = ChatRxFragment.createFragment(channelId, channelName, searchMessageId);
                currentChannel = channelId;
                getFragmentManager().beginTransaction()
                        .replace(binding.contentFrame.getId(), rxFragment, FRAGMENT_TAG)
                        .commit();
                MattermostPreference.getInstance().setLastChannelId(channelId);
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                if (searchMessageId != null) {
                    ChatRxFragment rxFragment = ChatRxFragment.createFragment(channelId, channelName, searchMessageId);
                    currentChannel = channelId;
                    getFragmentManager()
                            .beginTransaction()
                            .replace(binding.contentFrame.getId(), rxFragment, FRAGMENT_TAG)
                            .commit();
                    MattermostPreference.getInstance().setLastChannelId(channelId);
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    this.searchMessageId = null;
                }
            }
            if (searchMessageId != null) {
                searchMessageId = null;
            }
        }

        public static void start (Context context, Integer flags){
            Intent starter = new Intent(context, GeneralRxActivity.class);
            if (flags != null) {
                starter.setFlags(flags);
            }
            context.startActivity(starter);
        }

        private boolean parceIntent (Intent intent){
            if (intent.getExtras() != null) {
                String openChannelId = intent.getStringExtra(CHANNEL_ID);
                String openChannelName = intent.getStringExtra(CHANNEL_NAME);
                String openChannelType = intent.getStringExtra(CHANNEL_TYPE);
                this.setFragmentChat(openChannelId, openChannelName, openChannelType);
                return true;
            }
            return false;
        }

        public void showMainRxActivity () {
            MainRxActivity.start(this,
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        public void showTeemChoose () {
            FileToAttachRepository.getInstance().deleteUploadedFiles();
            ChooseTeamActivity.start(this);
        }

        public void showErrorText (String text){
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_general, menu);
            return true;
        }

        @Override
        public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
            if (Build.VERSION.SDK_INT >= 23 && fragment != null) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }

        @Override
        public void onChange (String channelId, String name){
            replaceFragment(channelId, name);
        }

        /**
         * ATTENTION: This was auto-generated to implement the App Indexing API.
         * See https://g.co/AppIndexing/AndroidStudio for more information.
         */
        public Action getIndexApiAction () {
            Thing object = new Thing.Builder()
                    .setName("GeneralRx Page") // TODO: Define a title for the content shown.
                    // TODO: Make sure this auto-generated URL is correct.
                    .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                    .build();
            return new Action.Builder(Action.TYPE_VIEW)
                    .setObject(object)
                    .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                    .build();
        }

        @Override
        public void onStop () {
            super.onStop();

            // ATTENTION: This was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            AppIndex.AppIndexApi.end(client, getIndexApiAction());
            client.disconnect();
        }
    }