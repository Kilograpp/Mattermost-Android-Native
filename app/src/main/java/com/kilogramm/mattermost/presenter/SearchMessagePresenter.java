package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.SearchResult;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.view.search.SearchMessageActivity;

import io.realm.Realm;
import nucleus.presenter.Presenter;
import rx.Observable;
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

    private String searchString = "";

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();
    }

    @Override
    protected void onTakeView(SearchMessageActivity searchMessageActivity) {
        super.onTakeView(searchMessageActivity);
    }

    public void search(String teamId, String terms) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }

        getView().hideKeyboard(getView());

        mSubscription = service.searchForPosts(teamId, terms)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SearchResult>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "Error get search");
                    }

                    @Override
                    public void onNext(SearchResult searchResult) {
                        Log.d(TAG, "OnNext");
                    }
                });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH){
            getView().test();
        } else {
            return false;
        }
        return true;
    }
}
