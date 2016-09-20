package com.kilogramm.mattermost.view.chat;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
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
import com.kilogramm.mattermost.adapters.UsersDropDownListAdapter;
import com.kilogramm.mattermost.databinding.FragmentChatMvpBinding;
import com.kilogramm.mattermost.model.entity.Team;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostByChannelId;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.presenter.ChatPresenter;
import com.kilogramm.mattermost.view.fragments.BaseFragment;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 13.09.2016.
 */
@RequiresPresenter(ChatPresenter.class)
public class ChatFragmentMVP extends BaseFragment<ChatPresenter> implements OnItemAddedListener {

    private static final String TAG = "ChatFragmentMVP";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "channel_name";

    private static final Integer TYPING_DURATION = 5000;

    private FragmentChatMvpBinding binding;

    private String channelId;

    private String teamId;

    private String channelName;

    private Realm realm;

    private NewChatListAdapter adapter;
    private UsersDropDownListAdapter dropDownListAdapter;

    private PostRepository  postRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.channelId = getArguments().getString(CHANNEL_ID);
        this.channelName = getArguments().getString(CHANNEL_NAME);
        this.realm = Realm.getDefaultInstance();
        this.teamId = realm.where(Team.class).findFirst().getId();
        this.postRepository = new PostRepository();
        setupToolbar("",channelName,v -> Toast.makeText(getActivity().getApplicationContext(), "In development", Toast.LENGTH_SHORT).show());
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
        setDropDownUserList();
        getPresenter().getExtraInfo(teamId,
                channelId);
    }

    private void setDropDownUserList() {
        dropDownListAdapter = new UsersDropDownListAdapter(name -> addUserLinkMessage(name));
        binding.idRecUser.setAdapter(dropDownListAdapter);
        binding.idRecUser.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.writingMessage.addTextChangedListener(getPresenter().getMassageTextWatcher());
    }

    public void setDropDown(RealmResults<User> realmResult){
        dropDownListAdapter.setUsers(realmResult);
    }

    public static ChatFragmentMVP createFragment(String channelId, String channelName){
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
            if(adapter!=null){
                if(results.size()-2 == binding.rev.findLastCompletelyVisibleItemPosition()){
                    onItemAdded();
                }
            }
        });
        adapter = new NewChatListAdapter(getActivity(), results, true);
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



    public String getChId(){
        return this.channelId;
    }

    public void setBtnSendOnClickListener(){
        binding.btnSend.setOnClickListener(view -> sendMessage());
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
        if(post.getMessage().length() != 0){
            getPresenter().sendToServer(post, teamId,channelId);
            //WebSocketService.with(context).sendTyping(channelId, teamId.getId());
        } else {
            Toast.makeText(getView().getContext(), "Message is empty", Toast.LENGTH_SHORT).show();
        }
    } // +

    private void setupRefreshListener() {
        binding.rev.getRecycleView().addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int bottomRow =
                        (recyclerView == null || recyclerView.getChildCount() == 0)
                                ? 0
                                : recyclerView.getAdapter().getItemCount()-1;
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

    public void showTyping(){
        binding.typing.setVisibility(View.VISIBLE);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                binding.typing.setVisibility(View.GONE);
            }
        },TYPING_DURATION);
    }

    public String getMessage() {
        return binding.writingMessage.getText().toString();
    }

    public void setMessage(String s){
        binding.writingMessage.setText(s);
    }


    @Override
    public void onItemAdded() {
        binding.rev.smoothScrollToPosition(binding.rev.getRecycleView().getAdapter().getItemCount()-1);
    }

    public void addUserLinkMessage(String s){
        binding.writingMessage.append(s + " ");
    }
}
