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
    private static final String TAG = "SearchMessageActivity";

    private ActivitySearchBinding binding;
    private SearchMessageAdapter adapter;
    private Realm realm;

    private String terms;
    private String teamId;
    private ArrayList<String> foundMessageId;
    private boolean isMessageIdsSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void init() {
        isMessageIdsSet = false;
        teamId = getIntent().getStringExtra(TEAM_ID);
        realm = Realm.getDefaultInstance();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.searchText.setOnEditorActionListener(getPresenter());
    }

    public void setRecycleView(ArrayList<String> messagesIds) {
        foundMessageId = messagesIds;
        isMessageIdsSet = true;

        RealmList<Post> foundPosts = new RealmList<>();
        for (String postId : foundMessageId) {
            foundPosts.add(realm.where(Post.class).equalTo("id", postId).findFirst());
        }

//        RealmResults<Post> foundPosts = realm.where(Post.class).contains("id", foundMessageId.get());

        if (isMessageIdsSet) {
            binding.recViewSearchResultList.setVisibility(View.VISIBLE);
            binding.defaultContainer.setVisibility(View.GONE);
            adapter = new SearchMessageAdapter(this, foundPosts/*, foundMessageId*/);
            binding.recViewSearchResultList.setAdapter(adapter);

            RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
            binding.recViewSearchResultList.setLayoutManager(manager);
        }
    }

    protected void cancelClick() {
        finish();
    }

    public void doMessageSearch() {
        Toast.makeText(this, "Search", Toast.LENGTH_LONG).show();

        terms = binding.searchText.getText().toString();
        if (terms != null) {
            getPresenter().search(teamId, terms);
        } else {
            Toast.makeText(this, "Search field cannot be empty", Toast.LENGTH_LONG).show();
        }
    }
}
