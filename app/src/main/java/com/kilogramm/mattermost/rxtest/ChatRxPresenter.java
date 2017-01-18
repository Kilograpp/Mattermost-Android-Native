package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfoRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.member.MembersRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostByIdSpecification;
import com.kilogramm.mattermost.model.entity.post.PostEdit;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusByDirectSpecification;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.kilogramm.mattermost.model.entity.userstatus.UsersStatusByChannelSpecification;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.CommandToNet;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.network.ServerMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 06.10.2016.
 */
public class ChatRxPresenter extends BaseRxPresenter<ChatRxFragment> {

    private static final String TAG = "ChatRxPresenter";

    private static final int REQUEST_EXTRA_INFO = 1;
    private static final int REQUEST_LOAD_POSTS = 2;
    private static final int REQUEST_STATS_INFO = 102;

    /**
     * Code for "send new post to server" request
     *
     * @see #initSendToServer()
     */
    private static final int REQUEST_SEND_TO_SERVER = 4;

    private static final int REQUEST_DELETE_POST = 5;
    private static final int REQUEST_EDIT_POST = 6;
    private static final int REQUEST_UPDATE_LAST_VIEWED_AT = 7;
    private static final int REQUEST_SEND_COMMAND = 15;

    private static final int REQUEST_DB_GETUSERS = 10;
    private static final int REQUEST_DB_USERS_STATUS = 13;
    private static final int REQUEST_DB_USER_STATUS = 14;

    private static final int REQUEST_LOAD_BEFORE = 8;
    private static final int REQUEST_LOAD_AFTER = 9;

    /**
     * Request code for find message operation
     *
     * @see #initLoadBeforeAndAfter()
     */
    private static final int REQUEST_LOAD_FOUND_MESSAGE = 12;
    private static final int REQUEST_HTTP_GETUSERS = 16;

    private ApiMethod service;

    private Toast mToast;

    private Boolean isEmpty = false;

    private String limit;

    private CommandToNet command;

