package com.kilogramm.mattermost.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MenuItem;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityAllChatsBinding;
import com.kilogramm.mattermost.presenter.AddExistingChannelsPresenter;
import com.kilogramm.mattermost.presenter.WholeDirectListPresenter;

import io.realm.Realm;
import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 18.10.16.
 */

@RequiresPresenter(AddExistingChannelsPresenter.class)
public class AddExistingChannelsActivity extends BaseActivity<WholeDirectListPresenter> {

    private ActivityAllChatsBinding binding;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_chats);
        init();
        setRecycleView();
    }

    private void setRecycleView() {
    }

    private void init() {
        setupToolbar(getString(R.string.title_existing_channels), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
        getPresenter().getProfilesForDirectMessage();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
