package com.kilogramm.mattermost.rxtest.left_menu;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.database.DBHelper;
import com.kilogramm.mattermost.database.MattermostContentProvider;
import com.kilogramm.mattermost.database.repository.UsersRepository;
import com.kilogramm.mattermost.databinding.FragmentLeftMenuBinding;
import com.kilogramm.mattermost.model.UserMember;
import com.kilogramm.mattermost.model.entity.Preference.PreferenceRepository;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository.ChannelDirectByIdSpecification;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.model.entity.team.TeamRepository;
import com.kilogramm.mattermost.model.entity.user_v2.UserV2;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.rxtest.left_menu.adapters.ChannelListAdapter;
import com.kilogramm.mattermost.rxtest.left_menu.adapters.LMDAdapter;
import com.kilogramm.mattermost.rxtest.left_menu.adapters.PrivateListAdapter;
import com.kilogramm.mattermost.view.addchat.AddExistingChannelsActivity;
import com.kilogramm.mattermost.view.createChannelGroup.CreateNewChannelActivity;
import com.kilogramm.mattermost.view.createChannelGroup.CreateNewGroupActivity;
import com.kilogramm.mattermost.view.direct.WholeDirectListActivity;
import com.kilogramm.mattermost.view.fragments.BaseFragment;

import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;