    @State
    String teamId;
    @State
    String channelType;
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
    int cursorPos;
    @State
    Long updateAt;
    @State
    Boolean isSendingPost = false;
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
        infoStats();
        initLoadPosts();
        initSendToServer();
        initUpdateLastViewedAt();
        initDeletePost();
        initEditPost();
        initLoadAfter();
        initLoadBefore();
        initGetUsers();
        initGetStatusUser();
        initGetUserStatusList();
        //initSendToServerError();
        initLoadBeforeAndAfter();
        initSendCommand();
    }

    private void initSendCommand() {
        restartableFirst(REQUEST_SEND_COMMAND,
                () -> ServerMethod.getInstance()
                        .executeCommand(teamId, command)
                        .observeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io()),
                (chatRxFragment, commandFromNet) -> {
                    sendEmptyMessage();
                    if (commandFromNet.getGoToLocation().equals("/"))
                        MattermostApp.logout().subscribe(new Subscriber<LogoutData>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "Complete logout");
                                MattermostApp.clearDataBaseAfterLogout();
                                MattermostApp.clearPreference();
                                MattermostApp.showMainRxActivity();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                Toast.makeText(chatRxFragment.getActivity(), "Error logout", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNext(LogoutData logoutData) {
                            }
                        });
                },
                (chatRxFragment, throwable) -> sendError(getError(throwable)));
    }

    private void initExtraInfo() {
        restartableFirst(REQUEST_EXTRA_INFO, () ->
                        ServerMethod.getInstance()
                                .extraInfo(this.teamId)
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io()),
                (chatRxFragment, channelsWithMembers) -> {
                    ChannelRepository.prepareChannelAndAdd(channelsWithMembers.getChannels(),
                            MattermostPreference.getInstance().getMyUserId());
                    MembersRepository.add(channelsWithMembers.getMembers());
                    channelType = ChannelRepository.query(new
                            ChannelRepository.ChannelByIdSpecification(this.channelId))
                            .first()
                            .getType();
                    setGoodLayout();
                    requestLoadPosts();
                    requestStats();
                }, (chatRxFragment, throwable) -> {
                    if (!isNetworkAvailable()) {
                        sendError(parceError(null, NO_NETWORK));
                        sendShowList();
                    } else {
                        sendError(getError(throwable));
                        setErrorLayout();
                    }
                });
    }

    private void infoStats() {
        restartableFirst(REQUEST_STATS_INFO, () ->
                        ServerMethod.getInstance()
                                .getInfoStatsChannel(teamId,channelId)
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io()),
                (chatRxFragment, extraInfo) -> {
                    ExtroInfoRepository.add(extraInfo);
                    showInfoDefault();
                }, (chatRxFragment, throwable) -> {
                    sendError(getError(throwable));
                });
    }

    private void initLoadPosts() {
        restartableFirst(REQUEST_LOAD_POSTS, () ->
                        ServerMethod.getInstance()
                                .getPosts(teamId, channelId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.computation()),
                (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null || posts.getPosts().size() == 0) {
                        isEmpty = true;
                        sendShowEmptyList(channelId);
                    }
                    List<Observable<List<FileInfo>>> observables = new ArrayList<>();
                    for (Map.Entry<String, Post> entry : posts.getPosts().entrySet()) {
                        if (entry.getValue().getFilenames().size() > 0) {
                            observables.add(service.getFileInfo(teamId, channelId, entry.getValue().getId())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.io()));
                        }
                    }
                    Observable.merge(observables).subscribe(new Subscriber<List<FileInfo>>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "onCompleted: ");
                            mergePosts(posts);
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            sendError("cannot get file");
                            mergePosts(posts);
                        }

                        @Override
                        public void onNext(List<FileInfo> fileInfos) {
                            if (fileInfos == null) return;
                            for (FileInfo fileInfo : fileInfos) {
                                Log.d(TAG, "onNext: " + fileInfo.getId());
                                FileInfoRepository.getInstance().add(fileInfo);
                            }
                        }
                    });
                }, (chatRxFragment1, throwable) -> {
                    sendRefreshing(false);
//                    sendShowList();
                    if (!isNetworkAvailable()) {
                        sendError(parceError(null, NO_NETWORK));
                    } else {
                        sendError(getError(throwable));
                    }
                    throwable.printStackTrace();
                });
    }

    /**
     * Indicates that posts was loaded for {@link #initLoadPosts()} request
     */
    private void sendFinishLoadPosts() {
        sendRefreshing(false);
        if (!isEmpty) {
            sendShowList();
        }
        setGoodLayout();
        Log.d(TAG, "Complete load post");
    }

    /**
     * Merge incoming posts with having ones in DB. For {@link #initLoadPosts()} request
     *
     * @param posts incoming posts from server
     */
    private void mergePosts(Posts posts) {
//        PostRepository.remove(new PostByChannelId(channelId));
        PostRepository.prepareAndAdd(posts);
        PostRepository.merge(posts.getPosts().values(), new PostByChannelId(channelId));
        requestUpdateLastViewedAt();
        sendFinishLoadPosts();
    }

    /**
     * Initialization of "send new post to server" request. Sends the {@link #forSendPost} object
     */
    private void initSendToServer() {
        restartableFirst(REQUEST_SEND_TO_SERVER,
                () -> ServerMethod.getInstance()
                        .sendPost(teamId, channelId, forSendPost)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, post) -> {
                    if (post.getFilenames() != null) {
                        for (String fileId : post.getFilenames()) {
                            FileInfoRepository.getInstance().updatePostId(fileId, post.getId());
                        }
                    }
                    if (PostRepository.query(post.getPendingPostId()) != null) {
                        Log.d(TAG, "initSendToServer: merge from http");
                        PostRepository.merge(post);
                    }
                    requestUpdateLastViewedAt();
                    sendOnItemAdded();
                    sendShowList();
                    FileToAttachRepository.getInstance().deleteUploadedFiles();
                    isSendingPost = false;
                    Log.d(TAG, "Complete create post");
                }, (chatRxFragment1, throwable) -> {
                    isSendingPost = false;
                    sendError(parceError(throwable, null));
                    setErrorPost(forSendPost.getPendingPostId());
                    sendIvalidateAdapter();
                    throwable.printStackTrace();
                    Log.d(TAG, "Error create post " + throwable.getMessage());
                });
    }

    private void initUpdateLastViewedAt() {
        restartableFirst(REQUEST_UPDATE_LAST_VIEWED_AT, () ->
                        ServerMethod.getInstance()
                                .updateLastViewedAt(teamId, channelId)
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
        restartableFirst(REQUEST_DELETE_POST,
                () -> ServerMethod.getInstance()
                        .deletePost(teamId, channelId, forDeletePost.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, post1) -> {
                    sendOnDeleteItem(post1);
                    sendNotifyNearItems();
                },
                (chatRxFragment1, throwable) -> {
                    sendError(throwable.getMessage());
                    throwable.printStackTrace();
                    Log.d(TAG, "Error delete post " + throwable.getMessage());
                });
    }

    private void initEditPost() {
        restartableFirst(REQUEST_EDIT_POST,
                () -> ServerMethod.getInstance()
                        .editPost(teamId, channelId, forEditPost)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, post1) -> {
                    PostRepository.prepareAndAddPost(post1);
                    sendIvalidateAdapter();
                }, (chatRxFragment1, throwable) -> {
                    Post post = new Post(PostRepository.query(new PostByIdSpecification(forEditPost.getId())).first());
                    Log.i("PRFIX", "initEditPost: ");
                    post.setUpdateAt(updateAt);
                    PostRepository.update(post);
                    sendIvalidateAdapter();
                    sendError(throwable.getMessage());
                    Log.d(TAG, "Error edit post " + throwable.getMessage());
                });
    }

    /**
     * Initialization of request for loading posts before (above) post with {@link #lastmessageId}
     */
    private void initLoadBefore() {
        restartableFirst(REQUEST_LOAD_BEFORE,
                () -> {
                    Log.d(TAG, "initLoadBefore");
                    return ServerMethod.getInstance()
                            .getPostsBefore(teamId, channelId, lastmessageId, limit)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io());
                },
                (chatRxFragment, posts) -> {

                    if (posts.getPosts() == null) {
                        sendCanPaginationTop(false);
                        return;
                    }
                    getFilesInfo(posts, false);
                }, (chatRxFragment1, throwable) -> {
                    sendDisableShowLoadMoreTop();
                    sendError(throwable.getMessage());
                    throwable.printStackTrace();

                });
    }

    /**
     * Initialization of request for loading posts after (below) post with {@link #lastmessageId}
     */
    private void initLoadAfter() {
        restartableFirst(REQUEST_LOAD_AFTER,
                () -> ServerMethod.getInstance()
                        .getPostsAfter(teamId, channelId, firstmessageId, limit)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null) {
                        sendCanPaginationBot(false);
                        return;
                    }
                    getFilesInfo(posts, true);
                }, (chatRxFragment1, throwable) -> {
                    sendDisableShowLoadMoreBot();
                    sendError(throwable.getMessage());
                    throwable.printStackTrace();
                });
    }

    /**
     * Get info for all files from given posts list
     *
     * @param posts object from server, containing posts list
     * @param isBot user can load posts above or below the post. This boolean variable indicates
     *              that request was made for above or below posts
     * @see #initLoadBefore()
     * @see #initLoadAfter()
     */
    private void getFilesInfo(Posts posts, boolean isBot) {
        List<Observable<List<FileInfo>>> observables = new ArrayList<>();
        for (Map.Entry<String, Post> entry : posts.getPosts().entrySet()) {
            if (entry.getValue().getFilenames().size() > 0) {
                observables.add(service.getFileInfo(teamId, channelId, entry.getValue().getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()));
            }
        }

        Observable.merge(observables).subscribe(new Subscriber<List<FileInfo>>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted: ");
                PostRepository.prepareAndAdd(posts);
                sendShowList();
                if (isBot) {
                    sendDisableShowLoadMoreBot();
                } else {
                    sendDisableShowLoadMoreTop();
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                sendError("cannot get file");
                sendShowList();
                if (isBot) {
                    sendDisableShowLoadMoreBot();
                } else {
                    sendDisableShowLoadMoreTop();
                }
            }

            @Override
            public void onNext(List<FileInfo> fileInfos) {
                if (fileInfos == null) return;
                for (FileInfo fileInfo : fileInfos) {
                    Log.d(TAG, "onNext: " + fileInfo.getId());
                    FileInfoRepository.getInstance().add(fileInfo);
                }
            }
        });
    }

    private void initGetUsers() {
        restartableFirst(REQUEST_HTTP_GETUSERS,
                () -> ServerMethod.getInstance()
                        .getAutocompleteUsers(MattermostPreference.getInstance().getTeamId(),
                                channelId, search)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io()),
                (chatRxFragment1, autocompleteUsers) -> {
                    chatRxFragment1.setDropDownUser(autocompleteUsers);
                },
                (chatRxFragment, throwable) -> {
                    throwable.printStackTrace();
                });
    }

    private void initGetUserStatusList() {
        restartableFirst(REQUEST_DB_USERS_STATUS,
                () -> UserStatusRepository.query(new UsersStatusByChannelSpecification(channelId))
                        .asObservable(),
                (chatRxFragment, o) -> sendTypingText(o));
    }

    private void initGetStatusUser() {
        restartableFirst(REQUEST_DB_USER_STATUS,
                () -> UserStatusRepository.query(new UserStatusByDirectSpecification(channelId))
                        .asObservable(),
                (chatRxFragment, o) -> {
                    if (o.size() > 0)
                        sendTypingText(o.first());
                    else
                        sendTypingText();
                });
    }

    /**
     * Request initialization for loading posts before and after post
     * with id equal to {@link #searchMessageId}. At first makes requests for
     * previous and future posts relatively current post {@link #searchMessageId}.
     * Then they composed into postsAll variable. At response we clear posts table
     * in DB and add all new posts.
     */
    // TODO прочекать отображение файлов
    private void initLoadBeforeAndAfter() {
        restartableFirst(REQUEST_LOAD_FOUND_MESSAGE,
                () -> ServerMethod.getInstance()
                        .loadBeforeAndAfter(teamId, channelId, searchMessageId, limit)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                , (chatRxFragment, postsAll) -> {
                    PostRepository.remove(new PostByChannelId(channelId));
                    if (postsAll == null) {
                        sendCanPaginationBot(false);
                        return;
                    }
                    try {
                        List<Observable<List<FileInfo>>> observables = new ArrayList<>();
                        for (Posts posts : postsAll) {
                            createObservablesList(observables, posts);
                        }
                        Observable.merge(observables).subscribe(new Subscriber<List<FileInfo>>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "onCompleted: ");
                                for (Posts posts : postsAll) {
                                    PostRepository.prepareAndAdd(posts);
                                }
                                sendRefreshing(false);
                                sendShowList();
                                sendDisableShowLoadMoreBot();
                                sendSlideDialogToFoundMessage();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                sendError(parceError(e,"cannot get file"));
                                sendRefreshing(false);
                                sendShowList();
                                sendDisableShowLoadMoreBot();
                                sendSlideDialogToFoundMessage();
                            }

                            @Override
                            public void onNext(List<FileInfo> fileInfos) {
                                if (fileInfos == null) return;
                                for (FileInfo fileInfo : fileInfos) {
                                    Log.d(TAG, "onNext: " + fileInfo.getId());
                                    FileInfoRepository.getInstance().add(fileInfo);
                                }
                            }
                        });
                    } catch (Throwable e) {
                        e.printStackTrace();
                        sendError(e.getMessage());
                    }
                }, (chatRxFragment, throwable) -> {
                    throwable.printStackTrace();
                    sendError(throwable.getMessage());
                });
    }

    private void createObservablesList(List<Observable<List<FileInfo>>> observables, Posts posts) {
        for (Map.Entry<String, Post> entry : posts.getPosts().entrySet()) {
            if (entry.getValue().getFilenames().size() > 0) {
                observables.add(service.getFileInfo(teamId,
                        channelId, entry.getValue().getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()));
            }
        }
    }
    //endregion

    //region Requests
    public void requestExtraInfo() {
        start(REQUEST_EXTRA_INFO);
    }

    public void requestLoadPosts() {
        start(REQUEST_LOAD_POSTS);
    }

    public void requestStats() {
        start(REQUEST_STATS_INFO);
    }

    public void requestSendToServer(Post post) {
//        if (isSendingPost) return;
        if (FileToAttachRepository.getInstance().haveUnloadedFiles()) return;
        isSendingPost = true;
        forSendPost = post;
        String sendedPostId = post.getPendingPostId();
        post.setId(null);
        Post forSavePost = new Post(forSendPost);
        forSavePost.setId(sendedPostId);
        forSavePost.setUser(UserRepository.query(new UserRepository.UserByIdSpecification(forSavePost.getUserId()))
                .first());
        forSavePost.setFilenames(post.getFilenames());
        //TODO markdown cpp
        //forSavePost.setMessage(Processor.process(forSavePost.getMessage(), Configuration.builder().forceExtentedProfile().build()));
        sendEmptyMessage();
        PostRepository.updateUnsentPosts();// TODO: 12.01.17
        sendIvalidateAdapter();
        PostRepository.add(forSavePost);
        start(REQUEST_SEND_TO_SERVER);

    }

    public void requestSendToServerError(Post post) {
//        if (isSendingPost) return;
        isSendingPost = true;
        forSendPost = post;

//        post.setUpdateAt(null);// TODO: 12.01.17
        Post post1 = PostRepository.query(new PostByIdSpecification(post.getId())).first();
        Realm.getDefaultInstance().executeTransaction(realm -> post1.setUpdateAt(null));
        sendIvalidateAdapter();
        post.setId(null);
        post.setUser(null);
        post.setMessage(Html.fromHtml(post.getMessage()).toString().trim());
        start(REQUEST_SEND_TO_SERVER);
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
        if (firstmessageId != null && !firstmessageId.contains(":")) {
            this.limit = "60";
            start(REQUEST_LOAD_AFTER);
        }
    }

    /*public void requestGetUsers(String search, int cursorPos) {
        this.search = search;
        this.cursorPos = cursorPos;
        start(REQUEST_DB_GETUSERS);
    }*/

    public void requestGetUsers(String search, int cursorPos) {
        this.search = search;
        this.cursorPos = cursorPos;
        start(REQUEST_HTTP_GETUSERS);
    }

    public void requestGetCountUsersStatus() {
        //start(REQUEST_DB_USERS_STATUS);
    }

    public void requestUserStatus() {
        start(REQUEST_DB_USER_STATUS);
    }

    public void requestSendCommand(CommandToNet command) {
        this.command = command;
        start(REQUEST_SEND_COMMAND);
    }
    //endregion

    // region To View
    private void sendShowEmptyList(String channelId) {
        createTemplateObservable(channelId).subscribe(split(
                (chatRxFragment, string) -> chatRxFragment.showEmptyList(channelId)));
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

    private void sendOnDeleteItem(Post post) {
        createTemplateObservable(post).subscribe(split((chatRxFragment, o) ->
                PostRepository.remove(post)));
    }

    private void sendNotifyNearItems() {
        createTemplateObservable(null).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.notifyItem()));
    }

    private void sendIvalidateAdapter() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.invalidateAdapter()));
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

    private void sendTypingText(RealmResults<UserStatus> users) {
        createTemplateObservable(users).subscribe(split(
                (chatRxFragment, userStatuses) -> chatRxFragment.setupTypingText(getStringTyping(users.size()))));
    }

    private void sendTypingText(UserStatus userStatus) {
        createTemplateObservable(userStatus).subscribe(split(
                (chatRxFragment, status) -> chatRxFragment.setupTypingText(status.getStatus())));
    }

    private void sendTypingText() {
        createTemplateObservable(new Object()).subscribe(split(
                (chatRxFragment, status) -> chatRxFragment.setupTypingText(UserStatus.OFFLINE)));
    }

    private void sendTypeChannel() {
        createTemplateObservable(new Object()).subscribe(split(
                (chatRxFragment, o) -> chatRxFragment.initTypingText()));
    }

    String getStringTyping(int count) {
        switch (count) {
            case 0:
                return "No users online";
            case 1:
                return count + " user online";
            default:
                return count + " users online";
        }
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
        createTemplateObservable(error).subscribe(split((chatRxFragment, s) -> {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(chatRxFragment.getActivity(), s, Toast.LENGTH_LONG);
            mToast.show();
        }));
    }


    private void sendInfoChanel(String strInfo) {
        createTemplateObservable(strInfo).subscribe(split((chatRxFragment, s) -> {
            chatRxFragment.setupTypingText(s);
        }));
    }

    private void setErrorLayout() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, s) ->
                chatRxFragment.setMessageLayout(View.GONE)));
    }

    private void setGoodLayout() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, s) ->
                chatRxFragment.setMessageLayout(View.VISIBLE)));
    }

    private void sendSlideDialogToFoundMessage() {
        createTemplateObservable(new Object())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.slideToMessageById()));
    }
    //endregion

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

    public String getChannelType() {
        return channelType;
    }

    private void setErrorPost(String sendedPostId) {
        Log.i("PRFIX", "setErrorPost: WAT DA FUK??!");
        PostRepository.updateUpdateAt(sendedPostId, Post.NO_UPDATE);
        /*Post post = new Post(PostRepository.query(new PostByIdSpecification(sendedPostId)).first());
        post.setUpdateAt(Post.NO_UPDATE);
        Log.d("CreateAt", "setErrorPost: " + post.getCreateAt());
        PostRepository.updateMembers(post);*/
        sendIvalidateAdapter();
    }

    public void showInfoDefault(){

        RealmResults<ExtraInfo> rExtraInfo = ExtroInfoRepository.query(new ExtroInfoRepository.ExtroInfoByIdSpecification(channelId));
        if(rExtraInfo.size()!=0){
            sendInfoChanel(String.format("%s members",rExtraInfo.first().getMember_count()));
        }else sendInfoChanel("");
    }
}
