package com.kilogramm.mattermost.rxtest;

import android.os.Bundle;
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
import com.kilogramm.mattermost.model.entity.post.PostByIdSpecification;
import com.kilogramm.mattermost.model.entity.post.PostEdit;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.fromnet.ChannelWithMember;
import com.kilogramm.mattermost.model.fromnet.CommandToNet;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;
import com.kilogramm.mattermost.model.fromnet.LogoutData;
import com.kilogramm.mattermost.network.ServerMethod;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 21.01.2017.
 */
public class ChatPresenterV2 extends BaseRxPresenter<ChatFragmentV2> {

    private static final String TAG = "ChatPresenterV2";

    private static final String LIMIT_SEARCH_POSTS = "10";
    private static final String LIMIT_NORMAL_LOAD = "60";

    private static final int REQUEST_CHANNEL_BY_ID = 1;
    private static final int REQUEST_LOAD_SEARCH = 2;
    private static final int REQUEST_LOAD_BEFORE = 3;
    private static final int REQUEST_LOAD_AFTER = 4;
    private static final int REQUEST_LOAD_FIRST_PAGE = 5;
    private static final int REQUEST_UPDATE_LAST_VIEWED_AT = 6;
    private static final int REQUEST_DELETE_POST = 7;
    private static final int REQUEST_EDIT_POST = 8;
    private static final int REQUEST_SEND_TO_SERVER = 9;
    private static final int REQUEST_HTTP_GETUSERS = 10;
    private static final int REQUEST_SEND_COMMAND = 11;
    //private static final int REQUEST_SEND_TO_SERVER_ERROR = 10;


    private String mChannelType;
    private String mChannelId;
    private String mTeamId;

    private Toast mToast;

