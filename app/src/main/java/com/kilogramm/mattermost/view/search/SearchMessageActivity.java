package com.kilogramm.mattermost.view.search;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivitySearchBinding;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.presenter.SearchMessagePresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import io.realm.Realm;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 03.10.16.
 */

@RequiresPresenter(SearchMessagePresenter.class)
public class SearchMessageActivity extends BaseActivity <SearchMessagePresenter> {

    private ActivitySearchBinding binding;
    private SearchMessageAdapter adapter;
    Realm realm;

    private String terms;
    String teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
        this.teamId = realm.where(Team.class).findFirst().getId();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.searchText.setOnEditorActionListener(getPresenter());

        setRecycleView();
    }

    private void setRecycleView() {
        adapter = new SearchMessageAdapter(this, null, false);
        binding.recViewSearchResultList.setAdapter(adapter);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        binding.recViewSearchResultList.setLayoutManager(manager);
    }

    protected void cancelClick(){
        finish();
    }

    public void test(){
        Toast.makeText(this, "Search", Toast.LENGTH_LONG).show();

//        terms = binding.searchText.toString();
//        if (terms != null) {
//            getPresenter().search(teamId, terms);
//        } else {
//            Toast.makeText(this, "Search field cannot be empty", Toast.LENGTH_LONG).show();
//        }
    }
}
