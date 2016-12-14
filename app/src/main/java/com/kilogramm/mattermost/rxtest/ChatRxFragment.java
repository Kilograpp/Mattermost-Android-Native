package com.kilogramm.mattermost.rxtest;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.AdapterPost;
import com.kilogramm.mattermost.adapters.AttachedFilesAdapter;
import com.kilogramm.mattermost.adapters.UsersDropDownListAdapter;
import com.kilogramm.mattermost.adapters.command.CommandAdapter;
import com.kilogramm.mattermost.databinding.EditDialogLayoutBinding;
import com.kilogramm.mattermost.databinding.FragmentChatMvpBinding;
import com.kilogramm.mattermost.model.entity.CommandObject;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostByIdSpecification;
import com.kilogramm.mattermost.model.entity.post.PostEdit;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserByChannelIdSpecification;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.fromnet.CommandToNet;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.ui.AttachedFilesLayout;
import com.kilogramm.mattermost.ui.ScrollAwareFabBehavior;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.channel.AddMembersActivity;
import com.kilogramm.mattermost.view.channel.ChannelActivity;
import com.kilogramm.mattermost.view.chat.OnItemAddedListener;
import com.kilogramm.mattermost.view.chat.OnItemClickListener;
import com.kilogramm.mattermost.view.chat.PostViewHolder;
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
 * Created by Evgeny on 06.10.2016.
 */
