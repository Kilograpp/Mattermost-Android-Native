package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.AllMembersBinding;
import com.kilogramm.mattermost.presenter.channel.AllMembersPresenter;
import com.kilogramm.mattermost.presenter.channel.ChannelPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 01.11.16.
 */
@RequiresPresenter(ChannelPresenter.class)
public class AllMembersActivity extends BaseActivity<AllMembersPresenter> {
    AllMembersBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.all_members);
        setToolbar();
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.members_toolbar), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, AllMembersActivity.class);
        context.startActivity(starter);
    }
}
