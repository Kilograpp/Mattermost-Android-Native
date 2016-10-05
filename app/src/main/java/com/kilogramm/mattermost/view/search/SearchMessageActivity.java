package com.kilogramm.mattermost.view.search;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivitySearchBinding;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.presenter.SearchMessagePresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 03.10.16.
 */

@RequiresPresenter(SearchMessagePresenter.class)
public class SearchMessageActivity extends BaseActivity<SearchMessagePresenter> {
    private static final String TEAM_ID = "team_id";

    private ActivitySearchBinding binding;
    private SearchMessageAdapter adapter;
    private Realm realm;

    private String teamId;

    ArrayList<String> foundMessageId;
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
        binding.searchText.setOnEditorActionListener(getPresenter());
    }

    public void setRecycleView(ArrayList<String> messagesIds) {
        this.foundMessageId = messagesIds;

        RealmList<Post> foundPosts = new RealmList<>();
        for (String postId : foundMessageId) {
            foundPosts.add(realm.where(Post.class).equalTo("id", postId).findFirst());
        }

//        binding.recViewSearchResultList.setVisibility(View.VISIBLE);
//        binding.defaultContainer.setVisibility(View.GONE);

        adapter = new SearchMessageAdapter(this, foundPosts, true);
        binding.recViewSearchResultList.setAdapter(adapter);
        binding.recViewSearchResultList.setLayoutManager(new LinearLayoutManager(this));
    }

    protected void cancelClick() {
        this.finish();
    }

    public void noResults() {
        binding.txtNoResults.setVisibility(View.VISIBLE);
//        binding.defaultContainer.setVisibility(View.GONE);
        binding.recViewSearchResultList.setVisibility(View.GONE);
    }

    public void doMessageSearch() {
        terms = binding.searchText.getText().toString();
        if (terms.equals("")) {
            Toast.makeText(this, this.getResources().getString(R.string.empty_search), Toast.LENGTH_SHORT).show();
        } else {
            getPresenter().search(teamId, terms);
        }
    }
}