@RequiresPresenter(ChatRxPresenter.class)
public class ChatRxFragment extends BaseFragment<ChatRxPresenter> implements OnItemAddedListener,
        OnItemClickListener<String>, OnMoreLoadListener, AttachedFilesAdapter.EmptyListListener,
        AttachedFilesLayout.AllUploadedListener {

    private static final String TAG = "ChatRxFragment";

    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "channel_name";
    private static final String CHANNEL_IS_SEARCH = "isSearch";

    private static final String REPLY_MESSAGE = "reply_message";
    private static final String EDIT_MESSAGE = "edit_message";

    private static final Integer TYPING_DURATION = 5000;
    private static final int PICKFILE_REQUEST_CODE = 5;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 6;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 7;

    private static final int PICK_IMAGE = 1;
    private static final int PICK_FROM_GALLERY = 8;
    private static final int CAMERA_PIC_REQUEST = 2;
    private static final int FILE_CODE = 3;
    public static final int SEARCH_CODE = 4;

    private FragmentChatMvpBinding binding;

    public static boolean active = false;

    @State
    String channelId;
    @State
    String teamId;
    @State
    String channelName;
    @State
    boolean isOpenedKeyboard = false;
    @State
    boolean isMessageTextOpen = false;
    @State
    String searchMessageId = null;
    @State
    int positionItemMessage;
    @State
    boolean isFocus = false;
    @State
    boolean isFirstLoad = true;

    @State
    int removeablePosition = -1;

    private Uri fileFromCamera;

    private Post rootPost;

    private AdapterPost adapter;
    private UsersDropDownListAdapter dropDownListAdapter;
    private CommandAdapter commandAdapter;

    private BroadcastReceiver brReceiverTyping;

    private ScrollAwareFabBehavior fabBehavior;

    Map<String, String> mapType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.channelId = getArguments().getString(CHANNEL_ID);
        this.channelName = getArguments().getString(CHANNEL_NAME);
        this.searchMessageId = getArguments().getString(CHANNEL_IS_SEARCH);
        this.teamId = MattermostPreference.getInstance().getTeamId();
        getPresenter().initPresenter(teamId, channelId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_mvp,
                container, false);
        View view = binding.getRoot();
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

    private void initView() {
        setupListChat(channelId);
        setupRefreshListener();
        setBtnSendOnClickListener();
//        binding.bottomToolbar.getRoot().setVisibility(View.GONE);
        setButtonAddFileOnClickListener();
        setDropDownUserList();
        setupCommandList();
        setAttachedFilesLayout();

        brReceiverTyping = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WebSocketObj obj = intent.getParcelableExtra(MattermostService.BROADCAST_MESSAGE);
                Log.d(TAG, obj.getEvent());
                if (obj.getChannelId().equals(channelId)) {
                    if (obj.getEvent().equals(WebSocketObj.EVENT_POSTED)
                            && !obj.getUserId().equals(MattermostPreference.getInstance().getMyUserId())) {
                        if (obj != null) {
                            if (mapType != null)
                                mapType.remove(obj.getUserId());
                            getActivity().runOnUiThread(() -> showTyping(null));
                        }
                    } else if (obj.getEvent().equals(WebSocketObj.EVENT_TYPING)) {
                        getActivity().runOnUiThread(() -> showTyping(obj));
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(WebSocketObj.EVENT_TYPING);
        intentFilter.addAction(WebSocketObj.EVENT_POSTED);
        getActivity().registerReceiver(brReceiverTyping, intentFilter);

        binding.fab.hide();
        binding.fab.setOnClickListener(v -> {
            if (searchMessageId == null) {
                binding.rev.scrollToPosition(adapter.getItemCount() - 1);
            } else {
                searchMessageId = null;
                setupListChat(channelId);
            }
        });

        fabBehavior = new ScrollAwareFabBehavior(getActivity(), null);

        if (searchMessageId != null) {
            getPresenter().requestLoadBeforeAndAfter(searchMessageId);
        } else {
            fabBehavior.lockBehavior();
            getPresenter().requestExtraInfo();
        }

        binding.writingMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (v == binding.writingMessage && !hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        if (adapter.getItemCount() > 0) {
            binding.rev.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupToolbar("", channelName, v -> {
            RealmResults<User> users = UserRepository.query(new UserByChannelIdSpecification(channelId));
            if (users != null) ProfileRxActivity.start(getActivity(), users.first().getId());
            else ChannelActivity.start(getActivity(), channelId);
        }, v -> searchMessage());
        checkNeededPermissions();
        NotificationManager notificationManager = (NotificationManager)
                getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(channelId.hashCode());
        Log.d(TAG, "onResume: channeld" + channelId.hashCode());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        channelId = null;
        getActivity().unregisterReceiver(brReceiverTyping);
    }

    @Override
    protected void setupTypingText(String text) {
        super.setupTypingText(text);
    }

    public void slideToMessageById() {
        if (adapter != null) {
            adapter.setmHighlitedPost(searchMessageId);
            positionItemMessage = adapter.getPositionById(searchMessageId);
        }

        isFocus = false;
        binding.rev.smoothScrollToPosition(positionItemMessage);

        binding.rev.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int pFirst = layoutManager.findFirstVisibleItemPosition();
                int pLast = layoutManager.findLastVisibleItemPosition();
                Log.d("testLinearLayoutManager", String.format("%d %d %d", pFirst, pLast, positionItemMessage));

                if (adapter != null && adapter.getmHighlitedPost() != null && pFirst < positionItemMessage && positionItemMessage < pLast && !isFocus) {
                    isFocus = true;
                    Log.d("testLinearLayoutManager", String.format("isFocus %d %d", pFirst, pLast));
                }

                if (isFocus && pFirst > positionItemMessage || positionItemMessage > pLast) {
                    Log.d("testLinearLayoutManager", String.format("reset %d %d", pFirst, pLast));
                    if (adapter != null) adapter.setmHighlitedPost(null);
                    adapter.notifyDataSetChanged();
                    isFocus = false;
                }
            }
        });
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
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

    private void setAttachedFilesLayout() {
        RealmResults<FileToAttach> fileToAttachRealmResults = FileToAttachRepository.getInstance().getFilesForAttach();
        if (fileToAttachRealmResults != null && fileToAttachRealmResults.size() > 0) {
            binding.attachedFilesLayout.setVisibility(View.VISIBLE);
        } else {
            binding.attachedFilesLayout.setVisibility(View.GONE);
        }
        binding.attachedFilesLayout.setEmptyListListener(this);
        binding.attachedFilesLayout.setmAllUploadedListener(this);
    }

    private void setupCommandList() {
        commandAdapter = new CommandAdapter(getActivity(), getCommandList(null), command -> {
            Toast.makeText(getActivity(), command.getCommand(), Toast.LENGTH_SHORT).show();
            binding.writingMessage.setText(command.getCommand() + " ");
            binding.writingMessage.setSelection(binding.writingMessage.getText().length());
        });
        binding.commandLayout.setAdapter(commandAdapter);
        binding.commandLayout.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.writingMessage.addTextChangedListener(getCommandListener());
    }

    private TextWatcher getCommandListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                commandAdapter.updateDate(getCommandList(s.toString()));
                if (commandAdapter.getItemCount() == 0)
                    binding.cardViewCommandCardView.setVisibility(View.INVISIBLE);
                else
                    binding.cardViewCommandCardView.setVisibility(View.VISIBLE);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    private List<CommandObject> getCommandList(String commandWrite) {
        if (commandWrite != null && !commandWrite.equals("")) {
            return Stream.of(CommandObject.getCommandList())
                    .filter(value -> value.getCommand().startsWith(commandWrite))
                    .sorted()
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    // TODO: 12.12.2016 dropdownmenu
    private void setDropDownUserList() {
        dropDownListAdapter = new UsersDropDownListAdapter(getActivity(), this::addUserLinkMessage);
        binding.idRecUser.setAdapter(dropDownListAdapter);
        binding.idRecUser.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.writingMessage.addTextChangedListener(getMassageTextWatcher());
        setListenerToRootView();
        binding.writingMessage.setOnClickListener(view ->
                getUserList(((EditText) view).getText().toString()));
    }

    public void setDropDown(RealmResults<User> realmResult) {
        if (binding.writingMessage.getText().length() > 0) {
            dropDownListAdapter.updateData(realmResult);
        } else {
            dropDownListAdapter.updateData(null);
        }
        if (dropDownListAdapter.getItemCount() == 0)
            binding.cardViewDropDown.setVisibility(View.INVISIBLE);
        else
            binding.cardViewDropDown.setVisibility(View.VISIBLE);

    }

    public static ChatRxFragment createFragment(String channelId, String channelName, String searchMessageId) {
        ChatRxFragment chatFragment = new ChatRxFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CHANNEL_ID, channelId);
        bundle.putString(CHANNEL_NAME, channelName);
        bundle.putString(CHANNEL_IS_SEARCH, searchMessageId);
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    private void setupListChat(String channelId) {
        RealmResults<Post> results = PostRepository.query(new PostByChannelId(channelId));
        results.addChangeListener(element -> {
            if (adapter != null) {
                if (results.size() - 2 == ((LinearLayoutManager) binding.rev.getLayoutManager()).findLastCompletelyVisibleItemPosition()) {
                    onItemAdded();
//                    binding.fab.hide();
                } else {
//                    if (results.size() - ((LinearLayoutManager) binding.rev.getLayoutManager()).findLastCompletelyVisibleItemPosition() > 4) {
//                        fabBehavior.animateFabUp(binding.fab);
//                    }
                }
            }
        });
        adapter = new AdapterPost(getActivity(), results, this);
        binding.rev.setAdapter(adapter);
        binding.rev.setListener(this);
    }

    public void setBtnSendOnClickListener() {
        binding.btnSend.setOnClickListener(view -> {
            fabBehavior.lockBehavior();
            if (!binding.btnSend.getText().equals("Save"))
                if (binding.writingMessage.getText().toString().startsWith("/")) {
                    sendCommand();
                } else {
                    sendMessage();
                }
            else
                editMessage();
        });
    }

    private void sendCommand() {
        getPresenter().requestSendCommand(new CommandToNet(this.channelId,
                binding.writingMessage.getText().toString(),
                Boolean.FALSE.toString()));
    }

    public void setListenerToRootView() {
        final View activityRootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            if (heightDiff > 100) {
                isOpenedKeyboard = true;
            } else if (isOpenedKeyboard) {
                isOpenedKeyboard = false;
            }
        });
    }

    boolean isSendTyping;

    public TextWatcher getMassageTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().trim().length() > 0 ||
                        (FileToAttachRepository.getInstance().haveFilesToAttach() &&
                                !FileToAttachRepository.getInstance().haveUnloadedFiles())) {
                    binding.btnSend.setTextColor(getResources().getColor(R.color.colorPrimary));
                    if (!isSendTyping) {
                        isSendTyping = true;
                        MattermostService.Helper.create(getActivity()).sendUserTuping(channelId);
                        binding.getRoot().postDelayed(() -> isSendTyping = false, TYPING_DURATION);
                    }
                } else {
                    binding.btnSend.setTextColor(getResources().getColor(R.color.grey));
                }
                getUserList(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private void getUserList(String text) {
        Log.d(TAG, "getUserList: true");
        int cursorPos = binding.writingMessage.getSelectionStart();
        if (cursorPos > 0 && text.contains("@")) {
            fabBehavior.lockBehavior();
            if (text.charAt(cursorPos - 1) == '@') {
                getPresenter().requestGetUsers(null, cursorPos);
            } else {
                getPresenter().requestGetUsers(
                        text, cursorPos);
            }
        } else {
            Log.d(TAG, "getUserList: false");
            setDropDown(null);
            fabBehavior.unlockBehavior();
        }
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
                    openGallery();
                }
                break;
            case CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                }
                break;
        }
    }