@RequiresPresenter(LeftMenuRxPresenter.class)
public class LeftMenuRxFragmentV2 extends BaseFragment<LeftMenuRxPresenter> implements OnLeftMenuClickListener,
        SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "LeftMenuRxFragment";


    private final int LEFT_MENU_LOADER = 0;
    private static final int NOT_SELECTED = -1;

    public static final int REQUEST_CREATE_CHANNEL = 97;
    public static final int REQUEST_CREATE_GROUP = 96;
    public static final int REQUEST_JOIN_CHANNEL = 98;
    public static final int REQUEST_JOIN_DIRECT = 99;

    private FragmentLeftMenuBinding mBinding;

    private ChannelListAdapter mChannelListAdapter;
    private PrivateListAdapter mPrivateListAdapter;
    private LMDAdapter mAdapterDirectMenuLeft;

    private OnChannelChangeListener mListener;

    private RealmResults<Member> mMembers;
    private RealmResults<Preferences> mPreferences;
    private RealmResults<UserMember> mUserMembers;
    private RealmResults<UserStatus> mUserStatuses;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        createMockUsers();
        getLoaderManager().initLoader(LEFT_MENU_LOADER, null, this);
        getLoaderManager().getLoader(LEFT_MENU_LOADER).forceLoad();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_left_menu, container, false);
        View view = mBinding.getRoot();
        mPreferences = PreferenceRepository.query(new PreferenceRepository.ListDirectMenu());

        mBinding.leftSwipeRefresh.setOnRefreshListener(this);

        initView();
        //showFirstLoadingMenu();

        return view;
    }


    @Override
    public void onChannelClick(String itemId, String name, String type) {
        removeSelection(type);
        sendOnChange(itemId, name, type);
        MattermostPreference.getInstance().setLastChannelId(itemId);
    }

    @Override
    public void onCreateChannelClick(View view) {
        switch (view.getId()) {
            case R.id.addChannel:
                CreateNewChannelActivity.startActivityForResult(this, REQUEST_CREATE_CHANNEL);
                break;
            case R.id.addGroup:
                CreateNewGroupActivity.startActivityForResult(this, REQUEST_CREATE_GROUP);
                break;
        }
    }

    @Override
    public void onRefresh() {
//        getPresenter().requestUpdate();
        mBinding.leftSwipeRefresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBinding.leftSwipeRefresh.setRefreshing(false);
                new TeastTask().execute();
            }
        },1000);
    }

    public class TeastTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {

            String selection= null;
            for(int i=0;i<10000;i++){
                selection = addIdToSpecification(DBHelper.FIELD_COMMON_ID,"John");
                if(i%2 == 0) getActivity().getContentResolver().update(MattermostContentProvider.CONTENT_URI_USERS,
                        UserV2.getMockableUserData(null, i%3 == 0?UserV2.STATUS_ONLINE:UserV2.STATUS_OFFLINE, "true" ),DBHelper.FIELD_COMMON_ID+" = ?",new String[]{"John"});
                selection = addIdToSpecification(DBHelper.FIELD_COMMON_ID,"EOPCp");
                if(i%3 == 0) getActivity().getContentResolver().update(MattermostContentProvider.CONTENT_URI_USERS,
                        UserV2.getMockableUserData(null, i%2 == 0?UserV2.STATUS_ONLINE:UserV2.STATUS_OFFLINE, "true" ),DBHelper.FIELD_COMMON_ID+" = ?",new String[]{"EOPCp"});

                selection = addIdToSpecification(DBHelper.FIELD_COMMON_ID,"Irtep");
                if(i%12 == 0)getActivity().getContentResolver().update(MattermostContentProvider.CONTENT_URI_USERS,
                        UserV2.getMockableUserData(null,i%2 == 0?UserV2.STATUS_ONLINE:UserV2.STATUS_OFFLINE, "true" ),DBHelper.FIELD_COMMON_ID+" = ?",new String[]{"Irtep"});

                selection = addIdToSpecification(DBHelper.FIELD_COMMON_ID,"Ijerf");
                if(i%3 == 0)getActivity().getContentResolver().update(MattermostContentProvider.CONTENT_URI_USERS,
                        UserV2.getMockableUserData(null,i%2 == 0?UserV2.STATUS_ONLINE:UserV2.STATUS_OFFLINE, "false" ),DBHelper.FIELD_COMMON_ID+" = ?",new String[]{"Ijerf"});

                if(i%2 == 0) getActivity().getContentResolver().update(MattermostContentProvider.CONTENT_URI_USERS,
                        UserV2.getMockableUserData2(null,""+i, "true" ),DBHelper.FIELD_COMMON_ID+" = ?",new String[]{"John1"});

                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        private String addIdToSpecification(String name, String id) {
            return name + " = " + id;
        }
    };

    public void setSelectItemMenu(String id, String typeChannel) {
//        switch (typeChannel) {
//            case Channel.OPEN:
//                mChannelListAdapter.setSelectedItem(mChannelListAdapter.getPositionById(id));
//                break;
//            case Channel.PRIVATE:
//                mPrivateListAdapter.setSelectedItem(mPrivateListAdapter.getPositionById(id));
//                break;
//            case Channel.DIRECT:
//                mAdapterDirectMenuLeft.setSelectedItem(mAdapterDirectMenuLeft.getPositionById(id));
//                break;
//        }
    }

    public void initView() {
        //initTeamHeader();
        //initChannelList();
        //initPrivateList();
        initDirectList();
    }
    public void setRefreshAnimation(boolean isVisible) {
        mBinding.leftSwipeRefresh.setRefreshing(isVisible);
    }

    public RealmResults<Channel> getDirectChannelData() {
        return ChannelRepository.query(new ChannelRepository.ChannelListDirectMenu());
    }

    public void selectLastChannel() {
        RealmResults<Channel> channels = ChannelRepository.query(
                new ChannelRepository.ChannelByIdSpecification(MattermostPreference.getInstance().getLastChannelId()));
        if (channels.size() > 0) {
            Channel channel = channels.first();
            setSelectItemMenu(channel.getId(), channel.getType());
        }
    }

    public void setOnChannelChangeListener(OnChannelChangeListener listener) {
        this.mListener = listener;
    }

    public void invalidateDirect() {
        if( getView()!=null) getView().postDelayed(() -> {
//            mAdapterDirectMenuLeft.update();
            selectLastChannel();
            mBinding.frDirect.recView.invalidate();
        },200);

    }

    private void handleRequestJoinChannel(Intent data) {
        onChannelClick(data.getStringExtra(AddExistingChannelsActivity.sCHANNEL_ID),
                data.getStringExtra(AddExistingChannelsActivity.sCHANNEL_NAME),
                data.getStringExtra(AddExistingChannelsActivity.sTYPE));
        setSelectItemMenu(data.getStringExtra(AddExistingChannelsActivity.sCHANNEL_ID),
                data.getStringExtra(AddExistingChannelsActivity.sTYPE));
    }

    private void handleRequestJoinDirect(Intent data) {
        String userTalkToId = data.getStringExtra(WholeDirectListActivity.mUSER_ID);
        Preferences saveData = new Preferences(userTalkToId,
                MattermostPreference.getInstance().getMyUserId(),
                true,
                "direct_channel_show");
        RealmResults<Channel> channels = ChannelRepository.query(new ChannelDirectByIdSpecification(userTalkToId));
        if (channels.size() == 0) {
            getPresenter().requestSaveData(saveData, userTalkToId);
        } else {
            onChannelClick(channels.get(0).getId(),
                    channels.get(0).getUsername(),
                    channels.get(0).getType());
            setSelectItemMenu(data.getStringExtra(AddExistingChannelsActivity.sCHANNEL_ID),
                    data.getStringExtra(AddExistingChannelsActivity.sTYPE));
        }
    }

    private void handleRequestCreateGroup(Intent data) {
//        mPrivateListAdapter.setSelectedItem(
//                mPrivateListAdapter.getPositionById(data.getStringExtra(CreateNewGroupActivity.sCREATED_GROUP_ID)));
//        onChannelClick(data.getStringExtra(CreateNewGroupActivity.sCREATED_GROUP_ID),
//                data.getStringExtra(CreateNewGroupActivity.sGROUP_NAME),
//                data.getStringExtra(CreateNewGroupActivity.sTYPE));
    }

    private void handleRequestCreateChannel(Intent data) {
//        mChannelListAdapter.setSelectedItem(
//                mChannelListAdapter.getPositionById(data.getStringExtra(CreateNewChannelActivity.sCREATED_CHANNEL_ID)));
//        onChannelClick(data.getStringExtra(CreateNewChannelActivity.sCREATED_CHANNEL_ID),
//                data.getStringExtra(CreateNewChannelActivity.CHANNEL_NAME),
//                data.getStringExtra(CreateNewChannelActivity.sTYPE));
    }


    private void removeSelection(String type) {
//        switch (type) {
//            case OPEN:
//                mAdapterDirectMenuLeft.setSelectedItem(NOT_SELECTED);
//                mPrivateListAdapter.setSelectedItem(NOT_SELECTED);
//                break;
//            case PRIVATE:
//                mChannelListAdapter.setSelectedItem(NOT_SELECTED);
//                mAdapterDirectMenuLeft.setSelectedItem(NOT_SELECTED);
//                break;
//            case DIRECT:
//                mChannelListAdapter.setSelectedItem(NOT_SELECTED);
//                mPrivateListAdapter.setSelectedItem(NOT_SELECTED);
//                break;
//        }
    }

    private void sendOnChange(String itemId, String name, String type) {
        if (this.mListener != null) {
            this.mListener.onChange(itemId, name, type);
        }
    }


    private void initTeamHeader() {
        RealmResults<Team> teams = TeamRepository.query();
        for (Team item : teams) {
            if (item.getId().equals(MattermostPreference.getInstance().getTeamId())) {
                mBinding.leftMenuHeader.teamHeaderText.setText(item.getDisplayName());
            }
        }
    }

    private void initChannelList() {
        RealmResults<Channel> channels = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("O"));
        mBinding.frChannel.recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.frChannel.recView.setNestedScrollingEnabled(false);
        mBinding.frChannel.addChannel.setOnClickListener(this::onCreateChannelClick);
        mBinding.frChannel.btnMoreChannel.setOnClickListener(this::openMore);
        mChannelListAdapter = new ChannelListAdapter(channels, getActivity(), mMembers, this);
        mBinding.frChannel.recView.setAdapter(null);
    }

    private void initPrivateList() {
        RealmResults<Channel> channels = ChannelRepository.query(new ChannelRepository.ChannelByTypeSpecification("P"));
        mBinding.frPrivate.recView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.frPrivate.recView.setNestedScrollingEnabled(false);
        mBinding.frPrivate.addGroup.setOnClickListener(this::onCreateChannelClick);
        mPrivateListAdapter = new PrivateListAdapter(channels, getActivity(), mMembers, this);
        mBinding.frPrivate.recView.setAdapter(null);
    }

    private void initDirectList() {
        mBinding.frDirect.recView.setLayoutManager(new LeftMenuLayoutManager(getActivity()));
        mAdapterDirectMenuLeft = new LMDAdapter(null,new String[]{UsersRepository.FIELD_STATUS, UsersRepository.FIELD_USER_NAME},getActivity());
        mBinding.frDirect.recView.setAdapter(mAdapterDirectMenuLeft);
    }


    private void openMore(View view) {
//        switch (view.getId()) {
//            case R.id.btnMoreChannel:
//                AddExistingChannelsActivity.startActivityForResult(this, REQUEST_JOIN_CHANNEL);
//                break;
//            case R.id.btnMore:
//                WholeDirectListActivity.startActivityForResult(this, REQUEST_JOIN_DIRECT);
//                break;
//        }
    }


    public void showErrorLoading(String message) {
        showErrorScene(message);
    }

    private void showErrorScene(String message) {
        mBinding.errorText.setText(message);
        mBinding.frChannel.getRoot().setVisibility(View.GONE);
        mBinding.frPrivate.getRoot().setVisibility(View.GONE);
        mBinding.frDirect.getRoot().setVisibility(View.GONE);
        mBinding.errorLayout.setVisibility(View.VISIBLE);
        setRefreshAnimation(false);
    }

    public void showLeftMenu(){
        mBinding.frChannel.getRoot().setVisibility(View.VISIBLE);
        mBinding.frPrivate.getRoot().setVisibility(View.VISIBLE);
        mBinding.frDirect.getRoot().setVisibility(View.VISIBLE);
        setRefreshAnimation(false);
    }

    private void showFirstLoadingMenu(){
        mBinding.frChannel.getRoot().setVisibility(View.GONE);
        mBinding.frPrivate.getRoot().setVisibility(View.GONE);
        mBinding.frDirect.getRoot().setVisibility(View.GONE);
        mBinding.errorLayout.setVisibility(View.GONE);
        setRefreshAnimation(true);
    }


    private void createMockUsers(){
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("John", UserV2.STATUS_OFFLINE, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Bob", UserV2.STATUS_ONLINE, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Buddy", UserV2.STATUS_ONLINE, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Matt", UserV2.STATUS_AWAY, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Dhgfds", UserV2.STATUS_OFFLINE, "false" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("RYUI", UserV2.STATUS_ONLINE, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("EOPCp", UserV2.STATUS_AWAY, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Irtep", UserV2.STATUS_ONLINE, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Ijerf", UserV2.STATUS_ONLINE, "false" ));


        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("John1", UserV2.STATUS_OFFLINE, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Bob2", UserV2.STATUS_ONLINE, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Buddy3", UserV2.STATUS_ONLINE, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Matt4", UserV2.STATUS_AWAY, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Dhgfds5", UserV2.STATUS_OFFLINE, "false" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("RYUI6", UserV2.STATUS_ONLINE, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("EOPCp7", UserV2.STATUS_AWAY, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Irtep8", UserV2.STATUS_ONLINE, "true" ));
        getActivity().getContentResolver().insert(MattermostContentProvider.CONTENT_URI_USERS, UserV2.getMockableUserData("Ijerf9", UserV2.STATUS_ONLINE, "false" ));
    }

//    private void update


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i){
            case LEFT_MENU_LOADER:
                return new CursorLoader(getActivity(), MattermostContentProvider.CONTENT_URI_USERS, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapterDirectMenuLeft.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapterDirectMenuLeft.changeCursor(null);
    }
}
