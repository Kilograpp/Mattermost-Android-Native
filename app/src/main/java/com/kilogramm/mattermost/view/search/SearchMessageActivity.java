package com.kilogramm.mattermost.view.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivitySearchBinding;
import com.kilogramm.mattermost.model.entity.FoundMessagesIds;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.presenter.SearchMessagePresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

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
                                   SearchMessageAdapter.OnJumpClickListener,
                                   TextWatcher {

    private static final String TEAM_ID = "team_id";
    public static final String MESSAGE_ID = "message_id";
    public static final String CHANNEL_ID = "channel_id";
    public static final String CHANNEL_NAME = "channel_name";
    public static final String TYPE_CHANNEL = "type_channel";
    public static final String SEARCH_HISTORY = "search_history";

    private ActivitySearchBinding binding;
    private SearchMessageAdapter adapter;

    String terms;

    ArrayAdapter<String> historyAutoCompleteAdapter;
    ArrayList<String> historyAutoComplete;

    Display display;
    Point size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.searchAutoComplete.setOnEditorActionListener(this);
        binding.searchAutoComplete.addTextChangedListener(this);

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        binding.searchAutoComplete.setDropDownWidth(size.x);

        historyAutoComplete = new ArrayList<>();
        historyAutoComplete = getSearchHistory(getApplication(), SEARCH_HISTORY);
        historyAutoCompleteAdapter = new ArrayAdapter<>(
                SearchMessageActivity.this,
                R.layout.item_search_history_dropdown,
                historyAutoComplete);
        binding.searchAutoComplete.setAdapter(historyAutoCompleteAdapter);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnClear.setOnClickListener(v -> {
            binding.searchAutoComplete.setText("");
            defaultMessageVisibility(true);
            searchResultVisibility(false);
            defaultVisibility(false);
            showKeyboard(this);
        });
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

        binding.recViewSearchResultList.setVisibility(View.VISIBLE);
        binding.defaultContainer.setVisibility(View.GONE);

        adapter = new SearchMessageAdapter(this, query.findAll().sort("createAt", Sort.DESCENDING), true, this, terms);
        binding.recViewSearchResultList.setLayoutManager(new LinearLayoutManager(this));
        binding.recViewSearchResultList.setAdapter(adapter);
    }

    public void doMessageSearch() {
        terms = binding.searchAutoComplete.getText().toString();

        if (terms.equals("")) {
            Toast.makeText(this, this.getResources().getString(R.string.empty_search), Toast.LENGTH_SHORT).show();
        } else {
            if (!historyAutoComplete.contains(terms)) {
                historyAutoCompleteAdapter.add(terms);
                historyAutoComplete.add(terms);
            }
            setSearchHistory(getApplication(), SEARCH_HISTORY, historyAutoComplete);
            getPresenter().search(terms);
        }
    }
    //TODO можно было бы сделать один метод и кидать туда view
    public void progressBarVisibility(boolean isShow) {
        binding.progressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void defaultVisibility(boolean isShow) {
        binding.defaultContainer.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void searchResultVisibility(boolean isShow) {
        binding.recViewSearchResultList.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void defaultMessageVisibility(boolean isShow) {
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
    public void onJumpClick(String messageId, String channelId, String channelName, String typeChannel) {
        Intent intent = new Intent(getApplicationContext(), SearchMessageActivity.class)
                .putExtra(MESSAGE_ID, messageId)
                .putExtra(CHANNEL_ID, channelId)
                .putExtra(CHANNEL_NAME, channelName)
                .putExtra(TYPE_CHANNEL, typeChannel);
        setResult(RESULT_OK, intent);
        finish();
    }

    public static void startForResult(Activity context, String teamId, Integer id) {
        Intent starter = new Intent(context, SearchMessageActivity.class);
        starter.putExtra(TEAM_ID, teamId);
        context.startActivityForResult(starter, id);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    private void setSearchHistory(Context context, String key, ArrayList<String> history) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        JSONArray jsonArray = new JSONArray();

        for (String item : history) {
            jsonArray.put(item);
        }
        if (!history.isEmpty()) {
            editor.putString(key, jsonArray.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
    }

    private ArrayList<String> getSearchHistory(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = preferences.getString(key, null);
        ArrayList<String> storedHistory = new ArrayList<>();

        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String historyItem = jsonArray.optString(i);
                    storedHistory.add(historyItem);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return storedHistory;
    }
}
