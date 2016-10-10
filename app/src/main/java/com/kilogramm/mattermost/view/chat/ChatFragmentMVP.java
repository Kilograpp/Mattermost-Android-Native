package com.kilogramm.mattermost.view.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.UsersDropDownListAdapter;
import com.kilogramm.mattermost.databinding.EditDialogLayoutBinding;
import com.kilogramm.mattermost.databinding.FragmentChatMvpBinding;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;
import com.kilogramm.mattermost.presenter.ChatPresenter;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.view.fragments.BaseFragment;
import com.kilogramm.mattermost.view.menu.GeneralActivity;
import com.kilogramm.mattermost.view.search.SearchMessageActivity;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 13.09.2016.
 */
@RequiresPresenter(ChatPresenter.class)
public class ChatFragmentMVP extends BaseFragment<ChatPresenter> implements OnItemAddedListener, OnItemClickListener<Post>, NewChatListAdapter.GetRootPost {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = "ChatFragmentMVP";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "channel_name";
    private static final String TEAM_ID = "team_id";

    private static final Integer TYPING_DURATION = 5000;
    private static final int PICKFILE_REQUEST_CODE = 5;

    private static final int PICK_IMAGE = 1;
    private static final int CAMERA_PIC_REQUEST = 2;
    private static final int FILE_CODE = 3;
    private static final int SEARCH_CODE = 4;

    private FragmentChatMvpBinding binding;
    private NewChatListAdapter adapter;
    private Realm realm;
    private String channelId;
    private String teamId;
    private String channelName;

    private boolean isMessageTextOpen = false;

    private UsersDropDownListAdapter dropDownListAdapter;

    private PostRepository postRepository;

    private BroadcastReceiver brReceiverTyping;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.channelId = getArguments().getString(CHANNEL_ID);
        this.channelName = getArguments().getString(CHANNEL_NAME);
        this.realm = Realm.getDefaultInstance();
        this.teamId = realm.where(Team.class).findFirst().getId();
        this.postRepository = new PostRepository();
        setupToolbar("", channelName, v -> Toast.makeText(getActivity().getApplicationContext(), "In development", Toast.LENGTH_SHORT).show(),
                v -> searchMessage());
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
        brReceiverTyping = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WebSocketObj obj = intent.getParcelableExtra(MattermostService.BROADCAST_MESSAGE);
                Log.d(TAG, obj.getEvent());
                if (obj.getChannelId().equals(channelId)) {
                    getActivity().runOnUiThread(() -> showTyping());
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(WebSocketObj.EVENT_TYPING);
        getActivity().registerReceiver(brReceiverTyping, intentFilter);
        getPresenter().getExtraInfo(teamId,
                channelId);
    }

    private void searchMessage(){
        Intent intent = new Intent(getActivity(), SearchMessageActivity.class)
                .putExtra(TEAM_ID, teamId);
        getActivity().startActivityForResult(intent, SEARCH_CODE);
    }

    private void setBottomToolbarOnClickListeners() {
        binding.bottomToolbar.writeText.setOnClickListener(view -> OnClickAddText());
        binding.bottomToolbar.makePhoto.setOnClickListener(view -> OnClickMakePhoto());
        binding.bottomToolbar.addExistedPhoto.setOnClickListener(view -> OnClickOpenGallery());
        binding.bottomToolbar.addDocs.setOnClickListener(view -> OnClickChooseDoc());
    }

    private void setDropDownUserList() {
        dropDownListAdapter = new UsersDropDownListAdapter(binding.getRoot().getContext(), this::addUserLinkMessage);
        binding.idRecUser.setAdapter(dropDownListAdapter);
        binding.idRecUser.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.writingMessage.addTextChangedListener(getPresenter().getMassageTextWatcher());
        setListenerToRootView();
    }

    public void setDropDown(RealmResults<User> realmResult) {
        dropDownListAdapter.updateData(realmResult);
    }

    boolean isOpened = false;

