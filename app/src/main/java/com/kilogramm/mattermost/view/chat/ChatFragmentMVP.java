package com.kilogramm.mattermost.view.chat;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentChatMvpBinding;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.presenter.ChatPresenter;
import com.kilogramm.mattermost.view.fragments.BaseFragment;
import com.kilogramm.mattermost.viewmodel.chat.ChatFragmentViewModel;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 13.09.2016.
 */
@RequiresPresenter(ChatPresenter.class)
public class ChatFragmentMVP extends BaseFragment<ChatPresenter> implements ChatFragmentViewModel.OnItemAddedListener {
    private static final String TAG = "ChatFragment";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "channel_name";

    private static final Integer TYPING_DURATION = 5000;

    private static final int PICK_IMAGE = 1;
    private static final int CAMERA_PIC_REQUEST = 2;
    private static final int FILE_CODE = 3;

    private FragmentChatMvpBinding binding;
    private NewChatListAdapter adapter;
    private Realm realm;
    private String channelId;
    private String teamId;
    private String channelName;

    private boolean isMessageTextOpen = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.channelId = getArguments().getString(CHANNEL_ID);
        this.channelName = getArguments().getString(CHANNEL_NAME);
        this.realm = Realm.getDefaultInstance();
        this.teamId = realm.where(Team.class).findFirst().getId();
        setupToolbar("", channelName, v -> {
            Toast.makeText(getActivity().getApplicationContext(), "In development", Toast.LENGTH_SHORT).show();
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_mvp, container, false);
        View view = binding.getRoot();
        initView();

        return view;
    }

    private void initView() {
        setupListChat(channelId);
        setupRefreshListener();
        setBtnSendOnClickListener();
        setBottomToolbarOnClickListeners();
        getPresenter().getExtraInfo(teamId, channelId);
    }

    private void setBottomToolbarOnClickListeners() {
        binding.bottomToolbar.writeText.setOnClickListener(view -> {
            OnClickAddText();
        });

        binding.bottomToolbar.makePhoto.setOnClickListener(view -> {
            OnClickMakePhoto();
        });

        binding.bottomToolbar.addExistedPhoto.setOnClickListener(view -> {
            OnClickOpenGallery();
        });

        binding.bottomToolbar.addDocs.setOnClickListener(view -> {
            OnClickChooseDoc();
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
        RealmResults<Post> results = realm.where(Post.class)
                .equalTo("channelId", channelId)
                .findAllSorted("createAt", Sort.ASCENDING);
        results.addChangeListener(element -> {
            if (adapter != null) {
                if (results.size() - 2 == binding.rev.findLastCompletelyVisibleItemPosition()) {
                    onItemAdded();
                }
            }
        });
        adapter = new NewChatListAdapter(getActivity(), results, true, binding.rev);
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
    }

    @Override
    public void onItemAdded() {
        binding.rev.smoothScrollToPosition(binding.rev.getRecycleView().getAdapter().getItemCount() - 1);
    }

    public String getChId() {
        return this.channelId;
    }

    public void setBtnSendOnClickListener() {
        binding.btnSend.setOnClickListener(view -> {
            sendMessage();
        });
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
            //Toast.makeText(context, "Message is empty", Toast.LENGTH_SHORT).show();
        }
    } // +

    private void setupRefreshListener() {
        binding.rev.getRecycleView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int bottomRow =
                        (recyclerView == null || recyclerView.getChildCount() == 0)
                                ? 0
                                : recyclerView.getAdapter().getItemCount() - 1;
                binding.swipeRefreshLayout
                        .setEnabled(bottomRow == ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition());

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
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                binding.typing.setVisibility(View.GONE);
            }
        }, TYPING_DURATION);
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
}
