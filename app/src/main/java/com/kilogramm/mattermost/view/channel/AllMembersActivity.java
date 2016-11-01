package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.MembersListBinding;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.presenter.channel.AllMembersPresenter;
import com.kilogramm.mattermost.presenter.channel.ChannelPresenter;
import com.kilogramm.mattermost.rxtest.ProfileRxActivity;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 01.11.16.
 */
@RequiresPresenter(ChannelPresenter.class)
public class AllMembersActivity extends BaseActivity<AllMembersPresenter> {
    MembersListBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.members_list);
        setToolbar();
        initiationData();
    }

    private void initiationData() {
        AllMembersAdapter allMembersAdapter = new AllMembersAdapter(this, UserRepository.query(),
                id -> ProfileRxActivity.start(this,id));
        binding.list.setAdapter(allMembersAdapter);
        binding.list.setLayoutManager(new LinearLayoutManager(this));

        binding.searchText.addTextChangedListener(getMassageTextWatcher());
    }

    public TextWatcher getMassageTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.all_members_toolbar), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, AllMembersActivity.class);
        context.startActivity(starter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
