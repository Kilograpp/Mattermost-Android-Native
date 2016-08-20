package com.kilogramm.mattermost.view.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentChatBinding;
import com.kilogramm.mattermost.viewmodel.chat.ChatFragmentViewModel;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class ChatFragment extends BaseFragment {

    private static final String TAG = "ChatFragment";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "channel_name";

    private FragmentChatBinding binding;
    private String channelId;
    private String channelName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.channelId = getArguments().getString(CHANNEL_ID);
        this.channelName = getArguments().getString(CHANNEL_NAME);
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
        binding.setViewModel(new ChatFragmentViewModel(getContext(),channelId));
       // setupListChat();
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


}
