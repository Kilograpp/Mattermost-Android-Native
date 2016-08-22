package com.kilogramm.mattermost.viewmodel.chat;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApplication;
import com.kilogramm.mattermost.adapters.ChatListAdapter;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.viewmodel.ViewModel;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class ChatFragmentViewModel implements ViewModel {

    private static final String TAG = "ChatFragmentViewModel";

    private Realm realm;
    private Context context;
    private String channelId;
    private Subscription subscription;

    public ChatFragmentViewModel(Context context, String channelId) {
        this.context = context;
        this.channelId = channelId;
        this.realm = Realm.getDefaultInstance();
        loadChannels(realm.where(Team.class).findFirst().getId(),
               channelId);
    }

    private void loadChannels(String teamId, String channelId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApplication application = MattermostApplication.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getPosts(teamId,channelId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Posts>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Complete load post");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error");
                    }

                    @Override
                    public void onNext(Posts posts) {
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                            RealmList<Post> list = new RealmList<Post>();
                            list.addAll(posts.getPosts().values());
                            realm.insertOrUpdate(list);
                        realm.commitTransaction();
                        realm.close();
                        /*realm.executeTransaction(realm1 -> {
                            RealmList<Post> list = new RealmList<Post>();
                            list.addAll(posts.getPosts().values());
                            realm1.copyToRealmOrUpdate(list);
                        });
                        realm.close();*/
                        Log.d(TAG, "save in data base");
                    }

                });

    }

    @BindingAdapter({"android:items", "android:context"})
    public static void setupListChat(RecyclerView listView, String channelId, Context context) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Post> results = realm.where(Post.class)
                .equalTo("channelId", channelId)
                .findAllSorted("createAt", Sort.ASCENDING);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setStackFromEnd(true);
        listView.setLayoutManager(manager);
        listView.setAdapter(new ChatListAdapter(listView.getContext(),results, listView));
        realm.close();

    }

    @Override
    public void destroy() {
        Log.d(TAG, "OnDestroy()");
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        subscription = null;
        context = null;
        realm.close();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }


    public String getChannelId() {
        return channelId;
    }

    public Context getContext() {
        return context;
    }
}
