package com.kilogramm.mattermost.view.direct;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityWholeDirectListBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.presenter.WholeDirectListPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 14.09.16.
 */
@RequiresPresenter(WholeDirectListPresenter.class)
public class WholeDirectListActivity extends BaseActivity<WholeDirectListPresenter> implements WholeDirectListAdapter.OnDirectItemClickListener {
    public static final String USER_ID = "USER_ID";
    public static final String NAME = "NAME";

    private ActivityWholeDirectListBinding binding;
    private WholeDirectListAdapter adapter;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.realm = Realm.getDefaultInstance();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_whole_direct_list);
        View view = binding.getRoot();
        init();
        setRecycleView();
    }

    private void init() {
        setupToolbar(getString(R.string.title_direct_list), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
        getPresenter().getProfilesForDirectMessage();
    }

    public void setRecycleView() {
        RealmResults<User> users = realm.where(User.class).isNotNull("id").isNotNull("email").findAllSorted("username");
        ArrayList<String> usersIds = new ArrayList<>();
        for (User user : users) {
            usersIds.add(user.getId());
        }

        adapter = new WholeDirectListAdapter(this, users, usersIds, getPresenter(), this);
        binding.recViewDirect.setAdapter(adapter);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        binding.recViewDirect.setLayoutManager(manager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void finishActivity() {
        finish();
    }

    @Override
    public void onDirectClick(String itemId, String name) {
        Intent intent = new Intent(this, WholeDirectListActivity.class)
                .putExtra(USER_ID, itemId)
                .putExtra(NAME, name);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
