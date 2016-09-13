package com.kilogramm.mattermost.viewmodel.chat;

import android.content.Context;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.ui.MRealmRecyclerView;
import com.kilogramm.mattermost.viewmodel.ViewModel;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class ChatFragmentViewModel implements ViewModel {

    private static final String TAG = "ChatFragmentViewModel";

    private static final Integer TYPING_DURATION = 5000;

    private Realm realm;
    private Context context;
    private String channelId;
    private Team teamId;
    private Subscription subscription;
    private OnItemAddedListener onItemAddedListener;

    private String mMessage = "";
    //visibility
    private ObservableInt listVisibility;
    private ObservableInt isVisibleProgress;
    private ObservableInt newMessageVis;
    private ObservableInt emptyListVis;
    private ObservableInt typingVisibility;
    private SwipyRefreshLayout swipeRefreshLayout;
    private EditText writingMessage;
    private MRealmRecyclerView recyclerView;

    private Boolean isEmpty = false;
    private Boolean isLoadNext = true;
    private String lastMessageId = "";

    public ChatFragmentViewModel(Context context, String channelId,
                                 SwipyRefreshLayout swipyRefreshLayout,
                                 EditText writingMessage,
                                 OnItemAddedListener listener,
                                 MRealmRecyclerView recyclerView) {
        this.context = context;
        this.channelId = channelId;
        this.realm = Realm.getDefaultInstance();
        this.listVisibility = new ObservableInt(View.GONE);
        this.isVisibleProgress = new ObservableInt(View.VISIBLE);
        this.emptyListVis = new ObservableInt(View.GONE);
        this.newMessageVis = new ObservableInt(View.GONE);
        this.typingVisibility = new ObservableInt(View.GONE);
        this.swipeRefreshLayout = swipyRefreshLayout;
        this.writingMessage = writingMessage;
        this.onItemAddedListener = listener;
        this.recyclerView = recyclerView;
        setupRefreshListener();
        teamId = realm.where(Team.class).findFirst();
        getExtraInfo(teamId.getId(),
                channelId);
    }

    //===============================Methods======================================================

    private void setupRefreshListener() {
        recyclerView.getRecycleView().addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int bottomRow =
                        (recyclerView == null || recyclerView.getChildCount() == 0)
                                ? 0
                                : recyclerView.getAdapter().getItemCount()-1;
                swipeRefreshLayout.setEnabled(bottomRow == ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition());

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        this.swipeRefreshLayout.setOnRefreshListener(direction -> {
            isLoadNext = new Boolean(true);
            loadPosts(teamId.getId(), channelId);
        });
    }

    private void getExtraInfo(String teamId, String channelId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getExtraInfoChannel(teamId,channelId)
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
    }

    private void loadPosts(String teamId, String channelId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getPosts(teamId,channelId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Posts>() {
                    @Override
                    public void onCompleted() {
                        updateLastViewedAt(teamId, channelId);
                        swipeRefreshLayout.setRefreshing(false);
                        if(!isEmpty){
                            isVisibleProgress.set(View.GONE);
                            listVisibility.set(View.VISIBLE);
                            newMessageVis.set(View.VISIBLE);
                        }
                        if(isLoadNext) {
                            loadNextPost(teamId, channelId, getLastMessageId());
                        }
                        Log.d(TAG, "Complete load post");
                    }

                    @Override
                    public void onError(Throwable e) {
                        swipeRefreshLayout.setRefreshing(false);
                        e.printStackTrace();
                        Log.d(TAG, "Error");
                    }

                    @Override
                    public void onNext(Posts posts) {
                        if(posts.getPosts()==null || posts.getPosts().size() == 0){
                            isEmpty = true;
                            isLoadNext = false;
                            isVisibleProgress.set(View.GONE);
                            emptyListVis.set(View.VISIBLE);
                            newMessageVis.set(View.VISIBLE);
                        }
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        for (Post post : posts.getPosts().values()) {
                            post.setUser(realm.where(User.class)
                                    .equalTo("id", post.getUserId())
                                    .findFirst());
                            post.setViewed(true);
                        }
                        RealmList<Post> realmList = new RealmList<Post>();
                        realmList.addAll(posts.getPosts().values());
                        realm.insertOrUpdate(realmList);
                        realm.commitTransaction();
                        realm.close();
                        if(realmList.size() < 60){
                            isLoadNext = false;
                            Log.d(TAG, "is loaded " + isLoadNext);
                        }
                    }
                });
    }

    private void loadNextPost(String teamId, String channelId, String lastMessageId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.getPostsBefore(teamId,channelId, lastMessageId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<Posts>() {
                    @Override
                    public void onCompleted() {
                        isVisibleProgress.set(View.GONE);
                        listVisibility.set(View.VISIBLE);
                        newMessageVis.set(View.VISIBLE);
                        if(isLoadNext) {
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
                        if(posts.getPosts() == null){
                            isLoadNext = false;
                            return;
                        }
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        for (Post post : posts.getPosts().values()) {
                            post.setUser(realm.where(User.class)
                                    .equalTo("id", post.getUserId())
                                    .findFirst());
                        }
                        RealmList<Post> realmList = new RealmList<Post>();
                        realmList.addAll(posts.getPosts().values());
                        realm.insertOrUpdate(realmList);
                        realm.commitTransaction();
                        realm.close();
                        if(realmList.size() < 60){
                            isLoadNext = false;
                            Log.d(TAG, "is loaded " + isLoadNext);
                        }
                    }
                });
    }

    private TextWatcher messageWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mMessage = s.toString();
                //isEnabledSignInButton.set(canClickSignIn());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private String getLastMessageId(){
        String id;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Post> realmList = realm.where(Post.class)
                .equalTo("channelId", channelId)
                .findAllSorted("createAt");
        id = realmList.get(0).getId();
        realm.close();
        return id;
    }

    public void onSendClick(View v){
        sendMessage();
    }

    public void onAddFileClick(View v){
        Toast.makeText(getContext(), "pickFile", Toast.LENGTH_SHORT).show();
        //pickFile();
    }

    private void sendMessage() {
        Post post = new Post();
        post.setChannelId(channelId);
        post.setCreateAt(Calendar.getInstance().getTimeInMillis());
        post.setMessage(mMessage);
        post.setUserId(MattermostPreference.getInstance().getMyUserId());
        post.setPendingPostId(String.format("%s:%s", post.getUserId(), post.getCreateAt()));
        writingMessage.setText("");
        if(post.getMessage().length() != 0){
            sendToServer(post);
        } else {
            Toast.makeText(context, "Message is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToServer(Post post) {
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.sendPost(teamId.getId(),channelId, post)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Post>() {
                    @Override
                    public void onCompleted() {
                        updateLastViewedAt(teamId.getId(), channelId);
                        if(onItemAddedListener != null){
                            onItemAddedListener.onItemAdded();
                        }
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
                        realm.insertOrUpdate(post);
                        realm.commitTransaction();
                        realm.close();
                    }
                });
    }

    public void onClickRetry(View v){
        isLoadNext = new Boolean(true);
        loadPosts(teamId.getId(), channelId);
    }

    private void updateLastViewedAt(String teamId, String channelId){
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        MattermostApp application = MattermostApp.get(context);
        ApiMethod service = application.getMattermostRetrofitService();
        subscription = service.updatelastViewedAt(teamId,channelId)
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

    public void showTyping(){
        typingVisibility.set(View.VISIBLE);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                typingVisibility.set(View.GONE);
            }
        },TYPING_DURATION);
    }

    //===============================LifeCycle=====================================================

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


    //===============================Getters and Setters===========================================

    public String getChannelId() {
        return channelId;
    }

    public Context getContext() {
        return context;
    }

    public ObservableInt getEmptyListVis() {
        return emptyListVis;
    }

    public void setEmptyListVis(ObservableInt emptyListVis) {
        this.emptyListVis = emptyListVis;
    }

    public ObservableInt getListVisibility() {
        return listVisibility;
    }

    public void setListVisibility(Integer listVisibility) {
        this.listVisibility.set(listVisibility);
    }

    public ObservableInt getIsVisibleProgress() {
        return isVisibleProgress;
    }

    public void setIsVisibleProgress(Integer isVisibleProgress) {
        this.isVisibleProgress.set(isVisibleProgress);
    }

    public ObservableInt getNewMessageVis() {
        return newMessageVis;
    }

    public void setNewMessageVis(int newMessageVis) {
        this.newMessageVis.set(newMessageVis);
    }

    public TextWatcher getMessageWatcher() {
        return messageWatcher;
    }

    public void setMessageWatcher(TextWatcher messageWatcher) {
        this.messageWatcher = messageWatcher;
    }

    public ObservableInt getTypingVisibility() {
        return typingVisibility;
    }

    public void setTypingVisibility(Integer typingVisibility) {
        this.typingVisibility.set(typingVisibility);
    }

    //==============================Interface======================================================
    public interface OnItemAddedListener{
        void onItemAdded();
    }
}
