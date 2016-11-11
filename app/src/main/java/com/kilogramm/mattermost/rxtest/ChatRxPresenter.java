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
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.member.MembersRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostByIdSpecification;
import com.kilogramm.mattermost.model.entity.post.PostByRootIdSpecification;
import com.kilogramm.mattermost.model.entity.post.PostEdit;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserByNameSearchSpecification;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.network.ApiMethod;

import java.util.ArrayList;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
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

    private static final int REQUEST_LOAD_FOUND_MESSAGE = 12;

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
    @State
    Boolean isSendingPost = false;

    private Boolean isEmpty = false;

    private String limit;

    @State
    String searchMessageId;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();
        limit = "60";
        initRequests();
    }

    public void initPresenter(String teamId, String channelId) {
        Log.d(TAG, "initPresenter");
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
        initLoadBeforeAndAfter();
    }

    private void initExtraInfo() {
        restartableFirst(REQUEST_EXTRA_INFO,
                () -> Observable.defer(() -> Observable.zip(
                        service.getChannelsTeam(this.teamId)
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io()),
                        service.getExtraInfoChannel(this.teamId, this.channelId)
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io()),
                        (channelsWithMembers, extraInfo) -> {
                            ChannelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
                                    MattermostPreference.getInstance().getMyUserId());
                            MembersRepository.add(channelsWithMembers.getMembers().values());
                            RealmList<User> results = new RealmList<>();
                            results.addAll(UserRepository.query(new UserRepository.UserByIdsSpecification(extraInfo.getMembers())));
                            extraInfo.setMembers(results);
                            ExtroInfoRepository.add(extraInfo);
                            return extraInfo;
                        }))
                , (chatRxFragment, extraInfo) -> {

                    requestLoadPosts();
                }, (chatRxFragment1, throwable) -> {
                    sendError(getError(throwable));
                    setErrorLayout(getError(throwable));
                    sendShowList();
                });
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
                    sendShowList();
                    sendError(getError(throwable));
                    throwable.printStackTrace();
                });
    }

    private void initSendToServerError() {
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
                    sendShowList();
                    sendHideFileAttachLayout();
                    FileToAttachRepository.getInstance().deleteUploadedFiles();
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
                    PostRepository.removeTempPost(post.getPendingPostId());
                    PostRepository.prepareAndAddPost(post);
                    requestUpdateLastViewedAt();
                    sendOnItemAdded();
                    sendShowList();
                    FileToAttachRepository.getInstance().deleteUploadedFiles();
                    isSendingPost = false;
                    Log.d(TAG, "Complete create post");
                }, (chatRxFragment1, throwable) -> {
                    sendError(getError(throwable));
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
                    sendError(getError(throwable));
                    throwable.printStackTrace();
                    Log.d(TAG, "Error");
                });
    }

    private void initDeletePost() {
        restartableFirst(REQUEST_DELETE_POST, () -> service.deletePost(teamId, channelId, forDeletePost.getId(), new Object())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, post1) -> {
                    PostRepository.remove(new PostByRootIdSpecification(post1.getId()));
                    PostRepository.remove(post1);
                },
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
                    PostRepository.prepareAndAddPost(post1);
                    sendIvalidateAdapter();
                }, (chatRxFragment1, throwable) -> {
                    Post post = new Post(PostRepository.query(new PostByIdSpecification(forEditPost.getId())).first());
                    post.setUpdateAt(updateAt);
                    PostRepository.update(post);
                    sendIvalidateAdapter();
                    sendError(throwable.getMessage());
                    Log.d(TAG, "Error edit post " + throwable.getMessage());
                });
    }

    private void initLoadBefore() {
        restartableFirst(REQUEST_LOAD_BEFORE,
                () -> {
                    Log.d(TAG, "initLoadBefore");
                    return service.getPostsBefore(teamId, channelId, lastmessageId, limit)
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
                    Log.d(TAG, "Complete load before post");
                }, (chatRxFragment1, throwable) -> {
                    sendDisableShowLoadMoreTop();
                    sendError(throwable.getMessage());
                    throwable.printStackTrace();
                    Log.d(TAG, "Error");
                });
    }

    private void initLoadAfter() {
        restartableFirst(REQUEST_LOAD_AFTER,
                () -> {
                    Log.d(TAG, "initLoadAfter");
                    return service.getPostsAfter(teamId, channelId, firstmessageId, limit)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io());
                }, (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null) {
                        sendCanPaginationBot(false);
                        return;
                    }
                    PostRepository.prepareAndAdd(posts);
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

    private void initGetUsers() {
        restartableFirst(REQUEST_DB_GETUSERS,
                () -> UserRepository.query((new UserByNameSearchSpecification(search))).asObservable(),
                (chatRxFragment, o) -> sendDropDown(o));
    }

    private void initLoadBeforeAndAfter() {
        restartableFirst(REQUEST_LOAD_FOUND_MESSAGE, () ->
                Observable.defer(() -> Observable.zip(
                        service.getExtraInfoChannel(teamId, channelId)
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io()),
                        service.getPostsBefore(teamId, channelId, searchMessageId, limit)
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io()),
                        service.getPost(teamId, channelId, searchMessageId)
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io()),
                        service.getPostsAfter(teamId, channelId, searchMessageId, limit)
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io()),
                        (extraInfo, postsBef, foundPosts, postsAft) -> {
                            ArrayList<Posts> allPosts = new ArrayList<>();
                            if (postsAft.getPosts() != null) {
                                allPosts.add(postsAft);
                            }
                            allPosts.add(foundPosts);
                            if (postsBef.getPosts() != null) {
                                allPosts.add(postsBef);
                            }
                            return allPosts;
                        }))
                , (chatRxFragment, postsAll) -> {
                    PostRepository.remove(new PostByChannelId(channelId));
                    if (postsAll == null) {
                        sendCanPaginationBot(false);
                        return;
                    }

                    try {
                        for (Posts posts : postsAll) {
                            PostRepository.prepareAndAdd(posts);
                        }
                    } catch (Throwable e){
                        e.printStackTrace();
                        sendError(e.getMessage());
                    } finally {
                        sendRefreshing(false);
                        sendShowList();
                        sendDisableShowLoadMoreBot();
                        sendSlideDialogToFoundMessage();
                    }
                }, (chatRxFragment, throwable) -> {
                    throwable.printStackTrace();
                    sendError(throwable.getMessage());
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

    public void requestSendToServer(Post post) {
        if(isSendingPost) return;
        if (FileToAttachRepository.getInstance().haveUnloadedFiles()) return;
        isSendingPost = true;
        forSendPost = post;
        String sendedPostId = post.getPendingPostId();
        post.setId(null);

        start(REQUEST_SEND_TO_SERVER);

        Post forSavePost = new Post(forSendPost);
        forSavePost.setId(sendedPostId);
        forSavePost.setUser(UserRepository.query(new UserRepository.UserByIdSpecification(forSavePost.getUserId()))
                .first());
        forSavePost.setMessage(Processor.process(forSavePost.getMessage(), Configuration.builder().forceExtentedProfile().build()));
        sendEmptyMessage();
        PostRepository.add(forSavePost);
    }

    public void requestSendToServerError(Post post) {
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

    public void requestLoadBeforeAndAfter(String searchMessageId) {
        this.limit = "10";
        this.searchMessageId = searchMessageId;
        start(REQUEST_LOAD_FOUND_MESSAGE);
    }

    public void requestLoadBefore() {
        getLastMessageId();
        this.limit = "60";
        start(REQUEST_LOAD_BEFORE);
    }

    public void requestLoadAfter() {
        getFirstMessageId();
        this.limit = "60";
        start(REQUEST_LOAD_AFTER);
    }

    public void requestGetUsers(String search) {
        this.search = search;
        start(REQUEST_DB_GETUSERS);
    }
    //endregion

    // region To View
    private void sendShowEmptyList() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.showEmptyList()));

    }

    private void sendRefreshing(Boolean isShow) {
        createTemplateObservable(isShow).subscribe(split(
                ChatRxFragment::setRefreshing));
    }

    private void sendShowList() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.showList()));
    }

    private void sendOnItemAdded() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.onItemAdded()));
    }

    private void sendIvalidateAdapter() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.invalidateAdapter()));
    }

    private void sendSetDropDown(RealmResults<User> results) {
        createTemplateObservable(results).subscribe(split(
                ChatRxFragment::setDropDown));
    }

    private void sendDisableShowLoadMoreTop() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.disableShowLoadMoreTop()));
    }

    private void sendDisableShowLoadMoreBot() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.disableShowLoadMoreBot()));
    }

    private void sendCanPaginationTop(Boolean b) {
        createTemplateObservable(b).subscribe(split(
                ChatRxFragment::setCanPaginationTop));
    }

    private void sendCanPaginationBot(Boolean b) {
        createTemplateObservable(b).subscribe(split(
                ChatRxFragment::setCanPaginationBot));
    }

    private void sendDropDown(RealmResults<User> users) {
        createTemplateObservable(users).subscribe(split(
                ChatRxFragment::setDropDown));
    }

    private void sendHideFileAttachLayout() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.hideAttachedFilesLayout()));
    }

    private void sendEmptyMessage() {
        createTemplateObservable(new Object())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.setMessage("")));
    }

    private void sendError(String error) {
        createTemplateObservable(error).subscribe(split((chatRxFragment, s) ->
                Toast.makeText(chatRxFragment.getActivity(), s, Toast.LENGTH_LONG).show()));
    }

    private void setErrorLayout(String error) {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, s) ->
                chatRxFragment.setErrorLayout(error)));
    }

    private void sendSlideDialogToFoundMessage() {
        createTemplateObservable(new Object())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.slideToMessageById()));
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
    }

    private void getLastMessageId() {
        RealmResults<Post> realmList = PostRepository.query(new PostByChannelId(channelId));
        if (realmList.size() != 0) {
            lastmessageId = realmList.get(0).getId();
            Log.d(TAG, "lastmessage " + realmList.get(0).getMessage());
        }
    }

    public void getFirstMessageId() {
        RealmResults<Post> realmList = PostRepository.query(new PostByChannelId(channelId));
        if (realmList.size() != 0) {
            firstmessageId = realmList.get(realmList.size() - 1).getId();
            Log.d(TAG, "firstmessage " + realmList.get(realmList.size() - 1).getMessage());
        }
    }

    private void setErrorPost(String sendedPostId) {
        PostRepository.updateUpdateAt(sendedPostId, Post.NO_UPDATE);
        /*Post post = new Post(PostRepository.query(new PostByIdSpecification(sendedPostId)).first());
        post.setUpdateAt(Post.NO_UPDATE);
        Log.d("CreateAt", "setErrorPost: " + post.getCreateAt());
        PostRepository.updateMembers(post);*/
        sendIvalidateAdapter();
    }
}
