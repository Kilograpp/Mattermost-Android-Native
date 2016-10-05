package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.SearchParams;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.view.search.SearchMessageActivity;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import nucleus.presenter.Presenter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by melkshake on 03.10.16.
 */

public class SearchMessagePresenter extends Presenter<SearchMessageActivity> implements TextView.OnEditorActionListener {
    private static final String TAG = "SearchMessagePresenter";

    private MattermostApp mMattermostApp;
    private Subscription mSubscription;
    private ApiMethod service;

    private ArrayList<String> foundMessageId;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();
        foundMessageId = new ArrayList<>();
    }

    @Override
    protected void onTakeView(SearchMessageActivity searchMessageActivity) {
        super.onTakeView(searchMessageActivity);
    }

    public void search(String teamId, String terms) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }

        Realm realm = Realm.getDefaultInstance();
        SearchParams params = new SearchParams(terms, true);

        mSubscription = service.searchForPosts(teamId, params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Posts>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                        getView().setRecycleView(foundMessageId);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Posts searchResult) {
                        Log.d(TAG, "OnNext");

                        if (searchResult.getPosts().values().isEmpty()){
                            getView().noResults();
                        } else {
                            RealmList<Post> foundPosts = new RealmList<>();
                            realm.executeTransaction(realm1 -> {
                                foundPosts.addAll(searchResult.getPosts().values());
                                foundMessageId.addAll(searchResult.getPosts().keySet());
                                realm1.insertOrUpdate(foundPosts);
                                realm1.close();
                            });
                        }
                    }
                });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH){
            getView().hideKeyboard(getView());
            getView().doMessageSearch();
        } else {
            return false;
        }
        return true;
    }
}
