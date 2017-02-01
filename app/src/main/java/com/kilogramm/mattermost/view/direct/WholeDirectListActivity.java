package com.kilogramm.mattermost.view.direct;

import android.app.Fragment;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityWholeDirectListBinding;
import com.kilogramm.mattermost.model.entity.Preference.PreferenceRepository;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.kilogramm.mattermost.presenter.WholeDirectListPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 14.09.16.
 */
@RequiresPresenter(WholeDirectListPresenter.class)
public class WholeDirectListActivity extends BaseActivity<WholeDirectListPresenter> {
    private static final String TAG = WholeDirectListActivity.class.getSimpleName();
    private ActivityWholeDirectListBinding mBinding;
    private WholeDirectListAdapter mAdapter;
    private MenuItem mDoneItem;
    private MenuItem mSearchItem;

    public static final String mUSER_ID = "mUSER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_whole_direct_list);
        init();
        setRecycleView();
        Log.d(TAG, "onCreate: " + getCurrentFocus());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_done:
                if (mAdapter.getmChangesMap().size() > 0) {
                    getPresenter().requestSavePreferences(mAdapter.getmChangesMap());
                    mDoneItem = item;
                    mDoneItem.setVisible(false);
                    mSearchItem.setVisible(false);
                    mBinding.progressBar.setVisibility(View.VISIBLE);
                } else
                    finish();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done, menu);
        mSearchItem = menu.findItem(R.id.action_search);
        initSearchView(menu, getMessageTextWatcher());
        return true;
    }

    public TextWatcher getMessageTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() > 0) {
                    getPresenter().getSearchUsers(charSequence.toString());
                } else {
                    getPresenter().getSearchUsers(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
    }

    public void updateDataList(List<User> thisTeamDirects) {
        RealmQuery<User> directUsers = Realm.getDefaultInstance().where(User.class);
        String currentUserId = MattermostPreference.getInstance().getMyUserId();

        for (int i = 0; i < thisTeamDirects.size(); i++) {
            if (!thisTeamDirects.get(i).getId().equals(currentUserId)
                    && !thisTeamDirects.get(i).getId().equals("null")) {
                if (thisTeamDirects.size() > 1) {
                    if(i==thisTeamDirects.size()-1){
                        directUsers.equalTo("id", thisTeamDirects.get(i).getId());
                    } else {
                        directUsers.equalTo("id", thisTeamDirects.get(i).getId()).or();
                    }
                } else {
                    directUsers.equalTo("id", thisTeamDirects.get(i).getId());
                }
            }
        }

        if (directUsers.findAllSorted("username", Sort.ASCENDING).size() == 0) {
            mBinding.listEmpty.setVisibility(View.VISIBLE);
        } else {
            mBinding.listEmpty.setVisibility(View.INVISIBLE);
            mAdapter.updateData(directUsers.findAllSorted("username", Sort.ASCENDING));
        }

    }

    public void setRecycleView() {
        RealmResults<UserStatus> statusRealmResults = UserStatusRepository
                .query(new UserStatusRepository.UserStatusAllSpecification());
        RealmResults<Preferences> preferences = PreferenceRepository
                .query(new PreferenceRepository
                        .PreferenceByCategorySpecification("direct_channel_show"));
        mAdapter = new WholeDirectListAdapter(this, statusRealmResults, preferences);
        mBinding.recViewDirect.setAdapter(mAdapter);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mBinding.recViewDirect.setLayoutManager(manager);
    }

    public void failSave() {
        mAdapter.getmChangesMap().clear();
        mAdapter.notifyDataSetChanged();
        mDoneItem.setVisible(true);
        mSearchItem.setVisible(true);
        mBinding.progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Unable to save", Toast.LENGTH_SHORT).show();
    }

    private void init() {
        setupToolbar(getString(R.string.title_direct_list), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
        setRecycleView();
        getPresenter().requestGetDirectUsers();
    }


    public static void startActivityForResult(Fragment fragment, Integer requestCode) {
        Intent starter = new Intent(fragment.getActivity(), WholeDirectListActivity.class);
        fragment.startActivityForResult(starter, requestCode);
    }
}