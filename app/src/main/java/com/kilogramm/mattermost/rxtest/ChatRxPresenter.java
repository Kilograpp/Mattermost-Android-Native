package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserByIdSpecification;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.network.ApiMethod;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 06.10.2016.
 */
public class ChatRxPresenter extends BaseRxPresenter<ChatRxFragment> {

    private static final String TAG = "ChatPresenter";

    private static final int REQUEST_EXTRA_INFO = 1;
    private static final int REQUEST_LOAD_POSTS = 2;
    private static final int REQUEST_LOAD_NEXT_POST = 3;
    private static final int REQUEST_SEND_TO_SERVER = 4;
    private static final int REQUEST_DELETE_POST = 5;
    private static final int REQUEST_EDIT_POST = 6;
    private static final int REQUEST_UPDATE_LAST_VIEWED_AT = 7;

    private static final int REQUEST_LOAD_BEFORE = 8;
    private static final int REQUEST_LOAD_AFTER = 9;


    private ApiMethod service;

    @State
    String teamId;
    @State
    String channelId;
    @State
    Post forSendPost;
    @State
    Post forDeletePost;
    @State
    Post forEditPost;
    @State
    String lastmessageId;
    @State
    String firstmessageId;
    @State
    Boolean hasNextPost;

    private Boolean isEmpty = false;
    private PostRepository postRepository;
    private UserRepository userRepository;


    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();
        postRepository = new PostRepository();
        userRepository = new UserRepository();
        initRequests();
    }

    public void initPresenter(String teamId, String channelId){
        Log.d(TAG,"initPresenter");
        this.teamId = teamId;
        this.channelId = channelId;

    }

    //region Init Requests
    private void initRequests() {
        initExtraInfo();
        initLoadPosts();
        initLoadNextPost();
        initSendToServer();
        initUpdateLastViewedAt();
        initDeletePost();
        initEditPost();
        initLoadAfter();
        initLoadBefore();
    }

    private void initExtraInfo() {
        //TODO FIX logic

        restartableLatestCache(REQUEST_EXTRA_INFO, () ->
                service.getExtraInfoChannel(this.teamId, this.channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()),

                (chatRxFragment, extraInfo) -> {
                    userRepository.add(extraInfo.getMembers());
                    requestLoadPosts();
            }, (chatRxFragment1, throwable) -> throwable.printStackTrace());
    }

    private void initLoadPosts() {

        restartableFirst(REQUEST_LOAD_POSTS, () -> service.getPosts(teamId, channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()),
                (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null || posts.getPosts().size() == 0) {
                        isEmpty = true;
                        sendShowEmptyList();
                    }
                    RealmList<Post> realmList = new RealmList<>();
                    for (Post post : posts.getPosts().values()) {
                        post.setUser(userRepository.query(new UserByIdSpecification(post.getUserId())).first());
                        post.setViewed(true);
                        post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
                    }
                    realmList.addAll(posts.getPosts().values());
                    postRepository.add(realmList);

                    requestUpdateLastViewedAt();
                    sendRefreshing(false);
                    if (!isEmpty) {
                        sendShowList();
                        requestLoadNextPost();
                    }
                    Log.d(TAG, "Complete load post");
                }, (chatRxFragment1, throwable) -> {
                    sendRefreshing(false);
                    throwable.printStackTrace();
                });
    }

    private void initLoadNextPost() {
        restartableFirst(REQUEST_LOAD_NEXT_POST, () ->
                service.getPostsBefore(teamId, channelId, lastmessageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()),
        (chatRxFragment, posts) -> {
            if (posts.getPosts() == null) {
                //isLoadNext = false;
                return;
            }
            RealmList<Post> realmList = new RealmList<>();
            for (Post post : posts.getPosts().values()) {
                User user = userRepository.queryList(new UserByIdSpecification(post.getUserId())).get(0);
                post.setUser(user);
                post.setViewed(true);
                post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
            }
            realmList.addAll(posts.getPosts().values());
            postRepository.add(realmList);
            sendShowList();
            sendDisableShowLoadMoreTop();
            Log.d(TAG, "Complete load next post");
        }, (chatRxFragment1, throwable) -> {
            throwable.printStackTrace();
            Log.d(TAG, "Error");
        });
    }

    private void initSendToServer() {

        restartableFirst(REQUEST_SEND_TO_SERVER, () -> service.sendPost(teamId, channelId, forSendPost)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()),
                (chatRxFragment, post) -> {
                    post.setUser(userRepository.query(new UserByIdSpecification(post.getUserId())).first());
                    post.setMessage(Processor.process(post.getMessage(), Configuration.builder()
                            .forceExtentedProfile()
                            .build()));
                    postRepository.removeTempPost(post.getPendingPostId());
                    postRepository.add(post);

                    requestUpdateLastViewedAt();
                    sendOnItemAdded();
                    Log.d(TAG, "Complete create post");
                }, (chatRxFragment1, throwable) -> {
                    throwable.printStackTrace();
                    Log.d(TAG, "Error create post " + throwable.getMessage());
                });
    }

    private void initUpdateLastViewedAt() {
        restartableFirst(REQUEST_UPDATE_LAST_VIEWED_AT, () -> service.updatelastViewedAt(teamId, channelId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.io()),

                (chatRxFragment, post) -> {

        }, (chatRxFragment1, throwable) -> {
            throwable.printStackTrace();
            Log.d(TAG, "Error");
        });
    }

    private void initDeletePost() {
        restartableFirst(REQUEST_DELETE_POST, () -> service.deletePost(teamId, channelId, forSendPost.getId(), new Object())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()),
                (chatRxFragment, post1) -> postRepository.remove(post1),
                (chatRxFragment1, throwable) -> {
                        throwable.printStackTrace();
                        Log.d(TAG, "Error delete post " + throwable.getMessage());
                });
    }

    private void initEditPost() {
        restartableFirst(REQUEST_EDIT_POST, () -> service.editPost(teamId, channelId, forEditPost)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()),
                (chatRxFragment, post1) -> {
            post1.setUser(userRepository.query(new UserByIdSpecification(post1.getUserId())).first());
            post1.setMessage(Processor.process(post1.getMessage(), Configuration.builder().forceExtentedProfile().build()));
            postRepository.update(post1);
            sendIvalidateAdapter();
        }, (chatRxFragment1, throwable) -> {
            throwable.printStackTrace();
            Log.d(TAG, "Error edit post " + throwable.getMessage());
        });
    }

    private void initLoadBefore(){
        restartableFirst(REQUEST_LOAD_BEFORE,
                () -> service.getPostsBefore(teamId, channelId, lastmessageId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread()),
                (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null) {
                        sendCanPaginationTop(false);
                        return;
                    }
                    RealmList<Post> realmList = new RealmList<>();
                    for (Post post : posts.getPosts().values()) {
                        User user = userRepository.queryList(new UserByIdSpecification(post.getUserId())).get(0);
                        post.setUser(user);
                        post.setViewed(true);
                        post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
                    }
                    realmList.addAll(posts.getPosts().values());
                    postRepository.add(realmList);
                    sendShowList();
                    sendDisableShowLoadMoreTop();
                    Log.d(TAG, "Complete load next post");
                }, (chatRxFragment1, throwable) -> {
                    throwable.printStackTrace();
                    Log.d(TAG, "Error");
                });
    }

    private void initLoadAfter(){
        restartableFirst(REQUEST_LOAD_AFTER,
                () -> service.getPostsAfter(teamId, channelId, firstmessageId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null) {
                        sendCanPaginationBot(false);
                        return;
                    }
                    RealmList<Post> realmList = new RealmList<>();
                    for (Post post : posts.getPosts().values()) {
                        User user = userRepository.queryList(new UserByIdSpecification(post.getUserId())).get(0);
                        post.setUser(user);
                        post.setViewed(true);
                        post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
                    }
                    realmList.addAll(posts.getPosts().values());
                    postRepository.add(realmList);
                    sendShowList();
                    sendDisableShowLoadMoreBot();
                    Log.d(TAG, "Complete load next post");
                }, (chatRxFragment1, throwable) -> {
                    throwable.printStackTrace();
                    Log.d(TAG, "Error");
                });
    }

    //endregion

    //region Requests
    public void requestExtraInfo() {
        start(REQUEST_EXTRA_INFO);
    }

    public void requestLoadPosts() {
        start(REQUEST_LOAD_POSTS);
    }

    public void requestLoadNextPost() {
        getLastMessageId();
        start(REQUEST_LOAD_NEXT_POST);
    }

    public void requestSendToServer(Post post) {
        forSendPost = post;
        String sendedPostId = post.getId();
        post.setId(null);

        start(REQUEST_SEND_TO_SERVER);

        Post forSavePost = new Post(forSendPost);
        forSavePost.setId(sendedPostId);
        forSavePost.setUser(userRepository.query(new UserByIdSpecification(forSavePost.getUserId()))
                .first());
        postRepository.add(forSavePost);
    }

    public void requestDeletePost(Post post) {
        forDeletePost = post;
        start(REQUEST_DELETE_POST);
    }

    public void requestEditPost(Post post) {
        forEditPost = post;
        start(REQUEST_EDIT_POST);
    }

    public void requestUpdateLastViewedAt() {
        start(REQUEST_UPDATE_LAST_VIEWED_AT);
    }

    public void requestLoadBefore(){
        getLastMessageId();
        start(REQUEST_LOAD_BEFORE);
    }

    public void requestLoadAfter(){
        getFirstMessageId();
        start(REQUEST_LOAD_AFTER);
    }

    //endregion

    // region To View
    private void sendShowEmptyList(){
        Observable.just(new Object())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.showEmptyList()));

    }
    private void sendRefreshing(Boolean isShow){
        Observable.just(isShow)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split(ChatRxFragment::setRefreshing));
    }
    private void sendShowList(){
        Observable.just(new Object())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.showList()));
    }
    private void sendOnItemAdded(){
        Observable.just(new Object())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.onItemAdded()));
    }
    private void sendIvalidateAdapter(){
        Observable.just(new Object())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.invalidateAdapter()));
    }
    private void sendSetDropDown(RealmResults<User> results){
        Observable.just(results)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split(ChatRxFragment::setDropDown));
    }
    private void sendDisableShowLoadMoreTop(){
        Observable.just(new Object())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.disableShowLoadMoreTop()));
    }
    private void sendDisableShowLoadMoreBot(){
        Observable.just(new Object())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.disableShowLoadMoreBot()));
    }
    private void sendCanPaginationTop(Boolean b){
        Observable.just(b)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split(ChatRxFragment::setCanPaginationTop));
    }
    private void sendCanPaginationBot(Boolean b){
        Observable.just(b)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(deliverFirst())
                .subscribe(split(ChatRxFragment::setCanPaginationBot));
    }

    //endregion


    public TextWatcher getMassageTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().contains("@"))
                    if (charSequence.charAt((count > 1 ? count : start) - before) == '@')
                        getUsers(null);
                    else
                        getUsers(charSequence.toString());
                else
                    sendSetDropDown(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    public void getUsers(String search) {
        RealmResults<User> users;
        String currentUser = MattermostPreference.getInstance().getMyUserId();
        Realm realm = Realm.getDefaultInstance();
        if (search == null)
            users = realm.where(User.class).isNotNull("id").notEqualTo("id", currentUser).findAllSorted("username", Sort.ASCENDING);
        else {
            String[] username = search.split("@");
            users = realm.where(User.class).isNotNull("id").notEqualTo("id", currentUser).contains("username", username[username.length - 1]).findAllSorted("username", Sort.ASCENDING);
        }
        sendSetDropDown(users);
        realm.close();
    }

    private void getLastMessageId() {
        RealmResults<Post> realmList = postRepository.query(new PostByChannelId(channelId));
        lastmessageId = realmList.get(0).getId();
        Log.d(TAG, "lastmessage " + realmList.get(0).getMessage());
    }

    public void getFirstMessageId() {
        RealmResults<Post> realmList = postRepository.query(new PostByChannelId(channelId));
        firstmessageId = realmList.get(realmList.size()-1).getId();
        Log.d(TAG, "firstmessage " + realmList.get(realmList.size()-1).getMessage());
    }
}
