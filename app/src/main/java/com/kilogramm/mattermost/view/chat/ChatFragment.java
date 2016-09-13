package com.kilogramm.mattermost.view.chat;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentChatBinding;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.view.fragments.BaseFragment;
import com.kilogramm.mattermost.viewmodel.chat.ChatFragmentViewModel;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class ChatFragment extends BaseFragment implements ChatFragmentViewModel.OnItemAddedListener {

    private static final String TAG = "ChatFragment";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "channel_name";

    private FragmentChatBinding binding;
    private String channelId;
    private String channelName;
    private static ChatFragmentViewModel viewModel;
    private Realm realm;
    private NewChatListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.channelId = getArguments().getString(CHANNEL_ID);
        this.channelName = getArguments().getString(CHANNEL_NAME);
        this.realm = Realm.getDefaultInstance();
        setupToolbar("",channelName,v -> {
            Toast.makeText(getContext(), "In development", Toast.LENGTH_SHORT).show();
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat,
                container, false);
        View view = binding.getRoot();
        viewModel = new ChatFragmentViewModel(getContext(),
                channelId,
                binding.swipeRefreshLayout,
                binding.writingMessage,
                this,
                binding.rev);
        binding.setViewModel(viewModel);
        setupListChat(channelId);
        return view;
    }


    public static ChatFragment createFragment(String channelId, String channelName){
        ChatFragment chatFragment = new ChatFragment();
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
            if(adapter!=null){
                if(results.size()-2 == binding.rev.findLastCompletelyVisibleItemPosition()){
                    onItemAdded();
                }
            }
        });
        adapter = new NewChatListAdapter(getContext(), results, true,binding.rev);
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
        viewModel.destroy();
        channelId = null;
    }


    @Override
    public void onItemAdded() {
        binding.rev.smoothScrollToPosition(binding.rev.getRecycleView().getAdapter().getItemCount()-1);
    }

    public static void showTyping(){
        viewModel.showTyping();
    }

    public static String getChannelId(){
        return viewModel.getChannelId();
    }
}