//==========================MVP methods==================================================

    private void sendMessage() {
        Post post = new Post();
        post.setChannelId(channelId);
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
        post.setFilenames(binding.attachedFilesLayout.getAttachedFiles());
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
    } // +

    private void editMessage() {
        PostEdit postEdit = new PostEdit();
        postEdit.setId(rootPost.getId());
        postEdit.setChannelId(channelId);
        postEdit.setMessage(getMessage());
        closeEditView();
        if (postEdit.getMessage().length() != 0) {
            setMessage("");
            getPresenter().requestEditPost(postEdit);
        } else {
            Toast.makeText(getActivity(), getString(R.string.message_empty), Toast.LENGTH_SHORT).show();
        }
    } // +

    private void closeEditView() {
        binding.editReplyMessageLayout.editableText.setText(null);
        binding.editReplyMessageLayout.getRoot().setVisibility(View.GONE);
        rootPost = null;
        binding.btnSend.setText(getString(R.string.send));
    }

    public void setMessageLayout(int visible) {
//        binding.bottomToolbar.bottomToolbarLayout.setVisibility(visible);
        binding.sendingMessageContainer.setVisibility(visible);
        binding.line.setVisibility(visible);
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

    private void setupRefreshListener() {
        binding.rev.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int bottomRow =
                        (recyclerView == null || recyclerView.getChildCount() == 0)
                                ? 0
                                : recyclerView.getAdapter().getItemCount() - 1;
                int lastVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findLastVisibleItemPosition();
                if (bottomRow == lastVisiblePosition) {
                    binding.swipeRefreshLayout
                            .setEnabled(true);
//                    fabBehavior.animateFabDown(binding.fab);
                    binding.fab.hide();
                } else {
                    binding.fab.show();
                    binding.swipeRefreshLayout
                            .setEnabled(false);
//                    if(!binding.fab.isShown()) fabBehavior.animateFabUp(binding.fab);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 1)
                    BaseActivity.hideKeyboard(getActivity());
            }
        });

        binding.swipeRefreshLayout.setOnRefreshListener(direction -> {
            //getPresenter().initLoadNext();
            Log.d("DISABLE", "disable loading");
            binding.rev.disableShowLoadMoreTop();
            binding.rev.disableShowLoadMoreBot();
            binding.rev.setCanPagination(false);
            fabBehavior.lockBehavior();
            getPresenter().requestLoadPosts();
//            binding.fab.hide();
        });
    }

