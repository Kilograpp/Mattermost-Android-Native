package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityAllMembersChannelBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.presenter.channel.AddMembersPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import java.util.List;

import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 01.11.16.
 */
@RequiresPresenter(AddMembersPresenter.class)
public class AddMembersActivity extends BaseActivity<AddMembersPresenter> {
    private static final String CHANNEL_ID = "CHANNEL_ID";

    ActivityAllMembersChannelBinding binding;
    AddMembersAdapter addMembersAdapter;
    SearchView searchView;

    private AddMembersAdapterNotRealm addMembersAdapterNotRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_members_channel);
        setToolbar();
        initiationData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        searchView = initSearchView(menu, getMassageTextWatcher());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public static void start(Context context, String channelId) {
        Intent starter = new Intent(context, AddMembersActivity.class);
        starter.putExtra(CHANNEL_ID, channelId);
        context.startActivity(starter);
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
                } else {
                    updateDataList(getPresenter().getMembers());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    public void requestMember(String s) {
        binding.recView.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        if (searchView.getQuery().length() == 0)
            updateDataList(getPresenter().getMembers());
        searchView.setIconified(true);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

//    public void updateDataList(OrderedRealmCollection<User> realmResult) {
//        addMembersAdapter.updateData(realmResult);
//        if (realmResult.size() == 0) {
//            binding.textViewListEmpty.setVisibility(View.VISIBLE);
//        } else
//            binding.textViewListEmpty.setVisibility(View.INVISIBLE);
//    }

    public void updateDataList(RealmResults<User> realmResult) {
        addMembersAdapterNotRealm.setUsersNotFromChannel(realmResult);
        binding.textViewListEmpty.setVisibility(
                realmResult.size() == 0 ? View.VISIBLE : View.GONE);
    }

//    private void initiationData() {
//        addMembersAdapter = new AddMembersAdapter(this, id -> {
//            getPresenter().addMember(id);
//            binding.recView.setVisibility(View.INVISIBLE);
//            binding.progressBar.setVisibility(View.VISIBLE);
//            hideKeyboard(this);
//        });
//        binding.recView.setAdapter(addMembersAdapter);
//        binding.recView.setLayoutManager(new LinearLayoutManager(this));
//        getPresenter().initPresenter(getIntent().getStringExtra(CHANNEL_ID));
//    }

    private void initiationData() {
//        getPresenter().initPresenter(getIntent().getStringExtra(CHANNEL_ID));

        addMembersAdapterNotRealm = new AddMembersAdapterNotRealm(id -> {
            getPresenter().addMember(id);
            binding.recView.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            hideKeyboard(this);
        }/*, getPresenter().getUsersNotInChannel()*/);

        binding.recView.setAdapter(addMembersAdapterNotRealm);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));

        getPresenter().initPresenter(getIntent().getStringExtra(CHANNEL_ID));
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.add_members_toolbar), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public void refreshAdapter(List<User> usersNotInChannel) {
        addMembersAdapterNotRealm.updateData(usersNotInChannel);
    }
}
