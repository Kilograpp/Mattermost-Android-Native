package com.kilogramm.mattermost.view.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import io.realm.Sort;
import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 03.10.16.
 */

@RequiresPresenter(SearchMessagePresenter.class)
public class SearchMessageActivity extends BaseActivity<SearchMessagePresenter>
                                   implements TextView.OnEditorActionListener, SearchMessageAdapter.OnJumpClickListener {

    private static final String TEAM_ID = "team_id";
    public static final String MESSAGE_ID = "message_id";
    public static final String CHANNEL_ID = "channel_id";
    public static final String CHANNEL_NAME = "channel_name";
    public static final String IS_CHANNEL = "is_channel";

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
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnClear.setOnClickListener(v -> {
            binding.searchText.setText("");
            DefaultMessageVisibility(true);
            SearchResultVisibility(false);
            DefaultVisibility(false);
        });
    }

    public void setRecycleView(String terms) {
        RealmQuery<Post> query = realm.where(Post.class);
        RealmResults<FoundMessagesIds> foundMessageId = realm.where(FoundMessagesIds.class).findAll();
        if (foundMessageId.size() != 0) {
            for (int i = 0; i < foundMessageId.size(); i++) {
                if (foundMessageId.size() > 1) {
                    query.equalTo("id", foundMessageId.get(i).getMessageId()).or();
                } else {
                    query.equalTo("id", foundMessageId.get(i).getMessageId());
                }
            }
        }

        binding.recViewSearchResultList.setVisibility(View.VISIBLE);
        binding.defaultContainer.setVisibility(View.GONE);

        adapter = new SearchMessageAdapter(this, query.findAll().sort("createAt", Sort.DESCENDING), true, this, terms);
        binding.recViewSearchResultList.setLayoutManager(new LinearLayoutManager(this));
        binding.recViewSearchResultList.setAdapter(adapter);
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

    public void DefaultVisibility(boolean isShow) {
        binding.defaultContainer.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void SearchResultVisibility(boolean isShow) {
        binding.recViewSearchResultList.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void DefaultMessageVisibility(boolean isShow) {
        binding.defaultMessage.setVisibility(isShow ? View.VISIBLE : View.GONE);
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
    public void onJumpClick(String messageId, String channelId, String channelName, boolean isChannel) {
        Intent intent = new Intent(getApplicationContext(), SearchMessageActivity.class)
                .putExtra(MESSAGE_ID, messageId)
                .putExtra(CHANNEL_ID, channelId)
                .putExtra(CHANNEL_NAME, channelName)
                .putExtra(IS_CHANNEL, isChannel);
        setResult(RESULT_OK, intent);
        finish();
    }

    public static void startForResult(Activity context, String teamId, Integer id) {
        Intent starter = new Intent(context, SearchMessageActivity.class);
        starter.putExtra(TEAM_ID, teamId);
        context.startActivityForResult(starter,id);
    }
}
