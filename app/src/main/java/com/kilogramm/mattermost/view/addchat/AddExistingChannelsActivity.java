package com.kilogramm.mattermost.view.addchat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityAllChatsBinding;
import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.kilogramm.mattermost.presenter.AddExistingChannelsPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 18.10.16.
 */

@RequiresPresenter(AddExistingChannelsPresenter.class)
public class AddExistingChannelsActivity
        extends BaseActivity<AddExistingChannelsPresenter>
        implements AddExistingChannelsAdapter.OnChannelItemClickListener {

    private ActivityAllChatsBinding mBinding;
    private AddExistingChannelsAdapter mAdapter;
    private Realm mRealm;

    public static final String sCHANNEL_ID = "CHANNEL_ID";
    public static final String sTYPE = "sTYPE";
    public static final String sCHANNEL_NAME = "CHANNEL_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_all_chats);
        init();
        setRecycleView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChannelItemClick(String joinChannelId, String channelName, String type) {
        getPresenter().requestAddChat(joinChannelId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    public void setProgress(boolean bool) {
        mBinding.circProgressBar.setVisibility(bool ? View.VISIBLE : View.GONE);
    }

    public void setNoChannels(boolean bool) {
        mBinding.noMoreChannels.setVisibility(bool ? View.VISIBLE : View.GONE);
    }

    public void setRecycleView(boolean bool) {
        mBinding.recViewMoreChannels.setVisibility(bool ? View.VISIBLE : View.GONE);
    }

    public void finishActivity(String joinChannelId, String channelName, String type) {
        Intent intent = new Intent(this, AddExistingChannelsActivity.class)
                .putExtra(sCHANNEL_ID, joinChannelId)
                .putExtra(sCHANNEL_NAME, channelName)
                .putExtra(sTYPE, type);
        setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    private void setRecycleView() {
        RealmResults<ChannelsDontBelong> moreChannels = mRealm.where(ChannelsDontBelong.class)
                .findAll()
                .sort("name", Sort.ASCENDING);
        mAdapter = new AddExistingChannelsAdapter(this, moreChannels, true, this);
        mBinding.recViewMoreChannels.setAdapter(mAdapter);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mBinding.recViewMoreChannels.setLayoutManager(manager);
    }

    private void init() {
        setupToolbar(getString(R.string.title_existing_channels), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
        getPresenter().requestChannelsMore();
    }

    public static void startActivityForResult(Fragment fragment, Integer requestCode) {
        Intent starter = new Intent(fragment.getActivity(), AddExistingChannelsActivity.class);
        fragment.startActivityForResult(starter, requestCode);
    }
}
