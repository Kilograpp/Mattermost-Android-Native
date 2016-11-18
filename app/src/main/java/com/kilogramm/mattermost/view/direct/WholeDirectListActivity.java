package com.kilogramm.mattermost.view.direct;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityWholeDirectListBinding;
import com.kilogramm.mattermost.model.entity.Preference.PreferenceRepository;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.kilogramm.mattermost.presenter.WholeDirectListPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import io.realm.OrderedRealmCollection;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 14.09.16.
 */
@RequiresPresenter(WholeDirectListPresenter.class)
public class WholeDirectListActivity extends BaseActivity<WholeDirectListPresenter> {
    public static final String USER_ID = "USER_ID";

    private ActivityWholeDirectListBinding binding;
    private WholeDirectListAdapter adapter;

    private MenuItem doneItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_whole_direct_list);
        init();
        setRecycleView();
    }

    private void init() {
        setupToolbar(getString(R.string.title_direct_list), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);

        binding.searchText.addTextChangedListener(getMassageTextWatcher());
        binding.btnClear.setOnClickListener(view -> {
            binding.searchText.setText("");
            hideKeyboard(this);
        });
        setRecycleView();
        getPresenter().getUsers();
    }

    public TextWatcher getMassageTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() > 0) {
                    getPresenter().getSearchUsers(charSequence.toString());
                    binding.btnClear.setVisibility(View.VISIBLE);
                } else {
                    getPresenter().getSearchUsers(null);
                    binding.btnClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    public void updateDataList(OrderedRealmCollection<User> realmResult) {
        adapter.updateData(realmResult);
        if (realmResult.size() == 0) {
            binding.listEmpty.setVisibility(View.VISIBLE);
        } else
            binding.listEmpty.setVisibility(View.INVISIBLE);
    }

    public void setRecycleView() {
        RealmResults<UserStatus> statusRealmResults = UserStatusRepository
                .query(new UserStatusRepository.UserStatusAllSpecification());
        RealmResults<Preferences> preferences = PreferenceRepository
                .query(new PreferenceRepository
                        .PreferenceByCategorySpecification("direct_channel_show"));
        adapter = new WholeDirectListAdapter(this, statusRealmResults, preferences);
        binding.recViewDirect.setAdapter(adapter);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        binding.recViewDirect.setLayoutManager(manager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done, menu);
        return true;
    }

    public void failSave() {
        adapter.getChangesMap().clear();
        adapter.notifyDataSetChanged();
        doneItem.setVisible(true);
        binding.progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Unable to save", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_done:
                if (adapter.getChangesMap().size() == 0) {
                    getPresenter().savePreferences(adapter.getChangesMap());
                    doneItem = item;
                    doneItem.setVisible(false);
                    binding.progressBar.setVisibility(View.VISIBLE);
                } else
                    getPresenter();
                break;
        }
        return true;
    }
}