package com.kilogramm.mattermost.presenter;

import com.kilogramm.mattermost.view.chat.ChatFragmentMVP;

import nucleus.presenter.Presenter;

/**
 * Created by Evgeny on 13.09.2016.
 */
public class ChatPresenter extends Presenter<ChatFragmentMVP> {
//    private static final String TAG = "ChatPresenter";
//
//    private Subscription mSubscription;
//
//    private MattermostApp mMattermostApp;
//
//    private Boolean isEmpty = false;
//    private Boolean isLoadNext = true;
//    private boolean isPosting = false;
//
//    private PostRepository postRepository;
//    private UserRepository userRepository;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedState) {
//        super.onCreate(savedState);
//        mMattermostApp = MattermostApp.getSingleton();
//        postRepository = new PostRepository();
//        userRepository = new UserRepository();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//    }
//    //===============================Methods======================================================
//
//    public void getExtraInfo(String teamId, String channelId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//
//
//        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.getExtraInfoChannel(teamId, channelId)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(Schedulers.io())
//                .subscribe(new Subscriber<ExtraInfo>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "Complete load extra_info");
//                        loadPosts(teamId, channelId);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        Log.d(TAG, "Error extra_info");
//                    }
//
//                    @Override
//                    public void onNext(ExtraInfo extraInfo) {
//                        userRepository.add(extraInfo.getMembers());
//                    }
//                });
//    } //  +
//
//
//    public Post getRootPost(Post postBase) {
//        RealmResults<Post> postsList = postRepository.query((new PostByIdSpecification(postBase.getRootId())));
//        Post rootPost = null;
//        if (postsList.size() > 0)
//            rootPost = postsList.first();
//        if (rootPost != null)
//            return rootPost;
//        return null;
//    }
//
//    //TODO много повторяющегося кода в этих методах
//    public void loadPosts(String teamId, String channelId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.getPosts(teamId, channelId)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Posts>() {
//                    @Override
//                    public void onCompleted() {
//                        updateLastViewedAt(teamId, channelId);
//
//                        getView().setRefreshing(false);
//                        if (!isEmpty) {
//                            getView().showList();
//                        }
//                        if (isLoadNext) {
//                            loadNextPost(teamId, channelId, getLastMessageId());
//                        }
//                        Log.d(TAG, "Complete load post");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        getView().setRefreshing(false);
//                        e.printStackTrace();
//                        Log.d(TAG, "Error");
//                    }
//
//                    @Override
//                    public void onNext(Posts posts) {
//                        if (posts.getPosts() == null || posts.getPosts().size() == 0) {
//                            isEmpty = true;
//                            isLoadNext = false;
//                            getView().showEmptyList();
//                        }
//                        RealmList<Post> realmList = new RealmList<>();
//                        for (Post post : posts.getPosts().values()) {
//                            if (!post.isSystemMessage())
//                                post.setUser(userRepository.query(new UserByIdSpecification(post.getUserId())).first());
//                            else
//                                post.setUser(new User("System", "System", "System"));
//                            post.setViewed(true);
//                            post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//                        }
//                        realmList.addAll(posts.getPosts().values());
//                        postRepository.add(realmList);
//                        if (realmList.size() < 60) {
//                            isLoadNext = false;
//                        }
//                    }
//                });
//    } // +
//
//    public void loadNextPost(String teamId, String channelId, String lastMessageId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.getPostsBefore(teamId, channelId, lastMessageId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Posts>() {
//                    @Override
//                    public void onCompleted() {
//                        if (getView() != null)
//                            getView().showList();
//                        else {
//                            if (isLoadNext) {
//                                loadNextPost(teamId, channelId, getLastMessageId());
//                            }
//                            Log.d(TAG, "Complete load next post");
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onNext(Posts posts) {
//                        if (posts.getPosts() == null) {
//                            isLoadNext = false;
//                            return;
//                        }
//                        RealmList<Post> realmList = new RealmList<>();
//                        for (Post post : posts.getPosts().values()) {
//                            User user = userRepository.query(new UserByIdSpecification(post.getUserId())).first();
//                            post.setUser(user);
//                            post.setViewed(true);
//                            post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//                        }
//                        realmList.addAll(posts.getPosts().values());
//                        postRepository.add(realmList);
//                        if (realmList.size() < 60) {
//                            isLoadNext = false;
//                        }
//                    }
//                });
//    }
//
//    /********************************
//     * for search
//     **************************/
//
//    public void loadPostsAfter(String teamId, String channelId, String postId, String offset, String limit) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//
//        mSubscription = service.getPostsAfter(teamId, channelId, postId, offset, limit)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Posts>() {
//                    @Override
//                    public void onCompleted() {
//                        if (getView() != null)
//                            getView().showList();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onNext(Posts posts) {
//                        RealmList<Post> realmList = new RealmList<>();
//                        for (Post post : posts.getPosts().values()) {
//                            User user = userRepository.query(new UserByIdSpecification(post.getUserId())).first();
//                            post.setUser(user);
//                            post.setViewed(true);
//                            post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//                        }
//                        realmList.addAll(posts.getPosts().values());
//                        postRepository.add(realmList);
//                    }
//                });
//    }
//
//    public void loadPostsBefore(String teamId, String channelId, String postId, String offset, String limit) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//
//        mSubscription = service.getPostsBefore_search(teamId, channelId, postId, offset, limit)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Posts>() {
//                    @Override
//                    public void onCompleted() {
//                        if (getView() != null)
//                            getView().showList();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onNext(Posts posts) {
//
//                        RealmList<Post> realmList = new RealmList<>();
//                        for (Post post : posts.getPosts().values()) {
//                            User user = userRepository.query(new UserByIdSpecification(post.getUserId())).first();
//                            post.setUser(user);
//                            post.setViewed(true);
//                            post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//                        }
//                        realmList.addAll(posts.getPosts().values());
//                        postRepository.add(realmList);
//                    }
//                });
//    }
//
//    /*******************************************************************************/
//
//    public void getUsers(String search) {
//        RealmResults<User> users = userRepository.query(new UserByNameSearchSpecification(search));
//        getView().setDropDown(users);
//    }
//
//    private String getLastMessageId() {
//        String id;
//        RealmResults<Post> realmList = postRepository.query(new PostByChannelId(getView().getChId()));
//        id = realmList.get(0).getId();
//        Log.d(TAG, "lastmessage " + realmList.get(0).getMessage());
//        return id;
//    }
//
//    public void sendToServerError(Post sendedPost, String teamId, String channelId) {
//        if (FileToAttachRepository.getInstance().haveUnloadedFiles()) {
//            return;
//        }
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//
//        String sendedPostId = sendedPost.getPendingPostId();
//        sendedPost.setUpdateAt(null);
//        postRepository.update(sendedPost);
//        getView().refreshItem();
//        sendedPost.setId(null);
//        sendedPost.setUser(null);
//        sendedPost.setMessage(Html.fromHtml(sendedPost.getMessage()).toString().trim());
//        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.sendPost(teamId, channelId, sendedPost)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//                        updateLastViewedAt(teamId, channelId);
//                        getView().onItemAdded();
//                        Log.d(TAG, "Complete create post");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        setErrorPost(sendedPostId);
//                        e.printStackTrace();
//                        Log.d(TAG, "Error repetition create post " + e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//                        setpostData(post, sendedPostId);
//                    }
//                });
//    }
//
//    public void sendToServer(Post sendedPost, String teamId, String channelId) {
//        if (isPosting) return;
//        if (FileToAttachRepository.getInstance().haveUnloadedFiles()) {
//            return;
//        }
//        isPosting = true;
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        String sendedPostId = sendedPost.getPendingPostId();
//        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.sendPost(teamId, channelId, sendedPost)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//                        updateLastViewedAt(teamId, channelId);
//                        getView().onItemAdded();
//                        Log.d(TAG, "Complete create post");
//                        FileToAttachRepository.getInstance().clearData();
//                        getView().hideAttachedFilesLayout();
//                        getView().setMessage("");
//                        isPosting = false;
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        setErrorPost(sendedPostId);
//                        e.printStackTrace();
//                        Log.d(TAG, "Error create post " + e.getMessage());
//                        isPosting = false;
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//                        setpostData(post, sendedPostId);
//                    }
//                });
//        Post forSavePost = new Post(sendedPost);
//        forSavePost.setId(sendedPostId);
//        forSavePost.setUser(userRepository.query(new UserByIdSpecification(forSavePost.getUserId()))
//                .first());
//        forSavePost.setMessage(Processor.process(forSavePost.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//        postRepository.add(forSavePost);
//    }
//
//
//    private void setErrorPost(String sendedPostId) {
//        Post post = new Post(postRepository.query(new PostByIdSpecification(sendedPostId)).first());
//        post.setUpdateAt(Post.NO_UPDATE);
//        Log.d("CreateAt", "setErrorPost: " + post.getCreateAt());
//        postRepository.update(post);
//        getView().refreshItem();
//    }
//
//    private void setpostData(Post post, String sendedPostId) {
//        post.setUser(userRepository.query(new UserByIdSpecification(post.getUserId())).first());
//        post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//        Log.d("CreateAt", "setpostData: " + post.getCreateAt());
//        postRepository.remove(new PostByPendingPostIdSpecification(sendedPostId));
//        postRepository.add(post);
//    }
//
//    private void updateLastViewedAt(String teamId, String channelId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.updatelastViewedAt(teamId, channelId)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(Schedulers.io())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "Complete update last viewed at");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        Log.d(TAG, "Error");
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//                    }
//                });
//    }
//
//    public void initLoadNext() {
//        isLoadNext = true;
//    }
//
//    public void uploadFileToServer(Context context, String teamId, String channel_id, Uri uri) {
//        String filePath = FileUtils.getPath(context, uri);
//        String mimeType = FileUtils.getMimeType(filePath);
//        File file = new File(filePath);
//        if (file.exists()) {
//            ProgressRequestBody fileBody = new ProgressRequestBody(file, mimeType, new ProgressRequestBody.UploadCallbacks() {
//                @Override
//                public void onProgressUpdate(int percentage) {
//                    Log.d(TAG, String.format("Progress: %d", percentage));
//                }
//
//                @Override
//                public void onError() {
//
//                }
//
//                @Override
//                public void onFinish() {
//
//                }
//            });
//            MultipartBody.Part filePart = MultipartBody.Part.createFormData("files", file.getName(), fileBody);
//            RequestBody channelId = RequestBody.create(MediaType.parse("multipart/form-data"), channel_id);
//            RequestBody clientId = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());
//
//            if (mSubscription != null && !mSubscription.isUnsubscribed())
//                mSubscription.unsubscribe();
//            ApiMethod service = mMattermostApp.getMattermostRetrofitService();
//
//            mSubscription = service.uploadFile(teamId, filePart, channelId, clientId)
//                    .subscribeOn(Schedulers.newThread())
//                    .observeOn(Schedulers.io())
//                    .subscribe(new Subscriber<FileUploadResponse>() {
//                        @Override
//                        public void onCompleted() {
//                            Log.d(TAG, "Complete update last viewed at");
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            e.printStackTrace();
//                            Log.d(TAG, "Error");
//                        }
//
//                        @Override
//                        public void onNext(FileUploadResponse fileUploadResponse) {
//                            Log.d(TAG, fileUploadResponse.toString());
//                        }
//                    });
//        } else {
//            Log.e(TAG, "file not found");
//        }
//    }
//
//    public void deleteErrorSendPost(Post post) {
//        postRepository.remove(post);
//    }
//
//    public void deletePost(Post post, String teamId, String channelId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.deletePost(teamId, channelId, post.getId(), new Object())
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "Complete delete post");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        Log.d(TAG, "Error delete post " + e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//                        postRepository.remove(post);
//                    }
//                });
//    }
//
//    public void editPost(PostEdit post, String teamId, String channelId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.editPost(teamId, channelId, post)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "Complete edit post");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        Log.d(TAG, "Error edit post " + e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//                        post.setUser(userRepository.query(new UserByIdSpecification(post.getUserId())).first());
//                        post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//                        postRepository.update(post);
//                        getView().invalidateAdapter();
//                    }
//                });
//    }
}
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//    }
//    //===============================Methods======================================================
//
//    public void getExtraInfo(String teamId, String channelId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//
//
//
//        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.getExtraInfoChannel(teamId, channelId)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(Schedulers.io())
//                .subscribe(new Subscriber<ExtraInfo>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "Complete load extra_info");
//                        loadPosts(teamId, channelId);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        Log.d(TAG, "Error extra_info");
//                    }
//
//                    @Override
//                    public void onNext(ExtraInfo extraInfo) {
//                        userRepository.add(extraInfo.getMembers());
//                    }
//                });
//    } //  +
//
//
//    public Post getRootPost(Post postBase) {
//        RealmResults<Post> postsList = postRepository.query((new PostByIdSpecification(postBase.getRootId())));
//        Post rootPost = null;
//        if (postsList.size() > 0)
//            rootPost = postsList.first();
//        if (rootPost != null)
//            return rootPost;
//        return null;
//    }
//
//    //TODO много повторяющегося кода в этих методах
//    public void loadPosts(String teamId, String channelId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.getPosts(teamId, channelId)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Posts>() {
//                    @Override
//                    public void onCompleted() {
//                        updateLastViewedAt(teamId, channelId);
//
//                        getView().setRefreshing(false);
//                        if (!isEmpty) {
//                            getView().showList();
//                        }
//                        if (isLoadNext) {
//                            loadNextPost(teamId, channelId, getLastMessageId());
//                        }
//                        Log.d(TAG, "Complete load post");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        getView().setRefreshing(false);
//                        e.printStackTrace();
//                        Log.d(TAG, "Error");
//                    }
//
//                    @Override
//                    public void onNext(Posts posts) {
//                        if (posts.getPosts() == null || posts.getPosts().size() == 0) {
//                            isEmpty = true;
//                            isLoadNext = false;
//                            getView().showEmptyList();
//                        }
//                        RealmList<Post> realmList = new RealmList<>();
//                        for (Post post : posts.getPosts().values()) {
//                            if (!post.isSystemMessage())
//                                post.setUser(userRepository.query(new UserByIdSpecification(post.getUserId())).first());
//                            else
//                                post.setUser(new User("System", "System", "System"));
//                            post.setViewed(true);
//                            post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//                        }
//                        realmList.addAll(posts.getPosts().values());
//                        postRepository.add(realmList);
//                        if (realmList.size() < 60) {
//                            isLoadNext = false;
//                        }
//                    }
//                });
//    } // +
//
//    public void loadNextPost(String teamId, String channelId, String lastMessageId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.getPostsBefore(teamId, channelId, lastMessageId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Posts>() {
//                    @Override
//                    public void onCompleted() {
//                        if (getView() != null)
//                            getView().showList();
//                        else {
//                            if (isLoadNext) {
//                                loadNextPost(teamId, channelId, getLastMessageId());
//                            }
//                            Log.d(TAG, "Complete load next post");
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onNext(Posts posts) {
//                        if (posts.getPosts() == null) {
//                            isLoadNext = false;
//                            return;
//                        }
//                        RealmList<Post> realmList = new RealmList<>();
//                        for (Post post : posts.getPosts().values()) {
//                            User user = userRepository.query(new UserByIdSpecification(post.getUserId())).first();
//                            post.setUser(user);
//                            post.setViewed(true);
//                            post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//                        }
//                        realmList.addAll(posts.getPosts().values());
//                        postRepository.add(realmList);
//                        if (realmList.size() < 60) {
//                            isLoadNext = false;
//                        }
//                    }
//                });
//    }
//
//    /********************************
//     * for search
//     **************************/
//
//    public void loadPostsAfter(String teamId, String channelId, String postId, String offset, String limit) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//
//        mSubscription = service.getPostsAfter(teamId, channelId, postId, offset, limit)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Posts>() {
//                    @Override
//                    public void onCompleted() {
//                        if (getView() != null)
//                            getView().showList();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onNext(Posts posts) {
//                        RealmList<Post> realmList = new RealmList<>();
//                        for (Post post : posts.getPosts().values()) {
//                            User user = userRepository.query(new UserByIdSpecification(post.getUserId())).first();
//                            post.setUser(user);
//                            post.setViewed(true);
//                            post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//                        }
//                        realmList.addAll(posts.getPosts().values());
//                        postRepository.add(realmList);
//                    }
//                });
//    }
//
//    public void loadPostsBefore(String teamId, String channelId, String postId, String offset, String limit) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//
//        mSubscription = service.getPostsBefore_search(teamId, channelId, postId, offset, limit)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Posts>() {
//                    @Override
//                    public void onCompleted() {
//                        if (getView() != null)
//                            getView().showList();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                    }
//
//                    @Override
//                    public void onNext(Posts posts) {
//
//                        RealmList<Post> realmList = new RealmList<>();
//                        for (Post post : posts.getPosts().values()) {
//                            User user = userRepository.query(new UserByIdSpecification(post.getUserId())).first();
//                            post.setUser(user);
//                            post.setViewed(true);
//                            post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//                        }
//                        realmList.addAll(posts.getPosts().values());
//                        postRepository.add(realmList);
//                    }
//                });
//    }
//
//    /*******************************************************************************/
//
//    public void getUsers(String search) {
//        RealmResults<User> users = userRepository.query(new UserByNameSearchSpecification(search));
//        getView().setDropDown(users);
//    }
//
//    private String getLastMessageId() {
//        String id;
//        RealmResults<Post> realmList = postRepository.query(new PostByChannelId(getView().getChId()));
//        id = realmList.get(0).getId();
//        Log.d(TAG, "lastmessage " + realmList.get(0).getMessage());
//        return id;
//    }
//
//    public void sendToServerError(Post sendedPost, String teamId, String channelId) {
//        if (FileToAttachRepository.getInstance().haveUnloadedFiles()) {
//            return;
//        }
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//
//        String sendedPostId = sendedPost.getPendingPostId();
//        sendedPost.setUpdateAt(null);
//        postRepository.update(sendedPost);
//        getView().refreshItem();
//        sendedPost.setId(null);
//        sendedPost.setUser(null);
//        sendedPost.setMessage(Html.fromHtml(sendedPost.getMessage()).toString().trim());
//        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.sendPost(teamId, channelId, sendedPost)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//                        updateLastViewedAt(teamId, channelId);
//                        getView().onItemAdded();
//                        Log.d(TAG, "Complete create post");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        setErrorPost(sendedPostId);
//                        e.printStackTrace();
//                        Log.d(TAG, "Error repetition create post " + e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//                        setpostData(post, sendedPostId);
//                    }
//                });
//    }
//
//    public void sendToServer(Post sendedPost, String teamId, String channelId) {
//        if (isPosting) return;
//        if (FileToAttachRepository.getInstance().haveUnloadedFiles()) {
//            return;
//        }
//        isPosting = true;
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        String sendedPostId = sendedPost.getPendingPostId();
//        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.sendPost(teamId, channelId, sendedPost)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//                        updateLastViewedAt(teamId, channelId);
//                        getView().onItemAdded();
//                        Log.d(TAG, "Complete create post");
//                        FileToAttachRepository.getInstance().clearData();
//                        getView().hideAttachedFilesLayout();
//                        getView().setMessage("");
//                        isPosting = false;
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        setErrorPost(sendedPostId);
//                        e.printStackTrace();
//                        Log.d(TAG, "Error create post " + e.getMessage());
//                        isPosting = false;
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//                        setpostData(post, sendedPostId);
//                    }
//                });
//        Post forSavePost = new Post(sendedPost);
//        forSavePost.setId(sendedPostId);
//        forSavePost.setUser(userRepository.query(new UserByIdSpecification(forSavePost.getUserId()))
//                .first());
//        forSavePost.setMessage(Processor.process(forSavePost.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//        postRepository.add(forSavePost);
//    }
//
//
//    private void setErrorPost(String sendedPostId) {
//        Post post = new Post(postRepository.query(new PostByIdSpecification(sendedPostId)).first());
//        post.setUpdateAt(Post.NO_UPDATE);
//        Log.d("CreateAt", "setErrorPost: " + post.getCreateAt());
//        postRepository.update(post);
//        getView().refreshItem();
//    }
//
//    private void setpostData(Post post, String sendedPostId) {
//        post.setUser(userRepository.query(new UserByIdSpecification(post.getUserId())).first());
//        post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//        Log.d("CreateAt", "setpostData: " + post.getCreateAt());
//        postRepository.remove(new PostByPendingPostIdSpecification(sendedPostId));
//        postRepository.add(post);
//    }
//
//    private void updateLastViewedAt(String teamId, String channelId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        ApiMethod service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.updatelastViewedAt(teamId, channelId)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(Schedulers.io())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "Complete update last viewed at");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        Log.d(TAG, "Error");
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//                    }
//                });
//    }
//
//    public void initLoadNext() {
//        isLoadNext = true;
//    }
//
//    public void uploadFileToServer(Context context, String teamId, String channel_id, Uri uri) {
//        String filePath = FileUtils.getPath(context, uri);
//        String mimeType = FileUtils.getMimeType(filePath);
//        File file = new File(filePath);
//        if (file.exists()) {
//            ProgressRequestBody fileBody = new ProgressRequestBody(file, mimeType, new ProgressRequestBody.UploadCallbacks() {
//                @Override
//                public void onProgressUpdate(int percentage) {
//                    Log.d(TAG, String.format("Progress: %d", percentage));
//                }
//
//                @Override
//                public void onError() {
//
//                }
//
//                @Override
//                public void onFinish() {
//
//                }
//            });
//            MultipartBody.Part filePart = MultipartBody.Part.createFormData("files", file.getName(), fileBody);
//            RequestBody channelId = RequestBody.create(MediaType.parse("multipart/form-data"), channel_id);
//            RequestBody clientId = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());
//
//            if (mSubscription != null && !mSubscription.isUnsubscribed())
//                mSubscription.unsubscribe();
//            ApiMethod service = mMattermostApp.getMattermostRetrofitService();
//
//            mSubscription = service.uploadFile(teamId, filePart, channelId, clientId)
//                    .subscribeOn(Schedulers.newThread())
//                    .observeOn(Schedulers.io())
//                    .subscribe(new Subscriber<FileUploadResponse>() {
//                        @Override
//                        public void onCompleted() {
//                            Log.d(TAG, "Complete update last viewed at");
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            e.printStackTrace();
//                            Log.d(TAG, "Error");
//                        }
//
//                        @Override
//                        public void onNext(FileUploadResponse fileUploadResponse) {
//                            Log.d(TAG, fileUploadResponse.toString());
//                        }
//                    });
//        } else {
//            Log.e(TAG, "file not found");
//        }
//    }
//
//    public void deleteErrorSendPost(Post post) {
//        postRepository.remove(post);
//    }
//
//    public void deletePost(Post post, String teamId, String channelId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.deletePost(teamId, channelId, post.getId(), new Object())
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "Complete delete post");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        Log.d(TAG, "Error delete post " + e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//                        postRepository.remove(post);
//                    }
//                });
//    }
//
//    public void editPost(PostEdit post, String teamId, String channelId) {
//        if (mSubscription != null && !mSubscription.isUnsubscribed())
//            mSubscription.unsubscribe();
//        ApiMethod service;
//        service = mMattermostApp.getMattermostRetrofitService();
//        mSubscription = service.editPost(teamId, channelId, post)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Post>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "Complete edit post");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        Log.d(TAG, "Error edit post " + e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(Post post) {
//                        post.setUser(userRepository.query(new UserByIdSpecification(post.getUserId())).first());
//                        post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
//                        postRepository.update(post);
//                        getView().invalidateAdapter();
//                    }
//                });
//    }
//}
