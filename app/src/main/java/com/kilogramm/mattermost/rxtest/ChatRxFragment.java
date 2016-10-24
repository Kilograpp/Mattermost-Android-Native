package com.kilogramm.mattermost.rxtest;

import android.Manifest;
import android.app.Activity;
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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
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
import android.widget.PopupMenu;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.AdapterPost;
import com.kilogramm.mattermost.adapters.AttachedFilesAdapter;
import com.kilogramm.mattermost.adapters.UsersDropDownListAdapter;
import com.kilogramm.mattermost.databinding.EditDialogLayoutBinding;
import com.kilogramm.mattermost.databinding.FragmentChatMvpBinding;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostEdit;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.view.chat.OnItemAddedListener;
import com.kilogramm.mattermost.view.chat.OnItemClickListener;
import com.kilogramm.mattermost.view.fragments.BaseFragment;
import com.kilogramm.mattermost.view.search.SearchMessageActivity;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import icepick.State;
import io.realm.Realm;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 06.10.2016.
 */
@RequiresPresenter(ChatRxPresenter.class)
public class ChatRxFragment extends BaseFragment<ChatRxPresenter> implements OnItemAddedListener,
        OnItemClickListener<Post>, OnMoreLoadListener, AttachedFilesAdapter.EmptyListListener {

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

    private Uri fileFromCamera;

    private Post rootPost;

    private Realm realm;

    private AdapterPost adapter;
    private UsersDropDownListAdapter dropDownListAdapter;


    private BroadcastReceiver brReceiverTyping;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.channelId = getArguments().getString(CHANNEL_ID);
        this.channelName = getArguments().getString(CHANNEL_NAME);
        this.searchMessageId = getArguments().getString(CHANNEL_IS_SEARCH);
        this.realm = Realm.getDefaultInstance();
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


    private void initView() {
        setupListChat(channelId);
        setupRefreshListener();
        setBtnSendOnClickListener();
        setBottomToolbarOnClickListeners();
        setButtonAddFileOnClickListener();
        setDropDownUserList();
        setAttachedFilesLayout();
        brReceiverTyping = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WebSocketObj obj = intent.getParcelableExtra(MattermostService.BROADCAST_MESSAGE);
                Log.d(TAG, obj.getEvent());
                if (obj.getEvent().equals(WebSocketObj.EVENT_POST_EDITED)) {
                    getActivity().runOnUiThread(() -> invalidateAdapter());
                } else {
                    if (obj.getChannelId().equals(channelId)) {
                        getActivity().runOnUiThread(() -> showTyping());
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(WebSocketObj.EVENT_TYPING);
        getActivity().registerReceiver(brReceiverTyping, intentFilter);

        if(searchMessageId != null){
            getPresenter().requestLoadBeforeAndAfter(searchMessageId);
        } else {
            getPresenter().requestExtraInfo();
        }
        binding.editReplyMessageLayout.close.setOnClickListener(view -> closeEditView());
    }

    @Override
    public void onResume() {
        super.onResume();
        setupToolbar("", channelName, v -> Toast.makeText(getActivity().getApplicationContext(),
                "In development", Toast.LENGTH_SHORT).show(), v -> searchMessage());
        checkNeededPermissions();
    }

    private void checkNeededPermissions(){
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

    private void setAttachedFilesLayout(){
        RealmResults<FileToAttach> fileToAttachRealmResults = FileToAttachRepository.getInstance().getFilesForAttach();
        if(fileToAttachRealmResults != null && fileToAttachRealmResults.size() > 0){
            binding.attachedFilesLayout.setVisibility(View.VISIBLE);
        } else {
            binding.attachedFilesLayout.setVisibility(View.GONE);
        }
        binding.attachedFilesLayout.setEmptyListListener(this);
    }

    private void setDropDownUserList() {
        dropDownListAdapter = new UsersDropDownListAdapter(getActivity(), this::addUserLinkMessage);
        binding.idRecUser.setAdapter(dropDownListAdapter);
        binding.idRecUser.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.writingMessage.addTextChangedListener(getMassageTextWatcher());
        setListenerToRootView();
    }

    public void setDropDown(RealmResults<User> realmResult) {
        dropDownListAdapter.updateData(realmResult);
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
                }
            }
        });
        adapter = new AdapterPost(getActivity(), results, this);
        binding.rev.setAdapter(adapter);
        binding.rev.setListener(this);
        //setupPaginationListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        channelId = null;
        getActivity().unregisterReceiver(brReceiverTyping);
    }

    public String getChId() {
        return this.channelId;
    }

    public void setBtnSendOnClickListener() {
        binding.btnSend.setOnClickListener(view -> {
            if (!binding.btnSend.getText().equals("Save"))
                sendMessage();
            else
                editMessage();
        });
    }

    public void setListenerToRootView() {
        final View activityRootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            if (heightDiff > 100) {
                binding.idRecUser.setVisibility(View.VISIBLE);
                isOpenedKeyboard = true;
            } else if (isOpenedKeyboard == true) {
                binding.idRecUser.setVisibility(View.INVISIBLE);
                isOpenedKeyboard = false;
            }
        });
    }

    public TextWatcher getMassageTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.toString().contains("@"))
                    if (charSequence.charAt((count > 1 ? count : start) - before) == '@')
                        getPresenter().requestGetUsers(null);
                    else
                        getPresenter().requestGetUsers(charSequence.toString());
                else
                    setDropDown(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<Uri> pickedFiles = new ArrayList<>();

        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == CAMERA_PIC_REQUEST) {
                pickedFiles.add(fileFromCamera);
            }
            if (requestCode == FILE_CODE) {
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
            }
        }
        if (resultCode == Activity.RESULT_OK && (requestCode == PICKFILE_REQUEST_CODE || requestCode == PICK_IMAGE)) {
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
        if (pickedFiles.size() > 0) {
            attachFiles(pickedFiles);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
/*            case WRITE_STORAGE_PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFilePicker();
                }
                break;*/
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
        if (post.getMessage().length() != 0) {
            getPresenter().requestSendToServer(post);
            //WebSocketService.with(context).sendTyping(channelId, teamId.getId());
        } else {
            Toast.makeText(getActivity(), "Message is empty", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "Message is empty", Toast.LENGTH_SHORT).show();
        }
    } // +

    private void closeEditView() {
        binding.editReplyMessageLayout.editableText.setText(null);
        binding.editReplyMessageLayout.getRoot().setVisibility(View.GONE);
        rootPost = null;
        binding.btnSend.setText("Send");
    }

    private Long getTimePost() {
        Long currentTime = Calendar.getInstance().getTimeInMillis();

        if (adapter.getLastItem() == null) {
            return currentTime;
        }
        Long lastTime = ((Post) adapter.getLastItem()).getCreateAt();

        if ((currentTime / 10000 * 10000) < lastTime)
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

                if (bottomRow == ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findLastCompletelyVisibleItemPosition()) {
                    binding.swipeRefreshLayout
                            .setEnabled(true);
                } else {
                    binding.swipeRefreshLayout
                            .setEnabled(false);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        binding.swipeRefreshLayout.setOnRefreshListener(direction -> {
            //getPresenter().initLoadNext();
            Log.d("DISABLE", "disable loading");
            binding.rev.disableShowLoadMoreTop();
            binding.rev.disableShowLoadMoreBot();
            binding.rev.setCanPagination(false);
            getPresenter().requestLoadPosts();
        });
    }

    private void setBottomToolbarOnClickListeners() {
        binding.bottomToolbar.writeText.setOnClickListener(view -> OnClickAddText());
        binding.bottomToolbar.makePhoto.setOnClickListener(view -> makePhoto());
        binding.bottomToolbar.addExistedPhoto.setOnClickListener(view -> OnClickOpenGallery());
        binding.bottomToolbar.addDocs.setOnClickListener(view -> OnClickChooseDoc());
    }

    public void OnClickChooseDoc() {
        /*Intent i = new Intent(getActivity(), FilePickerActivity.class)
                .putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE)
                .putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, FILE_CODE);*/
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

    public void OnClickMakePhoto() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    public void showEmptyList() {
        Log.d(TAG, "showEmptyList()");
        binding.progressBar.setVisibility(View.GONE);
        binding.emptyList.setVisibility(View.VISIBLE);
        binding.newMessageLayout.setVisibility(View.VISIBLE);
    }

    public void showList() {
        Log.d(TAG, "showList()");
        binding.progressBar.setVisibility(View.GONE);
        binding.rev.setVisibility(View.VISIBLE);
        binding.newMessageLayout.setVisibility(View.VISIBLE);
    }

    public void setRefreshing(boolean b) {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.rev.setCanPagination(true);
        binding.rev.setCanPaginationTop(true);
        binding.rev.setCanPaginationBot(true);
    }

    public void showTyping() {
        binding.typing.setVisibility(View.VISIBLE);
        binding.typing.postDelayed(() -> binding.typing.setVisibility(View.GONE), TYPING_DURATION);
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
        binding.writingMessage.append(s + " ");
    }

    @Override
    public void OnItemClick(View view, Post item) {
        switch (view.getId()) {
            case R.id.sendStatusError:
                showErrorSendMenu(view, item);
                break;
            case R.id.controlMenu:
                showPopupMenu(view, item);
                break;
            case R.id.avatar:
                ProfileRxActivity.start(getActivity(),item.getUserId());
                break;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = FileUtil.getInstance().createTempImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                fileFromCamera = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileFromCamera);
                startActivityForResult(takePictureIntent, CAMERA_PIC_REQUEST);
            }
        }
    }

    private void attachFiles(List<Uri> uriList) {
        binding.attachedFilesLayout.setVisibility(View.VISIBLE);
        binding.attachedFilesLayout.addItems(uriList, teamId, channelId);
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
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                                getPresenter().requestDeletePost(post);
                            })
                            .show();
                    break;
                case R.id.permalink:
                    binding.edit.setText(getMessageLink(post.getId()));
                    new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                            .setTitle(getString(R.string.copy_permalink))
                            .setView(binding.getRoot())
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton(R.string.copy_link, (dialogInterface1, i1) -> {
                                copyLink(binding.edit.getText().toString());
                            })
                            .show();
                    break;
                case R.id.reply:
                    rootPost = post;
                    showEditView(Html.fromHtml(post.getMessage()).toString(), REPLY_MESSAGE);
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private void showEditView(String message, String type) {
        Animation fallingAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.edit_card_anim);
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
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/"
                + realm.where(Team.class).findFirst().getName()
                + "/pl/"
                + postId;
    }

    public void invalidateAdapter() {
        adapter.notifyDataSetChanged();
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
        binding.attachedFilesLayout.setVisibility(View.GONE);
    }

    @Override
    public void onEmptyList() {
        hideAttachedFilesLayout();
    }

    public void setButtonAddFileOnClickListener() {
        binding.buttonAttachFile.setOnClickListener(view -> pickFile());
    }
}
