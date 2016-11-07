package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.MembersListBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.presenter.channel.AddMembersPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import io.realm.OrderedRealmCollection;
import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 01.11.16.
 */
@RequiresPresenter(AddMembersPresenter.class)
public class AddMembersActivity extends BaseActivity<AddMembersPresenter> {
    private static final String CHANNEL_ID = "channel_id";

    MembersListBinding binding;
    AddMembersAdapter addMembersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.members_list);
        setToolbar();
        initiationData();
    }

    private void initiationData() {
        addMembersAdapter = new AddMembersAdapter(this,
                id -> {
                    getPresenter().addMember(id);
                    binding.list.setVisibility(View.INVISIBLE);
                    binding.progressBar.setVisibility(View.VISIBLE);
                    hideKeyboard(this);
                });
        binding.list.setAdapter(addMembersAdapter);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.searchText.addTextChangedListener(getMassageTextWatcher());

        binding.btnClear.setOnClickListener(view -> {
            binding.searchText.setText("");
            hideKeyboard(this);
        });
        getPresenter().initPresenter(getIntent().getStringExtra(CHANNEL_ID));
    }


    public TextWatcher getMassageTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() > 0) {
                    updateDataList(getPresenter().getMembers(charSequence.toString()));
                    binding.btnClear.setVisibility(View.VISIBLE);
                } else {
                    updateDataList(getPresenter().getMembers());
                    binding.btnClear.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    public void requestMember() {
        binding.list.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        binding.searchText.setText("");
    }

    public void updateDataList(OrderedRealmCollection<User> realmResult) {
        addMembersAdapter.updateData(realmResult);
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.add_members_toolbar), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public static void start(Context context, String channelId) {
        Intent starter = new Intent(context, AddMembersActivity.class);
        starter.putExtra(CHANNEL_ID, channelId);
        context.startActivity(starter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