    //variables for requests
    private String mSearchMessageId;
    private String mLastmessageId;
    private String mFirstmessageId;
    private Post mForDeletePost;
    private PostEdit mForEditPost;
    private Post mForSendPost;
    private Long updateAt;
    private Boolean isSendingPost = false;
    private String search;
    private int cursorPos;
    private CommandToNet command;


    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        initRequests();
    }

    public void initPresenter(String teamId, String channelId, String channelType) {
        Log.d(TAG, "initPresenter");
        this.mTeamId = teamId;
        this.mChannelId = channelId;
        this.mChannelType = channelType;
    }

    private void initRequests() {
        initChannelById();
        initLoadSearch();
        initLoadBefore();
        initLoadAfter();
        initLoadPosts();
        initUpdateLastViewedAt();
        initDeletePost();
        initEditPost();
        initSendToServer();
        initGetUsers();
        initSendCommand();
    }


    /**
     * This piece of code written by Initialization of all used requests to a server
     */
    //region INIT_REQUESTS
    private void initChannelById() {
        restartableFirst(REQUEST_CHANNEL_BY_ID,
                () -> Observable.defer(
                        () -> Observable.zip(
                                ServerMethod.getInstance()
                                        .getChannelById(mTeamId, mChannelId),
                                ServerMethod.getInstance()
                                        .getInfoStatsChannel(mTeamId, mChannelId)
                                , ResponseChannelByIdInfo::new))
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, responseChannelByIdInfo) -> {
                    ChannelRepository.add(responseChannelByIdInfo.getChannelWithMember().getChannel());
                    MembersRepository.add(responseChannelByIdInfo.getChannelWithMember().getMember());
                    ExtroInfoRepository.add(responseChannelByIdInfo.getExtraInfo());
                    showInfoDefault();
                    sendStartLoad();
                    //TODO kepar wtf :):):)
                            /*if (isEmpty) {
                                sendShowEmptyList(channelId);
                                isEmpty = false;
                            }*/
                }, (chatRxFragment, throwable) -> {
                    throwable.printStackTrace();
                    sendError(getError(throwable));
                }
        );
    }

    private void initLoadSearch() {
        restartableFirst(REQUEST_LOAD_SEARCH,
                () -> ServerMethod.getInstance()
                        .loadBeforeAndAfter(mTeamId, mChannelId, mSearchMessageId, LIMIT_SEARCH_POSTS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                , (chatRxFragment, postsAll) -> {
                    loadFileInfo(postsAll)
                            .subscribe(new Subscriber<List<FileInfo>>() {
                                @Override
                                public void onCompleted() {
                                    ChatUtils.clearCurrentChatPost(mChannelId);
                                    ChatUtils.savePosts(postsAll);
                                    sendSlideDialogToFoundMessage();
                                    sendEnablePaginationTopAndBot();
                                    sendVisiblePrograssBar(false);
                                    sendShowList();
                                    sendFragmentState(ChatFragmentV2.StateFragment.STATE_NORMAL);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    sendShowErrorLayout();
                                }

                                @Override
                                public void onNext(List<FileInfo> fileInfos) {
                                    ChatUtils.saveFileInfo(fileInfos);
                                }
                            });
                }, (chatRxFragment, throwable) -> {
                    throwable.printStackTrace();
                    sendShowErrorLayout();
                    sendError(parceError(throwable, null));
                });
    }

    private void initLoadBefore() {
        restartableFirst(REQUEST_LOAD_BEFORE,
                () -> {
                    Log.d(TAG, "initLoadBefore");
                    return ServerMethod.getInstance()
                            .getPostsBefore(mTeamId, mChannelId, mLastmessageId, LIMIT_NORMAL_LOAD)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io());
                },
                (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null) {
                        sendCanPaginationTop(false);
                        sendDisableShowLoadMoreTop();
                        return;
                    }
                    List<Posts> list = new ArrayList<>();
                    list.add(posts);
                    loadFileInfo(list).subscribe(new Subscriber<List<FileInfo>>() {
                        @Override
                        public void onCompleted() {
                            ChatUtils.savePosts(posts);
                            sendDisableShowLoadMoreTop();
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            String error = parceError(e, BaseRxPresenter.UPLOAD_A_FILE);
                            if (error != null) {
                                sendError(error);
                            }
                            sendDisableShowLoadMoreTop();
                        }

                        @Override
                        public void onNext(List<FileInfo> fileInfos) {
                            ChatUtils.saveFileInfo(fileInfos);
                        }
                    });
                }, (chatRxFragment1, throwable) -> {
                    sendDisableShowLoadMoreTop();
                    sendError(parceError(throwable, null));
                    throwable.printStackTrace();

                });
    }

    private void initLoadAfter() {
        restartableFirst(REQUEST_LOAD_AFTER,
                () -> ServerMethod.getInstance()
                        .getPostsAfter(mTeamId, mChannelId, mFirstmessageId, LIMIT_NORMAL_LOAD)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null) {
                        sendCanPaginationBot(false);
                        sendDisableShowLoadMoreBot();
                        return;
                    }
                    List<Posts> list = new ArrayList<>();
                    list.add(posts);
                    loadFileInfo(list).subscribe(new Subscriber<List<FileInfo>>() {
                        @Override
                        public void onCompleted() {
                            ChatUtils.savePosts(list);
                            sendDisableShowLoadMoreBot();
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            String error = parceError(e, BaseRxPresenter.UPLOAD_A_FILE);
                            if (error != null) {
                                sendError(error);
                            }
                            sendDisableShowLoadMoreBot();
                        }

                        @Override
                        public void onNext(List<FileInfo> fileInfos) {
                            ChatUtils.saveFileInfo(fileInfos);
                        }
                    });
                }, (chatRxFragment1, throwable) -> {
                    sendDisableShowLoadMoreBot();
                    sendError(parceError(throwable, null));
                    throwable.printStackTrace();
                });
    }

    private void initLoadPosts() {
        restartableFirst(REQUEST_LOAD_FIRST_PAGE, () ->
                        ServerMethod.getInstance()
                                .getPosts(mTeamId, mChannelId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.computation()),
                (chatRxFragment, posts) -> {
                    if (posts.getPosts() == null || posts.getPosts().size() == 0) {
                        sendShowEmptyList(mChannelId);
                    } else {
                        List<Posts> list = new ArrayList<>();
                        list.add(posts);
                        loadFileInfo(list).subscribe(new Subscriber<List<FileInfo>>() {
                            @Override
                            public void onCompleted() {
                                ChatUtils.mergePosts(posts, mChannelId);
                                startRequestUpdateLastViewedAt();
                                sendEnableTopPagination();
                                sendRefreshing(false);
                                sendVisiblePrograssBar(false);
                                sendShowList();
                                setGoodLayout();
                                //sendFinishLoadPosts();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                String error = parceError(e, BaseRxPresenter.UPLOAD_A_FILE);
                                if (error != null) {
                                    sendError(error);
                                }
                                ChatUtils.mergePosts(posts, mChannelId);
                                startRequestUpdateLastViewedAt();
                                sendRefreshing(false);
                                sendEnableTopPagination();
                                sendVisiblePrograssBar(false);
                                sendShowList();
                                setGoodLayout();
                            }

                            @Override
                            public void onNext(List<FileInfo> fileInfos) {
                                ChatUtils.saveFileInfo(fileInfos);
                            }
                        });
                    }
                }, (chatRxFragment1, throwable) -> {
                    sendRefreshing(false);
                    sendEnableTopPagination();
                    sendVisiblePrograssBar(false);
                    sendShowList();
                    setGoodLayout();
                    if (!isNetworkAvailable()) {
                        sendError(parceError(null, NO_NETWORK));
                    } else {
                        sendError(parceError(throwable, null));
                    }
                    throwable.printStackTrace();
                });
    }

    private void initUpdateLastViewedAt() {
        restartableFirst(REQUEST_UPDATE_LAST_VIEWED_AT, () ->
                        ServerMethod.getInstance()
                                .updateLastViewedAt(mTeamId, mChannelId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io()),
                (chatRxFragment, post) -> {
                }, (chatRxFragment1, throwable) -> {
                    sendError(parceError(throwable, null));
                    throwable.printStackTrace();
                    Log.d(TAG, "Error");
                });
    }

    private void initDeletePost() {
        restartableFirst(REQUEST_DELETE_POST,
                () -> ServerMethod.getInstance()
                        .deletePost(mTeamId, mChannelId, mForDeletePost.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, post1) -> {
                    ChatUtils.removePost(post1);
/*
                    sendOnDeleteItem(post1);
                    sendNotifyNearItems();
*/
                },
                (chatRxFragment1, throwable) -> {
                    sendError(parceError(throwable, null));
                    throwable.printStackTrace();
                    Log.d(TAG, "Error delete post " + throwable.getMessage());
                });
    }

    private void initEditPost() {
        restartableFirst(REQUEST_EDIT_POST,
                () -> ServerMethod.getInstance()
                        .editPost(mTeamId, mChannelId, mForEditPost)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, post) -> {
                    PostRepository.prepareAndAddPost(post);
                    sendIvalidateAdapter();
                }, (chatRxFragment1, throwable) -> {
                    ChatUtils.setPostUpdateAt(mForEditPost.getId(), updateAt);
                    sendIvalidateAdapter();
                    sendError(parceError(throwable, null));
                    Log.d(TAG, "Error edit post " + throwable.getMessage());
                });
    }

    private void initSendToServer() {
        restartableFirst(REQUEST_SEND_TO_SERVER,
                () -> ServerMethod.getInstance()
                        .sendPost(mTeamId, mChannelId, mForSendPost)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io()),
                (chatRxFragment, post) -> {
                    if (post.getFilenames() != null) {
                        for (String fileId : post.getFilenames()) {
                            FileInfoRepository.getInstance().updatePostId(fileId, post.getId());
                        }
                    }
                    PostRepository.mergeSendedPost(post);
                    /*if (PostRepository.query(post.getPendingPostId()) != null) {
                        Log.d(TAG, "initSendToServer: merge from http");
                        PostRepository.mergeWithDelete(post);
                    } else {
                        Log.d(TAG, "initSendToServer: post pending id not found");
                    }*/
                    startRequestUpdateLastViewedAt();
                    // sendOnItemAdded();
                    sendShowList();
                    FileToAttachRepository.getInstance().deleteUploadedFiles();
                    isSendingPost = false;
                    Log.d(TAG, "Complete create post");
                }, (chatRxFragment1, throwable) -> {
                    isSendingPost = false;
                    sendError(parceError(throwable, "Can't send message"));
                    setErrorPost(mForSendPost.getPendingPostId());
                    sendIvalidateAdapter();
                    throwable.printStackTrace();
                    Log.d(TAG, "Error create post " + throwable.getMessage());
                });
    }

    private void initGetUsers() {
        restartableFirst(REQUEST_HTTP_GETUSERS,
                () -> ServerMethod.getInstance()
                        .getAutocompleteUsers(MattermostPreference.getInstance().getTeamId(),
                                mChannelId, search)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io()),
                (chatRxFragment1, autocompleteUsers) -> {
                    chatRxFragment1.setDropDownUser(autocompleteUsers);
                },
                (chatRxFragment, throwable) -> {
                    throwable.printStackTrace();
                });
    }

    private void initSendCommand() {
        restartableFirst(REQUEST_SEND_COMMAND,
                () -> ServerMethod.getInstance()
                        .executeCommand(mTeamId, command)
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
                (chatRxFragment, throwable) -> sendError(parceError(throwable, null)));
    }
    //endregion


    //region START_REQUEST_METHODS

    /**
     * @see #initChannelById()
     */
    public void startLoadInfoChannel() {
        start(REQUEST_CHANNEL_BY_ID);
    }


    /**
     * @see #initLoadPosts()
     */
    public void startRequestLoadNormal() {
        start(REQUEST_LOAD_FIRST_PAGE);
    }

    /**
     * @param searchMessageId
     * @see #initLoadSearch()
     */
    public void startRequestLoadSearch(String searchMessageId) {
        this.mSearchMessageId = searchMessageId;
        start(REQUEST_LOAD_SEARCH);
    }

    /**
     * @see #initLoadBefore()
     */
    public void requestLoadBefore() {
        this.mLastmessageId = ChatUtils.getLastMessageId(mChannelId);
        if (mLastmessageId != null && !mLastmessageId.contains(":")) {
            start(REQUEST_LOAD_BEFORE);
        }
    }

    /**
     * @see #initLoadAfter()
     */
    public void requestLoadAfter() {
        this.mFirstmessageId = ChatUtils.getFirstMessageId(mChannelId);
        if (mFirstmessageId != null && !mFirstmessageId.contains(":")) {
            start(REQUEST_LOAD_AFTER);
        }
    }

    public void startRequestUpdateLastViewedAt() {
        start(REQUEST_UPDATE_LAST_VIEWED_AT);
    }

    /**
     * @see #initDeletePost()
     */
    public void requestDeletePost(Post post) {
        mForDeletePost = post;
        start(REQUEST_DELETE_POST);
    }

    /**
     * @see #initEditPost()
     */
    public void requestEditPost(PostEdit post) {
        mForEditPost = post;
        start(REQUEST_EDIT_POST);
        ChatUtils.setPostUpdateAt(mForEditPost.getId(), null);
        sendIvalidateAdapter();
    }

    public void requestSendToServer(Post post) {
//        if (isSendingPost) return;
        Log.d(TAG, "requestSendToServer() called with: post = [" + post + "]");
        if (FileToAttachRepository.getInstance().haveUnloadedFiles()) return;
        isSendingPost = true;
        mForSendPost = post;
        String sendedPostId = post.getPendingPostId();
        post.setId(null);
        Post forSavePost = new Post(mForSendPost);
        forSavePost.setId(sendedPostId);
        forSavePost.setUser(UserRepository.query(new UserRepository.UserByIdSpecification(forSavePost.getUserId()))
                .first());
        forSavePost.setFilenames(post.getFilenames());
        sendEmptyMessage();
        PostRepository.updateUnsentPosts();
        sendIvalidateAdapter();
        PostRepository.add(forSavePost);
        Log.d(TAG, "requestSendToServer: req start");
        start(REQUEST_SEND_TO_SERVER);
    }

    public void requestSendToServerError(Post post) {
        isSendingPost = true;
        mForSendPost = post;
        Post post1 = PostRepository.query(new PostByIdSpecification(post.getId())).first();
        Realm.getDefaultInstance().executeTransaction(realm -> post1.setUpdateAt(null));
        sendIvalidateAdapter();
        post.setId(null);
        post.setUser(null);
        post.setMessage(Html.fromHtml(post.getMessage()).toString().trim());
        start(REQUEST_SEND_TO_SERVER);
    }

    public void requestGetUsers(String search, int cursorPos) {
        this.search = search;
        this.cursorPos = cursorPos;
        if (search != null && !search.equals("")) {
            int lastindex = this.search.lastIndexOf("@");
            this.search = this.search.substring(lastindex);
        }
        start(REQUEST_HTTP_GETUSERS);
    }

    public void requestSendCommand(CommandToNet command) {
        this.command = command;
        start(REQUEST_SEND_COMMAND);
    }
    //endregion

    //region send methods

    private void sendStartLoad() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.startLoad()));
    }

    private void sendVisiblePrograssBar(Boolean b) {
        createTemplateObservable(b)
                .subscribe(split(ChatFragmentV2::setVisibleProgressBar));
    }

    private void sendSlideDialogToFoundMessage() {
        createTemplateObservable(new Object())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.slideToMessageById()));
    }

    private void sendShowList() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.showList()));
    }

    private void sendEnablePaginationTopAndBot() {
        createTemplateObservable(new Object()).subscribe(split((chatFragmentV2, o) ->
                chatFragmentV2.enablePaginationTopAndBot()));
    }

    private void sendFragmentState(ChatFragmentV2.StateFragment stateNormal) {
        createTemplateObservable(stateNormal)
                .subscribe(split(ChatFragmentV2::setStateFragment));
    }

    private void sendError(String error) {
        createTemplateObservable(error).subscribe(split((chatRxFragment, s) -> {
            if (mToast != null) mToast.cancel();
            mToast = Toast.makeText(chatRxFragment.getActivity(), s, Toast.LENGTH_LONG);
            mToast.show();
        }));
    }

    private void sendShowErrorLayout() {
        createTemplateObservable(new Object()).subscribe(split((chatFragmentV2, o) -> chatFragmentV2.showErrorLayout()));
    }

    private void sendInfoChanel(String strInfo) {
        createTemplateObservable(strInfo).subscribe(split(ChatFragmentV2::setupTypingText));
    }

    private void sendCanPaginationTop(Boolean b) {
        createTemplateObservable(b).subscribe(split(
                ChatFragmentV2::setCanPaginationTop));
    }

    private void sendCanPaginationBot(Boolean b) {
        createTemplateObservable(b).subscribe(split(
                ChatFragmentV2::setCanPaginationBot));
    }

    private void sendDisableShowLoadMoreTop() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.disableShowLoadMoreTop()));
    }

    private void sendDisableShowLoadMoreBot() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.disableShowLoadMoreBot()));
    }

    private void sendShowEmptyList(String channelId) {
        createTemplateObservable(channelId).subscribe(split(
                (chatRxFragment, string) -> chatRxFragment.showEmptyList(channelId)));
    }

    private void sendRefreshing(Boolean isShow) {
        createTemplateObservable(isShow).subscribe(split(
                ChatFragmentV2::setRefreshing));
    }

    private void sendFinishLoadPosts() {
        sendRefreshing(false);
        sendVisiblePrograssBar(false);
        sendShowList();
        setGoodLayout();
        Log.d(TAG, "Complete load post");
    }

    private void setGoodLayout() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, s) ->
                chatRxFragment.setMessageLayout(View.VISIBLE)));
    }

    private void sendIvalidateAdapter() {
        createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.invalidateAdapter()));
    }

    private void sendEmptyMessage() {
        createTemplateObservable(new Object())
                .subscribe(split((chatRxFragment, o) -> chatRxFragment.setMessage("")));
    }

    private void sendEnableTopPagination() {
        createTemplateObservable(new Object()).subscribe(split((chatFragmentV2, o) -> chatFragmentV2.enableTopPagination()));
    }

    private void sendEnableBotPagination() {
        createTemplateObservable(new Object()).subscribe(split((chatFragmentV2, o) -> chatFragmentV2.enableBotPagination()));
    }

    private void sendEnableAllPagination() {
        createTemplateObservable(new Object()).subscribe(split((chatFragmentV2, o) -> chatFragmentV2.enableAllPagination()));
    }

    private void sendDisableAllPagination() {
        createTemplateObservable(new Object()).subscribe(split((chatFragmentV2, o) -> chatFragmentV2.disablePagination()));
    }


    //endregion


    private Observable<List<FileInfo>> loadFileInfo(List<Posts> postsAll) {
        Iterable<Observable<List<FileInfo>>> observables = new ArrayList<>();
        for (Posts posts : postsAll) {
            ChatUtils.createObservablesList((List<Observable<List<FileInfo>>>) observables, posts, mTeamId, mChannelId);
        }
        return Observable.merge(observables);
    }

    public void showInfoDefault() {
        if (ChatUtils.isDirectChannel(mChannelType)) {
            sendInfoChanel(ChatUtils.getInfoChannelDirect(mChannelId));
        } else {
            sendInfoChanel(ChatUtils.getInfoChannelNotDirect(mChannelId));
        }

    }

    private void setErrorPost(String sendedPostId) {
        ChatUtils.setPostUpdateAt(sendedPostId, Post.NO_UPDATE);
        sendIvalidateAdapter();
    }


    public static class ResponseChannelByIdInfo {

        private ChannelWithMember channelWithMember;
        private ExtraInfo extraInfo;

        public ResponseChannelByIdInfo(ChannelWithMember channelWithMember, ExtraInfo extraInfo) {
            this.channelWithMember = channelWithMember;
            this.extraInfo = extraInfo;
        }

        public ChannelWithMember getChannelWithMember() {
            return channelWithMember;
        }

        public ExtraInfo getExtraInfo() {
            return extraInfo;
        }
    }

}