    public void setListenerToRootView() {
        final View activityRootView = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            if (heightDiff > 100) {
                binding.idRecUser.setVisibility(View.VISIBLE);
                isOpened = true;
            } else if (isOpened == true) {
                binding.idRecUser.setVisibility(View.INVISIBLE);
                isOpened = false;
            }
        });
    }





    public static ChatFragmentMVP createFragment(String channelId, String channelName) {
        ChatFragmentMVP chatFragment = new ChatFragmentMVP();
        Bundle bundle = new Bundle();
        bundle.putString(CHANNEL_ID, channelId);
        bundle.putString(CHANNEL_NAME, channelName);
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    private void setupListChat(String channelId) {
        RealmResults<Post> results = postRepository.query(new PostByChannelId(channelId));
        results.addChangeListener(element -> {
            if (adapter != null) {
                if (results.size() - 2 == binding.rev.findLastCompletelyVisibleItemPosition()) {
                    onItemAdded();
                }
            }
        });
        adapter = new NewChatListAdapter(getActivity(), results, true, this, this);
        binding.rev.setAdapter(adapter);
        binding.rev.getRecycleView().addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.realm.close();
        Log.d(TAG, "onDestroy()");
        channelId = null;
        getActivity().unregisterReceiver(brReceiverTyping);
    }

    public String getChId() {
        return this.channelId;
    }

    public void setBtnSendOnClickListener() {
        binding.btnSend.setOnClickListener(view -> sendMessage());
    }

    public void setButtonAddFileOnClickListener() {
        binding.buttonAttachFile.setOnClickListener(view -> attachFile());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageFromGallery;
        ArrayList<Uri> pickedFiles = new ArrayList<>();
        Uri pickedImage;

        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == PICK_IMAGE) {
                imageFromGallery = data.getData();
            }
            if (requestCode == CAMERA_PIC_REQUEST) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
            }
            if (requestCode == FILE_CODE) {
                if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clip = data.getClipData();
                        if (clip != null) {
                            for (int i = 0; i < clip.getItemCount(); i++) {
//                                Uri uri = clip.getItemAt(i).getUri();
                                pickedFiles.add(clip.getItemAt(i).getUri());
                            }
                        }
                    } else {
                        ArrayList<String> paths = data.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);
                        if (paths != null) {
                            for (String path : paths) {
//                                Uri uri = Uri.parse(path);
                                pickedFiles.add(Uri.parse(path));
                            }
                        }
                    }
                } else {
                    pickedImage = data.getData();
                }
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == PICKFILE_REQUEST_CODE) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                binding.attachedFilesLayout.addItem(uri, teamId, channelId);
            }
        }
    }

    //==========================MVP methods==================================================

    private void sendMessage() {
        Post post = new Post();
        post.setChannelId(channelId);
        post.setCreateAt(Calendar.getInstance().getTimeInMillis());
        post.setMessage(getMessage());
        post.setUserId(MattermostPreference.getInstance().getMyUserId());
        post.setPendingPostId(String.format("%s:%s", post.getUserId(), post.getCreateAt()));

        setMessage("");
        if (post.getMessage().length() != 0) {
            getPresenter().sendToServer(post, teamId, channelId);
            //WebSocketService.with(context).sendTyping(channelId, teamId.getId());
        } else {
            Toast.makeText(getActivity(), "Message is empty", Toast.LENGTH_SHORT).show();
        }
    } // +

    private void attachFile() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("*/*");
//        startActivityForResult(intent, PICKFILE_REQUEST_CODE);

//        Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
//        intent.putExtra("CONTENT_TYPE", "*/*");
//        intent.addCategory(Intent.CATEGORY_DEFAULT);

        openFile(getActivity(), "*/*", PICKFILE_REQUEST_CODE);
    }

    private void openFile(Context context, String minmeType, int requestCode) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

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

    private void setupRefreshListener() {
        binding.rev.getRecycleView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int bottomRow =
                        (recyclerView == null || recyclerView.getChildCount() == 0)
                                ? 0
                                : recyclerView.getAdapter().getItemCount() - 1;
                binding.swipeRefreshLayout
                        .setEnabled(bottomRow == ((LinearLayoutManager) recyclerView.getLayoutManager())
                                .findLastCompletelyVisibleItemPosition());

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        binding.swipeRefreshLayout.setOnRefreshListener(direction -> {
            getPresenter().initLoadNext();
            getPresenter().loadPosts(teamId, channelId);
        });
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

    public void OnClickAddText() {
        if (!isMessageTextOpen) {
            binding.sendingMessageContainer.setVisibility(View.VISIBLE);
            isMessageTextOpen = true;
        } else {
            binding.sendingMessageContainer.setVisibility(View.GONE);
            isMessageTextOpen = false;
        }
    }

    public void OnClickMakePhoto() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    public void OnClickOpenGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);
    }

    public void OnClickChooseDoc() {
        Intent i = new Intent(getActivity(), FilePickerActivity.class)
                .putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE)
                .putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, FILE_CODE);
    }

    @Override
    public Post getRootPost(Post post) {
        return getPresenter().getRootPost(post);
    }


    @Override
    public void onItemAdded() {
        binding.rev.smoothScrollToPosition(binding.rev.getRecycleView().getAdapter().getItemCount() - 1);
    }

    public void addUserLinkMessage(String s) {
        binding.writingMessage.append(s + " ");
    }

    @Override
    public void OnItemClick(View view, Post item) {
        switch (view.getId()) {
            case R.id.controlMenu:
                showPopupMenu(view, item);
                break;
        }
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
                    showEditView(Html.fromHtml(post.getMessage()).toString());
                    break;
                case R.id.delete:
                    new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                            .setTitle(getString(R.string.confirm_post_delete))
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                                getPresenter().deletePost(post, teamId, channelId);
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
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    private void showEditView(String message) {
        Animation fallingAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.edit_card_anim);
        Animation upAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.edit_card_up);
        binding.editMessageLayout.editableText.setText(message);
        binding.editMessageLayout.root.startAnimation(upAnim);
        //binding.editMessageLayout.card.startAnimation(fallingAnimation);
        binding.editMessageLayout.getRoot().setVisibility(View.VISIBLE);
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

    public void loadBeforeAndAfter(String postId, String channelId){
        Realm realm = Realm.getDefaultInstance();
        String teamId = realm.where(Team.class).findFirst().getId();
        getPresenter().loadPostsAfter(teamId, channelId, postId, "0", "10");
        getPresenter().loadPostsBefore(teamId, channelId, postId, "0", "10");
    }
}