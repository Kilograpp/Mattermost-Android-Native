package com.kilogramm.mattermost.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.FoundMessagesIds;
import com.kilogramm.mattermost.model.entity.SearchParams;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.network.ServerMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.search.SearchMessageActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmList;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 03.10.16.
 */

public class SearchMessagePresenter extends BaseRxPresenter<SearchMessageActivity> {

    public static final int REQUEST_SEARCH = 1;

    private MattermostApp mMattermostApp;

    private boolean isSearchEmpty;
    private boolean isOrSearch = true; //was by default

    @State
    String terms;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        restartableFirst(REQUEST_SEARCH,
                () -> ServerMethod.getInstance()
                        .searchForPosts(MattermostPreference.getInstance().getTeamId(), new SearchParams(terms, isOrSearch))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (searchMessageActivity, posts) -> {
                    if (posts.getPosts() == null) {
                        sendShowDefaultVisibility(true);
                        sendShowProgressBarVisibility(false);
                        isSearchEmpty = true;
                    } else {
                        RealmList<FoundMessagesIds> list = new RealmList<>();
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        for (String s : posts.getPosts().keySet()) {
                            list.add(new FoundMessagesIds(s));
                        }
                        realm.where(FoundMessagesIds.class).findAll().deleteAllFromRealm();
                        realm.insertOrUpdate(list);
                        realm.commitTransaction();
                        realm.close();

                        PostRepository.prepareAndAdd(posts);
                    }

                    if (!isSearchEmpty) {
                        sendSetRecyclerView(terms);
                        sendShowProgressBarVisibility(false);
                    }
                }, (searchMessageActivity1, throwable) -> {
                    sendShowProgressBarVisibility(false);
                    sendError(getError(throwable));
                    throwable.printStackTrace();
                });
    }

    private void sendShowProgressBarVisibility(Boolean bool) {
        createTemplateObservable(bool)
                .subscribe(split((searchMessageActivity, aBoolean) -> searchMessageActivity.progressBarVisibility(bool)));
    }

    private void sendShowSearchResultVisibility(Boolean bool) {
        createTemplateObservable(bool)
                .subscribe(split((searchMessageActivity, aBoolean) -> searchMessageActivity.searchResultVisibility(bool)));
    }

    private void sendShowDefaultVisibility(Boolean bool) {
        createTemplateObservable(bool)
                .subscribe(split((searchMessageActivity, aBoolean) -> searchMessageActivity.defaultVisibility(bool)));
    }

    private void sendShowDefaultMessageVisibility(Boolean bool) {
        createTemplateObservable(bool)
                .subscribe(split((searchMessageActivity, aBoolean) -> searchMessageActivity.defaultMessageVisibility(bool)));
    }

    private void sendHideKeyboard() {
        createTemplateObservable(new Object())
                .subscribe(split((searchMessageActivity, o) -> BaseActivity.hideKeyboard(searchMessageActivity)));
    }

    private void sendSetRecyclerView(String terms) {
        createTemplateObservable(terms)
                .subscribe(split(SearchMessageActivity::setRecycleView));
    }

    private void sendError(String error) {
        createTemplateObservable(error).subscribe(split((messageActivity, s) ->
                Toast.makeText(messageActivity, s, Toast.LENGTH_SHORT).show()));
    }

    public void search(String terms) {
        this.terms = terms;
        if (terms.contains(" ")) {
            isOrSearch = false;
        }
        this.isSearchEmpty = false;

        //sendHideKeyboard();
        sendShowProgressBarVisibility(true);
        sendShowSearchResultVisibility(false);
        sendShowDefaultVisibility(false);
        sendShowDefaultMessageVisibility(false);

        start(REQUEST_SEARCH);
    }

    public static void setSearchHistory(Context context, String key, ArrayList<String> history) {
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
        editor.apply();
    }

    public static ArrayList<String> getSearchHistory(Context context, String key) {
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
