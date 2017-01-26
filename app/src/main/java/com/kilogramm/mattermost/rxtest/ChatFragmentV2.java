package com.kilogramm.mattermost.rxtest;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.AdapterPost;
import com.kilogramm.mattermost.adapters.AttachedFilesAdapter;
import com.kilogramm.mattermost.databinding.FragmentChatMvpBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostEdit;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.ui.AttachedFilesLayout;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.channel.AddMembersActivity;
import com.kilogramm.mattermost.view.channel.ChannelActivity;
import com.kilogramm.mattermost.view.chat.OnItemAddedListener;
import com.kilogramm.mattermost.view.chat.OnItemClickListener;
import com.kilogramm.mattermost.view.fragments.BaseFragment;
import com.kilogramm.mattermost.view.search.SearchMessageActivity;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 21.01.2017.
 */
@RequiresPresenter(ChatPresenterV2.class)
public class ChatFragmentV2 extends BaseFragment<ChatPresenterV2> implements OnMoreLoadListener,
        OnItemClickListener<String>, AttachedFilesAdapter.EmptyListListener,
        AttachedFilesLayout.AllUploadedListener, OnItemAddedListener {

    private static final String TAG = "ChatFragmentV2";

    private static final Integer TYPING_DURATION = 5000;

    public static final String START_NORMAL = "start_normal";
    public static final String START_SEARCH = "start_search";
    private static final String START_CODE = "start_code";

    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "channel_name";
    private static final String CHANNEL_TYPE = "channel_type";
    private static final String SEARCH_MESSAGE_ID = "search_message_id";


    private static final int SEARCH_CODE = 0;
    private static final int PICK_IMAGE = 1;
    private static final int CAMERA_PIC_REQUEST = 2;
    private static final int FILE_CODE = 3;
    private static final int PICKFILE_REQUEST_CODE = 5;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 6;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 7;
    private static final int PICK_FROM_GALLERY = 8;


    private FragmentChatMvpBinding mBinding;

    public static boolean active = false;

    private Realm mRealm;

    @State
    String mChannelId;
    @State
    String mChannelName;
    @State
    String mChannelType;
    @State
    String mStartCode;
    @State
    String mSearchMessageId = null;
    @State
    String mTeamId;
    @State
    boolean isFocus = false;
    @State
    int positionItemMessage;

    @State
    StateFragment mState = StateFragment.STATE_DEFAULT;

    private Uri fileFromCamera;

    private BroadcastReceiver brReceiverTyping;

    private AdapterPost adapter;

    Map<String, String> mapType;

    private Post rootPost;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mChannelId = getArguments().getString(CHANNEL_ID);
        this.mChannelName = getArguments().getString(CHANNEL_NAME);
        this.mChannelType = getArguments().getString(CHANNEL_TYPE);
        this.mStartCode  = getArguments().getString(START_CODE);
        this.mTeamId = MattermostPreference.getInstance().getTeamId();
        if (mStartCode.equals(START_SEARCH)){
            this.mSearchMessageId = getArguments().getString(SEARCH_MESSAGE_ID);
        }
        getPresenter().initPresenter(this.mTeamId, this.mChannelId, this.mChannelType);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_mvp, container, false);
        View view = mBinding.getRoot();
        initView();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupToolbar("", mChannelName, v -> {
            if(ChatUtils.isDirectChannel(mChannelType)){
                String userId = ChatUtils.getDirectUserId(mChannelId);
                if(userId!=null){
                    ProfileRxActivity.start(getActivity(), userId);
                } else {
                    Toast.makeText(getActivity(), "Error load user_id", Toast.LENGTH_SHORT).show();
                }
            } else {
                ChannelActivity.start(getActivity(), mChannelId);
            }
        }, v -> searchMessage());
        checkNeededPermissions();
        NotificationManager notificationManager = (NotificationManager)
                getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mChannelId.hashCode());
        Log.d(TAG, "onResume: channeld" + mChannelId.hashCode());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(brReceiverTyping!=null) {
            getActivity().unregisterReceiver(brReceiverTyping);
        }
    }

    @Override
    public void onTopLoadMore() {
        Log.d(TAG, "onTopLoadMore()");
        getPresenter().requestLoadBefore();
    }

    @Override
    public void onBotLoadMore() {
        Log.d(TAG, "onBotLoadMore()");
        getPresenter().requestLoadAfter();
    }

    @Override
    public void OnItemClick(View view, String item) {

    }

    @Override
    public void onItemAdded() {
        mBinding.rev.smoothScrollToPosition(mBinding.rev.getAdapter().getItemCount() - 1);
        //disableLoaders();
    }

    @Override
    public void onEmptyList() {
        hideAttachedFilesLayout();
        if (mBinding.writingMessage.getText().toString().trim().length() > 0) {
            mBinding.btnSend.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            mBinding.btnSend.setTextColor(getResources().getColor(R.color.grey));
        }
    }

    @Override
    public void onAllUploaded() {
        mBinding.btnSend.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    protected void setupTypingText(String text) {
        super.setupTypingText(text);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<Uri> pickedFiles = new ArrayList<>();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_PIC_REQUEST) {
                if (fileFromCamera != null) {
                    pickedFiles.add(fileFromCamera);
                } else {
                    Toast.makeText(getActivity(),
                            getString(R.string.cannot_attach_photo),
                            Toast.LENGTH_SHORT)
                            .show();
                }
            } else if (requestCode == FILE_CODE) {
                if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clip = data.getClipData();
                        if (clip != null) {
                            for (int i = 0; i < clip.getItemCount(); i++) {
                                pickedFiles.add(clip.getItemAt(i).getUri());
                            }
                        }
                    } else {
                        ArrayList<String> paths = data.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);
                        if (paths != null) {
                            for (String path : paths) {
                                pickedFiles.add(Uri.parse(path));
                            }
                        }
                    }
                }
            } else if ((requestCode == PICKFILE_REQUEST_CODE || requestCode == PICK_IMAGE)) {
                if (data != null) {
                    if (data.getData() != null) {
                        Uri uri = data.getData();
                        List<Uri> uriList = new ArrayList<>();
                        uriList.add(uri);
                        attachFiles(uriList);
                    } else if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            List<Uri> uriList = new ArrayList<>();
                            uriList.add(clipData.getItemAt(i).getUri());
                            attachFiles(uriList);
                        }
                    }
                }
            }
        }
        if (pickedFiles.size() > 0) {
            attachFiles(pickedFiles);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PICK_FROM_GALLERY:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //openGallery();
                }
                break;
            case CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //dispatchTakePictureIntent();
                }
                break;
        }
    }


    private void initView() {
        initListChat();
        initBtnSendOnClickListener();
        initRefreshListener();
        initButtonAddFileOnClickListener();
        initDropDownUserList();
        initCommandList();
        initAttachedFilesLayout();
        initBroadcastReceiver();
        initFab();
        initWritingFieldFocusListener();

        //TODO kepar wtf? :):):)
        /*if (adapter.getItemCount() > 0) {
            binding.rev.smoothScrollToPosition(adapter.getItemCount() - 1);
        }*/

        getPresenter().startLoadInfoChannel();

    }

    private void initAttachedFilesLayout() {
        RealmResults<FileToAttach> fileToAttachRealmResults = FileToAttachRepository.getInstance().getFilesForAttach();
        if (fileToAttachRealmResults != null && fileToAttachRealmResults.size() > 0) {
            mBinding.attachedFilesLayout.setVisibility(View.VISIBLE);
        } else {
            mBinding.attachedFilesLayout.setVisibility(View.GONE);
        }
        mBinding.attachedFilesLayout.setEmptyListListener(this);
        mBinding.attachedFilesLayout.setmAllUploadedListener(this);
    }

    private void initListChat() {
        RealmResults<Post> results = PostRepository.query(new PostByChannelId(mChannelId));
        results.addChangeListener(element -> {
            Log.d(TAG, "initListChat() change listener called");
            if (adapter != null) {
                if (results.size() - 2 == ((LinearLayoutManager) mBinding.rev.getLayoutManager()).findLastCompletelyVisibleItemPosition()) {
                    onItemAdded();
                }
            }
        });
        adapter = new AdapterPost(getActivity(), results, this);
        mBinding.rev.setAdapter(adapter);
        mBinding.rev.setListener(this);
    }


    public void initBtnSendOnClickListener() {
        mBinding.btnSend.setOnClickListener(view -> {
            if (!mBinding.btnSend.getText().equals("Save"))
                if (mBinding.writingMessage.getText().toString().startsWith("/")) {
                    sendCommand();
                } else {
                    sendMessage();
                }
            else
                editMessage();
        });
    }



    public void setChannelName(String channelName) {
        this.mChannelName = channelName;
    }

    private void initRefreshListener() {
        mBinding.rev.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int bottomRow = (recyclerView == null || recyclerView.getChildCount() == 0)
                        ? 0
                        : recyclerView.getAdapter().getItemCount() - 1;
                int lastVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findLastVisibleItemPosition();
                if (bottomRow == lastVisiblePosition) {
                    mBinding.swipeRefreshLayout
                            .setEnabled(true);
                    mBinding.fab.hide();
                } else {
                    mBinding.fab.show();
                    mBinding.swipeRefreshLayout
                            .setEnabled(false);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 1)
                    BaseActivity.hideKeyboard(getActivity());
            }
        });

        mBinding.swipeRefreshLayout.setOnRefreshListener(direction -> {
            getPresenter().startRequestLoadNormal();
        });
    }

    public void initButtonAddFileOnClickListener() {
        mBinding.buttonAttachFile.setOnClickListener(view -> showDialog());
    }

    private void initDropDownUserList() {
      /*  dropDownListAdapter = new UsersDropDownListAdapterV2(null, getActivity(), this::addUserLinkMessage);
        mBinding.idRecUser.setAdapter(dropDownListAdapter);
        mBinding.idRecUser.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.writingMessage.addTextChangedListener(getMassageTextWatcher());
        setListenerToRootView();
        mBinding.writingMessage.setOnClickListener(view ->
                getUserList(((EditText) view).getText().toString()));*/
    }

    private void initCommandList() {
        /*commandAdapter = new CommandAdapter(getActivity(), getCommandList(null), command -> {
            Toast.makeText(getActivity(), command.getCommand(), Toast.LENGTH_SHORT).show();
            mBinding.writingMessage.setText(command.getCommand() + " ");
            mBinding.writingMessage.setSelection(mBinding.writingMessage.getText().length());
        });
        mBinding.commandLayout.setAdapter(commandAdapter);
        mBinding.commandLayout.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.writingMessage.addTextChangedListener(getCommandListener());*/
    }


    private void initBroadcastReceiver() {
        brReceiverTyping = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WebSocketObj obj = intent.getParcelableExtra(MattermostService.BROADCAST_MESSAGE);
                Log.d(TAG, obj.getEvent());
                if (obj.getBroadcast() != null
                        && obj.getBroadcast().getChannel_id().equals(mChannelId)) {
                    if (obj.getEvent().equals(WebSocketObj.EVENT_POSTED)
                            && obj.getUserId() != null
                            && !obj.getUserId().equals(MattermostPreference.getInstance().getMyUserId())) {
                        if (mapType != null)
                            mapType.remove(obj.getUserId());
                        getActivity().runOnUiThread(() -> showTyping(null));
                    } else if (obj.getEvent().equals(WebSocketObj.EVENT_TYPING)) {
                        getActivity().runOnUiThread(() -> showTyping(obj));
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(WebSocketObj.EVENT_TYPING);
        intentFilter.addAction(WebSocketObj.EVENT_POSTED);
        getActivity().registerReceiver(brReceiverTyping, intentFilter);
    }

    private void initFab() {
        mBinding.fab.hide();
        /**
         * TODO kepar
         * I don`t know next logic
         */
       /* mBinding.fab.setOnClickListener(v -> {
            if (searchMessageId == null) {
                mBinding.rev.scrollToPosition(adapter.getItemCount() - 1);
            } else {
                mBinding.swipeRefreshLayout.setRefreshing(true);
                requestLoadPosts();
            }
        });*/
    }

    private void initWritingFieldFocusListener() {
        mBinding.writingMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (v == mBinding.writingMessage && !hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
    }

    private void attachFiles(List<Uri> uriList) {
        Log.d(TAG, "try to attach file");
        mBinding.attachedFilesLayout.setVisibility(View.VISIBLE);
        mBinding.attachedFilesLayout.addItems(uriList, mChannelId);
        mBinding.btnSend.setTextColor(getResources().getColor(R.color.grey));
    }

    private void searchMessage() {
        SearchMessageActivity.startForResult(getActivity(), mTeamId, SEARCH_CODE);
    }

    public static ChatFragmentV2 createFragment(String startCode, String channelId,
                                                String channelName, String channelType,
                                                String searchId) {
        Log.d(TAG, "createFragment() called with: startCode = [" + startCode + "], channelId = [" + channelId + "], channelName = [" + channelName + "], channelType = [" + channelType + "], searchId = [" + searchId + "]");
        ChatFragmentV2 chatFragment = new ChatFragmentV2();
        Bundle bundle = new Bundle();
        bundle.putString(CHANNEL_ID, channelId);
        bundle.putString(CHANNEL_NAME, channelName);
        bundle.putString(CHANNEL_TYPE, channelType);
        bundle.putString(START_CODE, startCode);
        if (startCode.equals(START_SEARCH) && searchId!=null && !searchId.equals("")) {
            bundle.putString(SEARCH_MESSAGE_ID, searchId);
        } /*else {
            throw new IllegalArgumentException("createFragment() called with: " +
                    "\nstartCode = [" + startCode + "], " +
                    "\nchannelId = [" + channelId + "], " +
                    "\nchannelName = [" + channelName + "], " +
                    "\nchannelType = [" + channelType + "], " +
                    "\nsearchId = [" + searchId + "]");
        }*/
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    public void showTyping(WebSocketObj obj) {
        String typing = getStringTyping(obj);
        if (typing != null) {
            setupTypingText(typing);

            mBinding.getRoot().postDelayed(() -> {
                if (mapType != null && obj != null) mapType.remove(obj.getUserId());
                showTyping(null);
            }, TYPING_DURATION);
        } else {
            if (getPresenter() != null) getPresenter().showInfoDefault();
            sendUsersStatus(null);
        }
    }

    public String getStringTyping(WebSocketObj obj) {
        StringBuffer result = new StringBuffer();
        if (mChannelType != null) {
            if (mChannelType.equals("D")) {
                if (obj != null) {
                    return getString(R.string.typing);
                } else return null;
            } else {
                if (mapType == null) {
                    mapType = new HashMap<>();
                }
                if (obj != null) {
                    RealmResults<User> resultUser = UserRepository
                            .query(new UserRepository.UserByIdSpecification(obj.getUserId()));

                    if (resultUser != null && resultUser.size() == 0) {
                        MattermostService.Helper.
                                create(MattermostApp.getSingleton()).
                                startLoadUser(obj.getUserId());
                    } else {
                        mapType.put(obj.getUserId(),
                                resultUser.first()
                                        .getUsername());
                    }

                }
                int count = 0;
                if (mapType.size() == 1) {
                    for (Map.Entry<String, String> item : mapType.entrySet()) {
                        result.append(item.getValue() + " " + getString(R.string.typing));
                    }
                    return result.toString();
                }

                if (mapType.size() == 2) {
                    for (Map.Entry<String, String> item : mapType.entrySet()) {
                        count++;
                        if (count == 2)
                            result.append(item.getValue() + " " + getString(R.string.typing));
                        else
                            result.append(item.getValue() + " and ");
                    }
                    return result.toString();
                }
                if (mapType.size() > 2)
                    for (Map.Entry<String, String> item : mapType.entrySet()) {
                        count++;
                        if (count == 1)
                            result.append(item.getValue() + ", ");
                        else if (count == 2) {
                            result.append(String.format("%s and %d %s",
                                    item.getValue(),
                                    mapType.size() - 2,
                                    getString(R.string.typing)));
                            return result.toString();
                        }
                    }
            }
        }
        return null;
    }

    private void sendUsersStatus(WebSocketObj obj) {
        if (mChannelType != null) {
            if (!mChannelType.equals("D")) {
                if (obj != null)
                    mapType.remove(obj.getUserId());
            } else {
                //getPresenter().requestUserStatus();
            }
        } else setupTypingText("");
    }

    private void checkNeededPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_STORAGE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void sendCommand() {
        /*getPresenter().requestSendCommand(new CommandToNet(this.mChannelId,
                mBinding.writingMessage.getText().toString(),
                Boolean.FALSE.toString()));*/
    }

    private void sendMessage() {
        Post post = new Post();
        post.setChannelId(mChannelId);
        post.setCreateAt(getTimePost());
        post.setMessage(getMessage());
        if (rootPost != null) {
            post.setRootId(rootPost.getId());
            closeEditView();
        }
        post.setUserId(MattermostPreference.getInstance().getMyUserId());
        // post.setId(String.format("%s:%s", post.getUserId(), post.getCreateAt()));
        //post.setUser(userRepository.query(new UserByIdSpecification(post.getUserId())).first());
        // post.setId(String.format("%s:%s", post.getUserId(), post.getCreateAt()));
        post.setFilenames(mBinding.attachedFilesLayout.getAttachedFiles());
        post.setPendingPostId(String.format("%s:%s", post.getUserId(), post.getCreateAt()));
        String message = post.getMessage().trim();
        if (
                message.length() != 0 && FileToAttachRepository.getInstance().getFilesForAttach().isEmpty() ||
                        message.length() == 0 && !FileToAttachRepository.getInstance().getFilesForAttach().isEmpty() && !FileToAttachRepository.getInstance().haveUnloadedFiles() ||
                        message.length() != 0 && !FileToAttachRepository.getInstance().getFilesForAttach().isEmpty() && !FileToAttachRepository.getInstance().haveUnloadedFiles()
                ) {
            getPresenter().requestSendToServer(post);
            hideAttachedFilesLayout();
            //WebSocketService.with(context).sendTyping(channelId, teamId.getId());
        } else {
            if (!FileToAttachRepository.getInstance().getFilesForAttach().isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.wait_files), Toast.LENGTH_SHORT).show();
            } else if (message.length() <= 0) {
                Toast.makeText(getActivity(), getString(R.string.message_empty), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void editMessage() {
        PostEdit postEdit = new PostEdit();
        postEdit.setId(rootPost.getId());
        postEdit.setChannelId(mChannelId);
        postEdit.setMessage(getMessage());
        closeEditView();
        if (postEdit.getMessage().length() != 0) {
            setMessage("");
            getPresenter().requestEditPost(postEdit);
        } else {
            Toast.makeText(getActivity(), getString(R.string.message_empty), Toast.LENGTH_SHORT).show();
        }
    }

    public String getMessage() {
        return mBinding.writingMessage.getText().toString();
    }

    public void setMessage(String s) {
        mBinding.writingMessage.setText(s);
    }

    private Long getTimePost() {
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        if (adapter.getLastItem() == null) {
            return currentTime;
        }
        Long lastTime = ((Post) adapter.getLastItem()).getCreateAt();
        if (currentTime > lastTime)
            return currentTime;
        else
            return lastTime + 1;
    }


    private void closeEditView() {
        mBinding.editReplyMessageLayout.editableText.setText(null);
        mBinding.editReplyMessageLayout.getRoot().setVisibility(View.GONE);
        rootPost = null;
        mBinding.btnSend.setText(getString(R.string.send));
    }

    public void hideAttachedFilesLayout() {
        mBinding.btnSend.setTextColor(getResources().getColor(R.color.colorPrimary));
        mBinding.attachedFilesLayout.setVisibility(View.GONE);
    }

    public void startLoad() {
        if (mStartCode.equals(START_SEARCH)) {
            getPresenter().startRequestLoadSearch(mSearchMessageId);
        } else if (mStartCode.equals(START_NORMAL)){
            getPresenter().startRequestLoadNormal();
        } else {
            //showError();
        }
    }


    private void showDialog() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_buttom_sheet, null);

        final Dialog mBottomSheetDialog = new Dialog(getActivity(), R.style.MaterialDialogSheet);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
        mBottomSheetDialog.show();

        view.findViewById(R.id.layCamera).setOnClickListener(v -> {
            makePhoto();
            mBottomSheetDialog.cancel();
        });
        view.findViewById(R.id.layGallery).setOnClickListener(v -> {
            openGallery();
            mBottomSheetDialog.cancel();
        });
        view.findViewById(R.id.layFile).setOnClickListener(v -> {
            pickFile();
            mBottomSheetDialog.cancel();
        });
    }

    private void makePhoto() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                dispatchTakePictureIntent();
            }
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            final File root = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES + "/Mattermost");
            root.mkdir();
            final String fname = "img_" + System.currentTimeMillis() + ".jpg";
            final File sdImageMainDirectory = new File(root, fname);

            fileFromCamera = Uri.fromFile(sdImageMainDirectory);
            Log.d(TAG, fileFromCamera.toString());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileFromCamera);
            startActivityForResult(takePictureIntent, CAMERA_PIC_REQUEST);
        }
    }

    private void openGallery() {
        openFile(getActivity(), "image/*", PICK_IMAGE);
    }

    private void pickFile() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                openFilePicker();
            }
        } else {
            openFilePicker();
        }
    }

    private void openFilePicker() {
        openFile(getActivity(), "*/*", PICKFILE_REQUEST_CODE);
    }

    private void openFile(Context context, String minmeType, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", minmeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (context.getPackageManager().resolveActivity(sIntent, 0) != null) {
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
        } else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            startActivityForResult(chooserIntent, requestCode);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    public void setVisibleProgressBar(Boolean aBoolean) {
        mBinding.progressBar.setVisibility((aBoolean)?View.VISIBLE:View.GONE);
    }

    public void setRefreshing(boolean b) {
        mBinding.swipeRefreshLayout.setRefreshing(b);
        //enableAllPagination();
    }

    public void enableAllPagination() {
        mBinding.rev.setCanPagination(true);
        mBinding.rev.setCanPaginationTop(true);
        mBinding.rev.setCanPaginationBot(true);
        disableShowLoadMoreTop();
        disableShowLoadMoreBot();
    }
    public void enableTopPagination() {
        mBinding.rev.setCanPagination(true);
        mBinding.rev.setCanPaginationTop(true);
        mBinding.rev.setCanPaginationBot(false);
        disableShowLoadMoreTop();
        disableShowLoadMoreBot();
    }
    public void enableBotPagination() {
        mBinding.rev.setCanPagination(true);
        mBinding.rev.setCanPaginationTop(false);
        mBinding.rev.setCanPaginationBot(true);
        disableShowLoadMoreTop();
        disableShowLoadMoreBot();
    }
    public void disablePagination() {
        mBinding.rev.setCanPagination(false);
        mBinding.rev.setCanPaginationTop(false);
        mBinding.rev.setCanPaginationBot(false);
        disableShowLoadMoreTop();
        disableShowLoadMoreBot();
    }

    public void slideToMessageById() {
        if (adapter != null) {
            adapter.setmHighlightedPostId(mSearchMessageId);
            positionItemMessage = adapter.getPositionById(mSearchMessageId);
        }

        isFocus = false;
        try {
            mBinding.rev.scrollToPosition(positionItemMessage);
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "slideToMessageById() called");
        }
        mBinding.rev.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int pFirst = layoutManager.findFirstVisibleItemPosition();
                int pLast = layoutManager.findLastVisibleItemPosition();

                if (adapter != null && adapter.getmHighlightedPostId() != null && pFirst < positionItemMessage && positionItemMessage < pLast && !isFocus) {
                    isFocus = true;
                }

                if (isFocus && pFirst > positionItemMessage || positionItemMessage > pLast) {
                    if (adapter != null) adapter.setmHighlightedPostId(null);
                    adapter.notifyDataSetChanged();
                    isFocus = false;
                    mBinding.rev.removeOnScrollListener(this);
                }
            }
        });
    }

    public void showList() {
        mBinding.rev.setVisibility(View.VISIBLE);
        mBinding.newMessageLayout.setVisibility(View.VISIBLE);
    }

    public void setMessageLayout(int visible) {
//        binding.bottomToolbar.bottomToolbarLayout.setVisibility(visible);
        mBinding.sendingMessageContainer.setVisibility(visible);
        mBinding.line.setVisibility(visible);
    }

    public void enablePaginationTopAndBot() {
        mBinding.rev.setCanPaginationTop(true);
        mBinding.rev.setCanPaginationBot(true);
        mBinding.rev.setCanPagination(true);
    }

    public void setStateFragment(StateFragment stateFragment) {
        this.mState = stateFragment;
    }

    public void showErrorLayout() {
        Toast.makeText(getActivity(), "ErrorLayout", Toast.LENGTH_SHORT).show();
    }

    public void setCanPaginationTop(Boolean aBoolean) {
        mBinding.rev.setCanPaginationTop(aBoolean);
        disableShowLoadMoreTop();
    }

    public void setCanPaginationBot(Boolean aBoolean) {
        mBinding.rev.setCanPaginationBot(aBoolean);
        disableShowLoadMoreBot();
    }

    public void disableShowLoadMoreTop() {
        Log.d(TAG, "disableShowLoadMoreTop()");
        mBinding.rev.disableShowLoadMoreTop();
    }

    public void disableShowLoadMoreBot() {
        Log.d(TAG, "disableShowLoadMoreBot()");
        mBinding.rev.disableShowLoadMoreBot();
    }

    public void showEmptyList(String channelId) {
        mBinding.progressBar.setVisibility(View.GONE);
        try {
            Channel channel = ChannelRepository.query(
                    new ChannelRepository.ChannelByIdSpecification(channelId)).last();

            User user = Realm.getDefaultInstance().where(User.class)
                    .equalTo("id", ChatUtils.getDirectUserId(mChannelId)).findFirst();

            String createAtDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
                    .format(new Date(channel.getCreateAt()));

            mBinding.emptyListTitle.setVisibility(View.VISIBLE);
            mBinding.emptyListMessage.setVisibility(View.VISIBLE);
            if (channel.getType().equals(Channel.DIRECT)) {
                mBinding.emptyListTitle.setText(user.getUsername());
                mBinding.emptyListMessage.setText(String.format(
                        getResources().getString(R.string.empty_dialog_direct_message), user.getUsername()));
            } else {
                mBinding.emptyListTitle.setText(String.format(
                        getResources().getString(R.string.empty_dialog_title), channel.getDisplayName()));

                String emptyListMessage = String.format(
                        getResources().getString(R.string.empty_dialog_beginning_message),
                        channel.getDisplayName(), createAtDate);

                if (channel.getType().equals(Channel.OPEN)) {
                    mBinding.emptyListMessage.setText(new StringBuilder(emptyListMessage
                            + " " + getResources().getString(R.string.empty_dialog_group_message)));
                } else {
                    mBinding.emptyListMessage.setText(new StringBuilder(emptyListMessage
                            + " " + getResources().getString(R.string.empty_dialog_private_message)));
                }
                mBinding.emptyListInviteOthers.setText(getResources().getString(R.string.empty_dialog_invite));
                mBinding.emptyListInviteOthers.setOnClickListener(
                        v -> AddMembersActivity.start(getActivity(), channel.getId()));
                mBinding.emptyListInviteOthers.setVisibility(View.VISIBLE);
            }
            mBinding.emptyList.setVisibility(View.VISIBLE);
            mBinding.newMessageLayout.setVisibility(View.VISIBLE);
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "showEmptyList: ", e);
        }
    }

    public void invalidateAdapter() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }


    public enum StateFragment {
        STATE_NORMAL_LOADING,
        STATE_SEARCH_LOADING,
        STATE_NORMAL,
        STATE_DEFAULT
    }
}