package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.github.rjeschke.txtmark.Processor;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.view.chat.ChatFragmentMVP;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import nucleus.presenter.Presenter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 13.09.2016.
 */
public class ChatPresenter extends Presenter<ChatFragmentMVP> {
    private static final String TAG = "ChatPresenter";

    private Subscription mSubscription;

    private MattermostApp mMattermostApp;

    private Boolean isEmpty = false;
    private Boolean isLoadNext = true;
    //private String lastMessageId = "";


    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mMattermostApp = MattermostApp.getSingleton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
    }
    //===============================Methods======================================================

    public void getExtraInfo(String teamId, String channelId) {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();


        //TODO FIX logic
        ApiMethod service = null;
        service = mMattermostApp.getMattermostRetrofitService();
        mSubscription = service.getExtraInfoChannel(teamId, channelId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<ExtraInfo>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete load extra_info");
                        loadPosts(teamId, channelId);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error extra_info");
                    }

                    @Override
                    public void onNext(ExtraInfo extraInfo) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        RealmList<User> list = new RealmList<User>();
                        list.addAll(extraInfo.getMembers());
                        realm.insertOrUpdate(list);
                        realm.commitTransaction();
                        realm.close();
                    }
                });
    } //  +

    public void loadPosts(String teamId, String channelId) {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
        ApiMethod service = null;
        service = mMattermostApp.getMattermostRetrofitService();
        mSubscription = service.getPosts(teamId, channelId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Posts>() {
                    @Override
                    public void onCompleted() {
                        updateLastViewedAt(teamId, channelId);
                        getView().setRefreshing(false);
                        if (!isEmpty) {
                            getView().showList();
                        }
                        if (isLoadNext) {
                            loadNextPost(teamId, channelId, getLastMessageId());
                        }
                        Log.d(TAG, "Complete load post");
                    }

                    @Override
                    public void onError(Throwable e) {
                        getView().setRefreshing(false);
                        e.printStackTrace();
                        Log.d(TAG, "Error");
                    }

                    @Override
                    public void onNext(Posts posts) {
                        if (posts.getPosts() == null || posts.getPosts().size() == 0) {
                            isEmpty = true;
                            isLoadNext = false;
                            getView().showEmptyList();
                        }
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        for (Post post : posts.getPosts().values()) {
                            post.setUser(realm.where(User.class)
                                    .equalTo("id", post.getUserId())
                                    .findFirst());
                            post.setViewed(true);
                            post.setMessage(Processor.process(post.getMessage()));
                        }
                        RealmList<Post> realmList = new RealmList<Post>();
                        realmList.addAll(posts.getPosts().values());
                        realm.insertOrUpdate(realmList);
                        realm.commitTransaction();
                        realm.close();
                        if (realmList.size() < 60) {
                            isLoadNext = false;
                            Log.d(TAG, "is loaded " + isLoadNext);
                        }
                    }
                });
    } // +

    public void loadNextPost(String teamId, String channelId, String lastMessageId) {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
        ApiMethod service = null;
        service = mMattermostApp.getMattermostRetrofitService();
        mSubscription = service.getPostsBefore(teamId, channelId, lastMessageId)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<Posts>() {
                    @Override
                    public void onCompleted() {
                        getView().showList();
                        if (isLoadNext) {
                            loadNextPost(teamId, channelId, getLastMessageId());
                        }
                        Log.d(TAG, "Complete load next post");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error");
                    }

                    @Override
                    public void onNext(Posts posts) {
                        if (posts.getPosts() == null) {
                            isLoadNext = false;
                            return;
                        }
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        for (Post post : posts.getPosts().values()) {
                            post.setUser(realm.where(User.class)
                                    .equalTo("id", post.getUserId())
                                    .findFirst());
                            post.setMessage(Processor.process(post.getMessage()));
                        }
                        RealmList<Post> realmList = new RealmList<Post>();
                        realmList.addAll(posts.getPosts().values());
                        realm.insertOrUpdate(realmList);
                        realm.commitTransaction();
                        realm.close();
                        if (realmList.size() < 60) {
                            isLoadNext = false;
                            Log.d(TAG, "is loaded " + isLoadNext);
                        }
                    }
                });
    } // +


    public TextWatcher getMassageTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().contains("@"))
                    if (charSequence.charAt(start - before) == '@')
                        onMoreClick(null);
                    else
                        onMoreClick(charSequence.toString());
                else
                    getView().setDropDown(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }


    public void onMoreClick(String search) {
        RealmResults<User> users;
        Realm realm = Realm.getDefaultInstance();
        if (search == null)
            users = realm.where(User.class).isNotNull("id").findAllSorted("username", Sort.ASCENDING);
        else {
            String[] username = search.split("@");
            users = realm.where(User.class).isNotNull("id").contains("username", username[username.length - 1]).findAllSorted("username", Sort.ASCENDING);
        }
        getView().setDropDown(users);
        realm.close();
    }


    private String getLastMessageId() {
        String id;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Post> realmList = realm.where(Post.class)
                .equalTo("channelId", getView().getChId())
                .findAllSorted("createAt");
        id = realmList.get(0).getId();
        realm.close();
        return id;
    } // +

    public void sendToServer(Post post, String teamId, String channelId) {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
        ApiMethod service = null;
        service = mMattermostApp.getMattermostRetrofitService();
        mSubscription = service.sendPost(teamId, channelId, post)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Post>() {
                    @Override
                    public void onCompleted() {
                        updateLastViewedAt(teamId, channelId);
                        getView().onItemAdded();
                        Log.d(TAG, "Complete create post");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error create post " + e.getMessage());
                    }

                    @Override
                    public void onNext(Post post) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        post.setUser(realm.where(User.class)
                                .equalTo("id", post.getUserId())
                                .findFirst());
                        post.setMessage(Processor.process(post.getMessage()));
                        realm.insertOrUpdate(post);
                        realm.commitTransaction();
                        realm.close();
                    }
                });
    }

    private void updateLastViewedAt(String teamId, String channelId) {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
        ApiMethod service = null;
        service = mMattermostApp.getMattermostRetrofitService();
        mSubscription = service.updatelastViewedAt(teamId, channelId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<Post>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete update last viewed at");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error");
                    }

                    @Override
                    public void onNext(Post post) {
                    }
                });
    }

    public void initLoadNext() {
        isLoadNext = new Boolean(true);
    }
}
