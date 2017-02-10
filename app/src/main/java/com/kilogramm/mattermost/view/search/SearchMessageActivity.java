package com.kilogramm.mattermost.view.search;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivitySearchBinding;
import com.kilogramm.mattermost.model.entity.FoundMessagesIds;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.presenter.SearchMessagePresenter;
import com.kilogramm.mattermost.rxtest.GeneralRxActivity;
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
        implements TextView.OnEditorActionListener,
        SearchMessageAdapter.OnJumpClickListener {

    private static final String TEAM_ID = "team_id";

    public static final String MESSAGE_ID = "message_id";
    public static final String CHANNEL_ID = "channel_id";
    public static final String CHANNEL_NAME = "channel_name";
    public static final String TYPE_CHANNEL = "type_channel";

    private ActivitySearchBinding mBinding;

    private Runnable mSearchDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            mBinding.searchAutoComplete.dismissDropDown();
            this.doMessageSearch(mBinding.searchAutoComplete.getText().toString());
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onJumpClick(String messageId, String channelId, String channelName, String typeChannel) {
        MattermostPreference.getInstance().setLastChannelId(channelId);
        GeneralRxActivity.startSearch(this, messageId);
    }

    public void init() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        mBinding.searchAutoComplete.setOnEditorActionListener(this);
        mBinding.searchAutoComplete.addTextChangedListener(dynamicalSearch);
        mBinding.searchAutoComplete.setOnFocusChangeListener((v, hasFocus) ->
                mBinding.btnClear.setVisibility(hasFocus ? View.VISIBLE : View.GONE));

        mBinding.btnBack.setOnClickListener(v -> finish());

        mBinding.btnClear.setOnClickListener(v -> {
            mBinding.searchAutoComplete.setText("");
            defaultMessageVisibility(true);
            searchResultVisibility(false);
            defaultVisibility(false);
            showKeyboard(this);
        });

        mSearchDelay = () -> doMessageSearch(mBinding.searchAutoComplete.getText().toString());
    }

    public void setRecycleView(String terms) {
        RealmQuery<Post> query = Realm.getDefaultInstance().where(Post.class);
        RealmResults<FoundMessagesIds> foundMessageId = Realm.getDefaultInstance().where(FoundMessagesIds.class).findAll();
        if (foundMessageId.size() != 0) {
            for (int i = 0; i < foundMessageId.size(); i++) {
                if (foundMessageId.size() > 1) {
                    query.equalTo("id", foundMessageId.get(i).getMessageId()).or();
                } else {
                    query.equalTo("id", foundMessageId.get(i).getMessageId());
                }
            }
        }

        mBinding.recViewSearchResultList.setVisibility(View.VISIBLE);
        mBinding.defaultContainer.setVisibility(View.GONE);

        SearchMessageAdapter mAdapter = new SearchMessageAdapter(this, query.findAll().sort("createAt", Sort.DESCENDING), true, this, terms);
        mBinding.recViewSearchResultList.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recViewSearchResultList.setAdapter(mAdapter);
    }

    public void doMessageSearch(String terms) {
        mBinding.searchAutoComplete.dismissDropDown();
        getPresenter().search(terms);
    }

    public void progressBarVisibility(boolean isShow) {
        mBinding.progressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void defaultVisibility(boolean isShow) {
        mBinding.defaultContainer.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void searchResultVisibility(boolean isShow) {
        mBinding.recViewSearchResultList.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void defaultMessageVisibility(boolean isShow) {
        mBinding.defaultMessage.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public static void startForResult(Activity context, String teamId, Integer id) {
        Intent starter = new Intent(context, SearchMessageActivity.class);
        starter.putExtra(TEAM_ID, teamId);
        context.startActivityForResult(starter, id);
    }

    private TextWatcher dynamicalSearch = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
                mBinding.searchAutoComplete.removeCallbacks(mSearchDelay);
                defaultMessageVisibility(true);
                searchResultVisibility(false);
            } else {
                mBinding.searchAutoComplete.removeCallbacks(mSearchDelay);
                int DELAY_SEARCH = 100;
                mBinding.searchAutoComplete.postDelayed(mSearchDelay, DELAY_SEARCH);
            }
        }
    };
}
