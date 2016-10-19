package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostByIdSpecification;
import com.kilogramm.mattermost.model.entity.post.PostEdit;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserByNameSearchSpecification;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.network.ApiMethod;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 06.10.2016.
 */
public class ChatRxPresenter extends BaseRxPresenter<ChatRxFragment> {

    private static final String TAG = "ChatRxPresenter";

    private static final int REQUEST_EXTRA_INFO = 1;
    private static final int REQUEST_LOAD_POSTS = 2;
    //private static final int REQUEST_LOAD_NEXT_POST = 3;
    private static final int REQUEST_SEND_TO_SERVER = 4;
    private static final int REQUEST_DELETE_POST = 5;
    private static final int REQUEST_EDIT_POST = 6;
    private static final int REQUEST_UPDATE_LAST_VIEWED_AT = 7;
    private static final int REQUEST_SEND_TO_SERVER_ERROR = 11;

    private static final int REQUEST_DB_GETUSERS = 10;

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
    PostEdit forEditPost;
    @State
    String lastmessageId;
    @State
    String firstmessageId;
    @State
    Boolean hasNextPost;
    @State
    String search;
    @State
    Long updateAt;

    private Boolean isEmpty = false;


    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();
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
        initSendToServer();
        initUpdateLastViewedAt();
        initDeletePost();
        initEditPost();
        initLoadAfter();
        initLoadBefore();
        initGetUsers();
        initSendToServerError();
    }

    private void initExtraInfo() {
        restartableLatestCache(REQUEST_EXTRA_INFO, () ->
                service.getExtraInfoChannel(this.teamId, this.channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()),

                (chatRxFragment, extraInfo) -> {
                    UserRepository.add(extraInfo.getMembers());
                    requestLoadPosts();
            }, (chatRxFragment1, throwable) -> sendError(throwable.getMessage()));
    }

    private void initLoadPosts() {

        restartableFirst(REQUEST_LOAD_POSTS, () -> service.getPosts(teamId, channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()),
                (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null || posts.getPosts().size() == 0) {
                        isEmpty = true;
                        sendShowEmptyList();
                    }
                    PostRepository.remove(new PostByChannelId(channelId));
                    PostRepository.prepareAndAdd(posts);
                    requestUpdateLastViewedAt();
                    sendRefreshing(false);
                    if (!isEmpty) {
                        sendShowList();
                    }
                    Log.d(TAG, "Complete load post");
                }, (chatRxFragment1, throwable) -> {
                    sendRefreshing(false);
                    sendError(throwable.getMessage());
                    throwable.printStackTrace();
                });
    }

    private void initSendToServerError(){
        restartableFirst(REQUEST_SEND_TO_SERVER_ERROR, () -> service.sendPost(teamId, channelId, forSendPost)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, post) -> {
                    post.setUser(UserRepository.query(new UserRepository.UserByIdSpecification(post.getUserId())).first());
                    post.setMessage(Processor.process(post.getMessage(), Configuration.builder()
                            .forceExtentedProfile()
                            .build()));
                    PostRepository.removeTempPost(post.getPendingPostId());
                    PostRepository.add(post);

                    requestUpdateLastViewedAt();
                    sendOnItemAdded();
                    sendHideFileAttachLayout();
                    FileToAttachRepository.getInstance().clearData();
                    Log.d(TAG, "Complete create post");
                }, (chatRxFragment1, throwable) -> {
                    sendError(throwable.getMessage());
                    setErrorPost(forSendPost.getPendingPostId());
                    throwable.printStackTrace();
                    Log.d(TAG, "Error create post " + throwable.getMessage());
                });
    }

    private void initSendToServer() {

        restartableFirst(REQUEST_SEND_TO_SERVER, () -> service.sendPost(teamId, channelId, forSendPost)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()),
                (chatRxFragment, post) -> {
                    post.setUser(UserRepository.query(new UserRepository.UserByIdSpecification(post.getUserId())).first());
                    post.setMessage(Processor.process(post.getMessage(), Configuration.builder()
                            .forceExtentedProfile()
                            .build()));
                    PostRepository.removeTempPost(post.getPendingPostId());
                    PostRepository.add(post);

                    requestUpdateLastViewedAt();
                    sendOnItemAdded();
                    sendHideFileAttachLayout();
                    FileToAttachRepository.getInstance().clearData();
                    Log.d(TAG, "Complete create post");
                }, (chatRxFragment1, throwable) -> {
                    sendError(throwable.getMessage());
                    setErrorPost(forSendPost.getPendingPostId());
                    throwable.printStackTrace();
                    Log.d(TAG, "Error create post " + throwable.getMessage());
                });
    }

    private void initUpdateLastViewedAt() {
        restartableFirst(REQUEST_UPDATE_LAST_VIEWED_AT, () -> service.updatelastViewedAt(teamId, channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()),

                (chatRxFragment, post) -> {

        }, (chatRxFragment1, throwable) -> {
                    sendError(throwable.getMessage());
                    throwable.printStackTrace();
                    Log.d(TAG, "Error");
        });
    }

    private void initDeletePost() {
        restartableFirst(REQUEST_DELETE_POST, () -> service.deletePost(teamId, channelId, forDeletePost.getId(), new Object())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()),
                (chatRxFragment, post1) -> PostRepository.remove(post1),
                (chatRxFragment1, throwable) -> {
                    sendError(throwable.getMessage());
                    throwable.printStackTrace();
                    Log.d(TAG, "Error delete post " + throwable.getMessage());
                });
    }

    private void initEditPost() {
        restartableFirst(REQUEST_EDIT_POST, () -> service.editPost(teamId, channelId, forEditPost)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, post1) -> {
                    post1.setUser(UserRepository.query(new UserRepository.UserByIdSpecification(post1.getUserId())).first());
                    post1.setMessage(Processor.process(post1.getMessage(), Configuration.builder().forceExtentedProfile().build()));
                    PostRepository.update(post1);
                    sendIvalidateAdapter();
                }, (chatRxFragment1, throwable) -> {
                    Post post = new Post(PostRepository.query(new PostByIdSpecification(forEditPost.getId())).first());
                    post.setUpdateAt(updateAt);
                    PostRepository.update(post);
                    sendIvalidateAdapter();
                    throwable.printStackTrace();
                    Log.d(TAG, "Error edit post " + throwable.getMessage());
                });
    }

    private void initLoadBefore(){
        restartableFirst(REQUEST_LOAD_BEFORE,
                () -> {
                    Log.d(TAG, "initLoadBefore");
                    return service.getPostsBefore(teamId, channelId, lastmessageId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io());
                },
                (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null) {
                        sendCanPaginationTop(false);
                        return;
                    }
                    PostRepository.prepareAndAdd(posts);
                    sendShowList();
                    sendDisableShowLoadMoreTop();
                    Log.d(TAG, "Complete load next post");
                }, (chatRxFragment1, throwable) -> {
                    sendDisableShowLoadMoreTop();
                    sendError(throwable.getMessage());
                    throwable.printStackTrace();
                    Log.d(TAG, "Error");
                });
    }

    private void initLoadAfter(){
        restartableFirst(REQUEST_LOAD_AFTER,
                () -> {
                    Log.d(TAG, "initLoadAfter");
                    return  service.getPostsAfter(teamId, channelId, firstmessageId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io());
                }, (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null) {
                        sendCanPaginationBot(false);
                        return;
                    }
                    RealmList<Post> realmList = new RealmList<>();
                    for (Post post : posts.getPosts().values()) {
                        User user = UserRepository.queryList(new UserRepository.UserByIdSpecification(post.getUserId())).get(0);
                        post.setUser(user);
                        post.setViewed(true);
                        post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
                    }
                    realmList.addAll(posts.getPosts().values());
                    PostRepository.add(realmList);
                    sendShowList();
                    sendDisableShowLoadMoreBot();
                    Log.d(TAG, "Complete load next post");
                }, (chatRxFragment1, throwable) -> {
                    sendDisableShowLoadMoreBot();
                    sendError(throwable.getMessage());
                    throwable.printStackTrace();
                    Log.d(TAG, "Error");
                });
    }

    private void initGetUsers(){
        restartableFirst(REQUEST_DB_GETUSERS,
                () ->
                        UserRepository.query((new UserByNameSearchSpecification(search))).asObservable(),
                (chatRxFragment, o) -> sendDropDown(o));
    }

    //endregion

    //region Requests
    public void requestExtraInfo() {
        start(REQUEST_EXTRA_INFO);
    }

    public void requestLoadPosts() {
        start(REQUEST_LOAD_POSTS);
    }


    public void requestSendToServer(Post post) {
        forSendPost = post;
        String sendedPostId = post.getPendingPostId();
        post.setId(null);

        start(REQUEST_SEND_TO_SERVER);

        Post forSavePost = new Post(forSendPost);
        forSavePost.setId(sendedPostId);
        forSavePost.setUser(UserRepository.query(new UserRepository.UserByIdSpecification(forSavePost.getUserId()))
                .first());
        forSavePost.setMessage(Processor.process(forSavePost.getMessage(), Configuration.builder().forceExtentedProfile().build()));
        PostRepository.add(forSavePost);
    }



    public void requestSendToServerError(Post post){
        forSendPost = post;
        post.setId(null);
        post.setUser(null);
        post.setMessage(Html.fromHtml(post.getMessage()).toString().trim());
        start(REQUEST_SEND_TO_SERVER_ERROR);
    }

    public void requestDeletePost(Post post) {
        forDeletePost = post;
        start(REQUEST_DELETE_POST);
    }

    public void requestEditPost(PostEdit post) {
        forEditPost = post;
        start(REQUEST_EDIT_POST);
        Post post1 = new Post(PostRepository.query(new PostByIdSpecification(forEditPost.getId())).first());
        post1.setUpdateAt(null);
        PostRepository.update(post1);
        sendIvalidateAdapter();
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

    public void requestGetUsers(String search){
        this.search = search;
        start(REQUEST_DB_GETUSERS);
    }

    //endregion

    // region To View
    private void sendShowEmptyList(){
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.showEmptyList()));

    }
    private void sendRefreshing(Boolean isShow){
        createTemplateObservable(isShow).subscribe(split(
                ChatRxFragment::setRefreshing));
    }
    private void sendShowList(){
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.showList()));
    }
    private void sendOnItemAdded(){
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.onItemAdded()));
    }
    private void sendIvalidateAdapter(){
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.invalidateAdapter()));
    }
    private void sendSetDropDown(RealmResults<User> results){
        createTemplateObservable(results).subscribe(split(
                ChatRxFragment::setDropDown));
    }
    private void sendDisableShowLoadMoreTop(){
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.disableShowLoadMoreTop()));
    }
    private void sendDisableShowLoadMoreBot(){
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.disableShowLoadMoreBot()));
    }
    private void sendCanPaginationTop(Boolean b){
        createTemplateObservable(b).subscribe(split(
                ChatRxFragment::setCanPaginationTop));
    }
    private void sendCanPaginationBot(Boolean b){
        createTemplateObservable(b).subscribe(split(
                ChatRxFragment::setCanPaginationBot));
    }
    private void sendDropDown(RealmResults<User> users){
        createTemplateObservable(users).subscribe(split(
                ChatRxFragment::setDropDown));
    }
    private void sendHideFileAttachLayout(){
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                        chatRxFragment.hideAttachedFilesLayout()));

    }
    private void sendError(String error){
        createTemplateObservable(error).subscribe(split((chatRxFragment, s) ->
                        Toast.makeText(chatRxFragment.getActivity(),s,Toast.LENGTH_SHORT).show()));
    }
    //endregion



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
        RealmResults<Post> realmList = PostRepository.query(new PostByChannelId(channelId));
        if(realmList.size()!=0) {
            lastmessageId = realmList.get(0).getId();
            Log.d(TAG, "lastmessage " + realmList.get(0).getMessage());
        }
    }

    public void getFirstMessageId() {
        RealmResults<Post> realmList = PostRepository.query(new PostByChannelId(channelId));
        if(realmList.size()!=0){
            firstmessageId = realmList.get(realmList.size()-1).getId();
            Log.d(TAG, "firstmessage " + realmList.get(realmList.size()-1).getMessage());
        }
    }

    private void setErrorPost(String sendedPostId) {
        Post post = new Post(PostRepository.query(new PostByIdSpecification(sendedPostId)).first());
        post.setUpdateAt(Post.NO_UPDATE);
        Log.d("CreateAt", "setErrorPost: " + post.getCreateAt());
        PostRepository.update(post);
        sendIvalidateAdapter();
    }
}
