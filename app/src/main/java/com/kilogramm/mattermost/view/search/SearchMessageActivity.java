package com.kilogramm.mattermost.view.search;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivitySearchBinding;
import com.kilogramm.mattermost.model.entity.FoundMessagesIds;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.presenter.SearchMessagePresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 03.10.16.
 */

@RequiresPresenter(SearchMessagePresenter.class)
public class SearchMessageActivity extends BaseActivity<SearchMessagePresenter>
                                    implements TextView.OnEditorActionListener, View.OnClickListener {
    private static final String TEAM_ID = "team_id";

    private ActivitySearchBinding binding;
    private SearchMessageAdapter adapter;
    private Realm realm;

    private String teamId;

    String terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void init() {
        teamId = getIntent().getStringExtra(TEAM_ID);
        realm = Realm.getDefaultInstance();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.searchText.setOnEditorActionListener(this);
        binding.cancelBtn.setOnClickListener(this);
    }

    public void setRecycleView() {
        RealmQuery<Post> query = realm.where(Post.class);
        RealmResults<FoundMessagesIds> foundMessageId = realm.where(FoundMessagesIds.class).findAll();
        if (foundMessageId.size() != 0) {
            for (int i = 0; i < foundMessageId.size(); i++) {
                query.equalTo("id", foundMessageId.get(i).getMessageId()).or();
            }
        }

        binding.recViewSearchResultList.setVisibility(View.VISIBLE);
        binding.defaultContainer.setVisibility(View.GONE);

        adapter = new SearchMessageAdapter(this, query.findAll(), true);
        binding.recViewSearchResultList.setLayoutManager(new LinearLayoutManager(this));
        binding.recViewSearchResultList.setAdapter(adapter);
    }

    protected void cancelClick() {
        this.finish();
    }

    public void doMessageSearch() {
        terms = binding.searchText.getText().toString();
        if (terms.equals("")) {
            Toast.makeText(this, this.getResources().getString(R.string.empty_search), Toast.LENGTH_SHORT).show();
        } else {
            getPresenter().search(teamId, terms);
        }
    }

    public void ProgressBarVisibility(boolean isShow) {
        binding.progressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void NoResultsVisibility(boolean isShow) {
        binding.txtNoResults.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void DefaultVisibility(boolean isShow) {
        binding.defaultContainer.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void SearchResultVisibility(boolean isShow) {
        binding.recViewSearchResultList.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            this.doMessageSearch();
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        this.cancelClick();
    }
}
