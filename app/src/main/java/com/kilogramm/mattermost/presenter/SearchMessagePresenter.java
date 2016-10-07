package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.FoundMessagesIds;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.SearchParams;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.view.search.SearchMessageActivity;

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

public class SearchMessagePresenter extends Presenter<SearchMessageActivity> {

    private MattermostApp mMattermostApp;
    private Subscription mSubscription;
    private ApiMethod service;

    private PostRepository postRepository;
    private boolean isSearchEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();
        postRepository = new PostRepository();
        this.isSearchEmpty = false;
    }

    @Override
    protected void onTakeView(SearchMessageActivity searchMessageActivity) {
        super.onTakeView(searchMessageActivity);
    }

    public void search(String teamId, String terms) {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }

        SearchParams params = new SearchParams(terms, true);
        getView().hideKeyboard(getView());
        getView().ProgressBarVisibility(true);
        getView().NoResultsVisibility(false);
        getView().SearchResultVisibility(false);
        getView().DefaultVisibility(false);

        mSubscription = service.searchForPosts(teamId, params)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Posts>() {
                    @Override
                    public void onCompleted() {
                        getView().ProgressBarVisibility(false);
                        if (!isSearchEmpty) {
                            getView().setRecycleView();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().ProgressBarVisibility(false);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Posts searchResult) {
                        if (searchResult.getPosts() == null) {
                            getView().NoResultsVisibility(true);
                            isSearchEmpty = true;
                        } else {
                            Realm realm = Realm.getDefaultInstance();
                            RealmList<FoundMessagesIds> list = new RealmList<>();
                            realm.beginTransaction();
                            for (String s : searchResult.getPosts().keySet()){
                                list.add(new FoundMessagesIds(s));
                            }
                            realm.where(FoundMessagesIds.class).findAll().deleteAllFromRealm();
                            realm.insertOrUpdate(list);
                            realm.commitTransaction();
                            realm.close();

                            postRepository.add(searchResult.getPosts().values());
                        }
                    }
                });
    }
}