/*    private void setBottomToolbarOnClickListeners() {
        binding.bottomToolbar.writeText.setOnClickListener(view -> OnClickAddText());
        binding.bottomToolbar.makePhoto.setOnClickListener(view -> makePhoto());
        binding.bottomToolbar.addExistedPhoto.setOnClickListener(view -> OnClickOpenGallery());
        binding.bottomToolbar.addDocs.setOnClickListener(view -> OnClickChooseDoc());
    }*/

    public void OnClickChooseDoc() {
        pickFile();
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

    public void OnClickAddText() {
        if (!isMessageTextOpen) {
            binding.sendingMessageContainer.setVisibility(View.VISIBLE);
            isMessageTextOpen = true;
        } else {
            binding.sendingMessageContainer.setVisibility(View.GONE);
            isMessageTextOpen = false;
        }
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

    public void OnClickOpenGallery() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        PICK_FROM_GALLERY);
            } else {

                openGallery();
            }
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        openFile(getActivity(), "image/*", PICK_IMAGE);
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

    public void showEmptyList(String channelId) {
        binding.progressBar.setVisibility(View.GONE);

        Channel channel = ChannelRepository.query(
                new ChannelRepository.ChannelByIdSpecification(channelId)).first();

        String createAtDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
                .format(new Date(channel.getCreateAt()));

        binding.emptyListTitle.setVisibility(View.VISIBLE);
        binding.emptyListMessage.setVisibility(View.VISIBLE);
        if (channel.getType().equals(Channel.DIRECT)) {
            binding.emptyListTitle.setText(channel.getUsername());
            binding.emptyListMessage.setText(String.format(
                    getResources().getString(R.string.empty_dialog_direct_message), channel.getUsername()));
        } else {
            binding.emptyListTitle.setText(String.format(
                    getResources().getString(R.string.empty_dialog_title), channel.getDisplayName()));

            String emptyListMessage = String.format(
                    getResources().getString(R.string.empty_dialog_beginning_message),
                    channel.getDisplayName(), createAtDate);

            if (channel.getType().equals(Channel.OPEN)) {
                binding.emptyListMessage.setText(new StringBuilder(emptyListMessage
                        + " " + getResources().getString(R.string.empty_dialog_group_message)));
            } else {
                binding.emptyListMessage.setText(new StringBuilder(emptyListMessage
                        + " " + getResources().getString(R.string.empty_dialog_private_message)));
            }
            binding.emptyListInviteOthers.setText(getResources().getString(R.string.empty_dialog_invite));
            binding.emptyListInviteOthers.setOnClickListener(
                    v -> AddMembersActivity.start(getActivity(), channel.getId()));
            binding.emptyListInviteOthers.setVisibility(View.VISIBLE);
        }
        binding.emptyList.setVisibility(View.VISIBLE);
        binding.newMessageLayout.setVisibility(View.VISIBLE);
    }

    public void showList() {
        Log.d(TAG, "showList()");
        binding.progressBar.setVisibility(View.GONE);
        binding.rev.setVisibility(View.VISIBLE);
        binding.emptyList.setVisibility(View.GONE);
        binding.newMessageLayout.setVisibility(View.VISIBLE);
        fabBehavior.unlockBehavior();
        if (isFirstLoad && adapter.getItemCount() > 0) {
            binding.rev.scrollToPosition(adapter.getItemCount() - 1);
            isFirstLoad = false;
        }
    }

    public void setRefreshing(boolean b) {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.rev.setCanPagination(true);
        binding.rev.setCanPaginationTop(true);
        binding.rev.setCanPaginationBot(true);
    }

    public void initTypingText() {
        if (!getPresenter().getChannelType().equals("D"))
            getPresenter().requestGetCountUsersStatus();
        else
            getPresenter().requestUserStatus();
    }

    public void showTyping(WebSocketObj obj) {
        String typing = getStringTyping(obj);
        if (typing != null) {
            setupTypingText(typing);
            binding.getRoot().postDelayed(() -> {
                sendUsersStatus(obj);
            }, TYPING_DURATION);
        } else
            sendUsersStatus(null);
    }

    private void sendUsersStatus(WebSocketObj obj) {
        if (getPresenter().getChannelType() != null) {
            if (!getPresenter().getChannelType().equals("D")) {
                if (obj != null)
                    mapType.remove(obj.getUserId());
                if (mapType.size() == 0)
                    getPresenter().requestGetCountUsersStatus();
            } else {
                getPresenter().requestUserStatus();
            }
        } else setupTypingText("");
    }

    public String getStringTyping(WebSocketObj obj) {
        StringBuffer result = new StringBuffer();
        if (getPresenter().getChannelType() != null) {
            if (getPresenter().getChannelType().equals("D")) {
                if (obj != null) {
                    return getString(R.string.typing);
                } else return null;
            } else {
                if (mapType == null) {
                    mapType = new HashMap<>();
                }
                if (obj != null) {
                    mapType.put(obj.getUserId(),
                            UserRepository
                                    .query(new UserRepository.UserByIdSpecification(obj.getUserId()))
                                    .first()
                                    .getUsername());
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

    public String getMessage() {
        return binding.writingMessage.getText().toString();
    }

    public void setMessage(String s) {
        binding.writingMessage.setText(s);
    }

    @Override
    public void onItemAdded() {
        binding.rev.smoothScrollToPosition(binding.rev.getAdapter().getItemCount() - 1);
    }

    public void addUserLinkMessage(String s) {
        StringBuffer nameBufferStart = new StringBuffer(binding.writingMessage.getText().toString());
        int cursorPos = binding.writingMessage.getSelectionStart();

        if (cursorPos != 0 && cursorPos == nameBufferStart.length()
                && nameBufferStart.charAt(cursorPos - 1) == '@') {
            binding.writingMessage.append(String.format("%s ", s));
            binding.writingMessage.setSelection(binding.writingMessage.getText().length());
            return;
        }
        if (cursorPos < nameBufferStart.length())
            nameBufferStart.delete(cursorPos, nameBufferStart.length());
        if (nameBufferStart.charAt(cursorPos - 1) == '@') {
            nameBufferStart.append(String.format("%s ", s));
        } else {
            String[] username = nameBufferStart.toString().split("@");
            nameBufferStart = new StringBuffer();
            int count = 1;
            if (username.length == 0) {
                nameBufferStart.append(String.format("@%s ", s));
            }
            for (String element : username) {
                if (count == username.length)
                    nameBufferStart.append(String.format("%s ", s));
                else
                    nameBufferStart.append(String.format("%s@", element));
                count++;
            }
        }
        StringBuffer nameBufferEnd = new StringBuffer(binding.writingMessage.getText());
        if (cursorPos < nameBufferStart.length())
            nameBufferEnd.delete(0, cursorPos);

        binding.writingMessage.setText(nameBufferStart.toString() + nameBufferEnd.toString());
        binding.writingMessage.setSelection(nameBufferStart.length());
    }

    @Override
    public void OnItemClick(View view, String item) {
        if (PostRepository.query(new PostByIdSpecification(item)).size() != 0) {
            Post post = new Post(PostRepository.query(new PostByIdSpecification(item)).first());
            removeablePosition = adapter.getPositionById(item);
            switch (view.getId()) {
                case R.id.sendStatusError:
                    showErrorSendMenu(view, post);
                    break;
                case R.id.controlMenu:
                    showPopupMenu(view, post);
                    break;
                case R.id.avatar:
                    ProfileRxActivity.start(getActivity(), post.getUserId());
                    break;
            }
        }
    }

    public void notifyItem() {
        if (removeablePosition != -1) adapter.notifyItemChanged(removeablePosition);
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

    private void attachFiles(List<Uri> uriList) {
        Log.d(TAG, "try to attach file");
        binding.attachedFilesLayout.setVisibility(View.VISIBLE);
        binding.attachedFilesLayout.addItems(uriList, channelId);
        binding.btnSend.setTextColor(getResources().getColor(R.color.grey));
    }

    private void showErrorSendMenu(View view, Post post) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view, Gravity.BOTTOM);
        popupMenu.inflate(R.menu.error_send_item_popupmenu);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.try_again:
                    Post p = new Post(post);
                    getPresenter().requestSendToServerError(p);
                    break;
                case R.id.delete:
                    PostRepository.remove(post);
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private void showPopupMenu(View view, Post post) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view, Gravity.BOTTOM);

        if (post.getUserId().equals(MattermostPreference.getInstance().getMyUserId())) {
            popupMenu.inflate(R.menu.my_chat_item_popupmenu);
        } else {
            popupMenu.inflate(R.menu.foreign_chat_item_popupmenu);
        }

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            EditDialogLayoutBinding binding = DataBindingUtil.inflate(getActivity().getLayoutInflater(),
                    R.layout.edit_dialog_layout, null, false);

            switch (menuItem.getItemId()) {
                case R.id.edit:
                    rootPost = post;
                    showEditView(Html.fromHtml(post.getMessage()).toString(), EDIT_MESSAGE);
                    break;
                case R.id.delete:
                    new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                            .setTitle(getString(R.string.confirm_post_delete))
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton(R.string.delete, (dialogInterface, i) -> getPresenter().requestDeletePost(post))
                            .show();
                    break;
                case R.id.permalink:
                    binding.edit.setText(getMessageLink(post.getId()));
                    new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                            .setTitle(getString(R.string.copy_permalink))
                            .setView(binding.getRoot())
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton(R.string.copy_link, (dialogInterface1, i1) -> copyLink(binding.edit.getText().toString()))
                            .show();
                    break;
                case R.id.copy:
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(PostViewHolder.getMarkdownPost(post.getMessage(), getActivity()));
                    Toast.makeText(getActivity(), "Сopied to the clipboard", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.reply:
                    rootPost = post;
                    showReplayView(Html.fromHtml(post.getMessage()).toString(), REPLY_MESSAGE);
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private void showEditView(String message, String type) {
        showView(message, type);
        binding.editReplyMessageLayout.close.setOnClickListener(view -> {
            binding.writingMessage.setText(null);
            closeEditView();
        });
        binding.writingMessage.setText(rootPost.getMessage());
        binding.writingMessage.setSelection(rootPost.getMessage().length());
    }

    private void showReplayView(String message, String type) {
        showView(message, type);
        binding.editReplyMessageLayout.close.setOnClickListener(view -> closeEditView());
    }

    private void showView(String message, String type) {
        Animation upAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.edit_card_up);

        if (type.equals(REPLY_MESSAGE))
            binding.editReplyMessageLayout.title.setText(getResources().getString(R.string.reply_message));
        else {
            binding.editReplyMessageLayout.title.setText(getResources().getString(R.string.edit_message));
            binding.btnSend.setText(R.string.save);
        }

        binding.editReplyMessageLayout.editableText.setText(message);
        binding.editReplyMessageLayout.root.startAnimation(upAnim);
        //binding.editMessageLayout.card.startAnimation(fallingAnimation);
        binding.editReplyMessageLayout.getRoot().setVisibility(View.VISIBLE);
    }

    private String getMessageLink(String postId) {
        Realm realm = Realm.getDefaultInstance();
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/"
                + realm.where(Team.class).findFirst().getName()
                + "/pl/"
                + postId;
    }

    public void invalidateByPosition(int position) {
        adapter.notifyItemChanged(position);
    }

    public void invalidateAdapter() {
        adapter.notifyDataSetChanged();
        fabBehavior.unlockBehavior();
    }

    public void copyLink(String link) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(link);
        Toast.makeText(getActivity(), "link copied", Toast.LENGTH_SHORT).show();
    }

    private void searchMessage() {
        SearchMessageActivity.startForResult(getActivity(), teamId, SEARCH_CODE);
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

    public void disableShowLoadMoreTop() {
        Log.d(TAG, "disableShowLoadMoreTop()");
        binding.rev.disableShowLoadMoreTop();
    }

    public void disableShowLoadMoreBot() {
        Log.d(TAG, "disableShowLoadMoreBot()");
        binding.rev.disableShowLoadMoreBot();
    }

    public void setCanPaginationTop(Boolean aBoolean) {
        binding.rev.setCanPaginationTop(aBoolean);
        disableShowLoadMoreTop();
    }

    public void setCanPaginationBot(Boolean aBoolean) {
        binding.rev.setCanPaginationBot(aBoolean);
        disableShowLoadMoreBot();
    }

    public void hideAttachedFilesLayout() {
        binding.btnSend.setTextColor(getResources().getColor(R.color.colorPrimary));
        binding.attachedFilesLayout.setVisibility(View.GONE);
    }

    @Override
    public void onEmptyList() {
        hideAttachedFilesLayout();
        if (binding.writingMessage.getText().toString().trim().length() > 0) {
            binding.btnSend.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            binding.btnSend.setTextColor(getResources().getColor(R.color.grey));
        }
    }

    public void setButtonAddFileOnClickListener() {
        binding.buttonAttachFile.setOnClickListener(view -> showDialog());
    }

    @Override
    public void onAllUploaded() {
        binding.btnSend.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setupTypingText("");
    }


}
