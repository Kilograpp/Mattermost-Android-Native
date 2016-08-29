package com.kilogramm.mattermost.view.chat;

import android.content.Context;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.databinding.ChatListItemBinding;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.viewmodel.chat.ItemChatViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class ChatListAdapter extends RealmRecyclerViewAdapter<Post, ChatListAdapter.MyViewHolder>{

    private static final String TAG = "ChatListAdapter";

    private Context context;
    private RecyclerView mRecyclerView;

    public ChatListAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Post> data,
                           RecyclerView mRecyclerView) {
        super(context, data, true);
        this.context = context;
        this.mRecyclerView = mRecyclerView;
    }

    static Object DATA_INVALIDATION = new Object();

    private boolean isForDataBinding(List<Object> payloads) {
        if (payloads == null || payloads.size() == 0) {
            return false;
        }
        for (Object obj : payloads) {
            if (obj != DATA_INVALIDATION) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = MyViewHolder.create(inflater, parent);
        holder.getmBinding().addOnRebindCallback(new OnRebindCallback() {
            public boolean onPreBind(ViewDataBinding binding) {
                return mRecyclerView != null && mRecyclerView.isComputingLayout();
            }
            public void onCanceled(ViewDataBinding binding) {
                if (mRecyclerView == null || mRecyclerView.isComputingLayout()) {
                    return;
                }
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position, DATA_INVALIDATION);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Post post = getData().get(position);
        Calendar curDate = Calendar.getInstance();
        Calendar preDate = Calendar.getInstance();
        Post prePost = null;
        Boolean isTitle = false;
        if(position-1 >= 0){
            prePost = getData().get(position-1);
            curDate.setTime(new Date(post.getCreateAt()));
            preDate.setTime(new Date(prePost.getCreateAt()));
            if(curDate.get(Calendar.DAY_OF_MONTH) != preDate.get(Calendar.DAY_OF_MONTH)){
                isTitle = true;
            }
        }
        holder.bindTo(post, context, isTitle);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if(isForDataBinding(payloads)){
            holder.getmBinding().executePendingBindings();
        } else {
            onBindViewHolder(holder, position);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ChatListItemBinding mBinding;

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            ChatListItemBinding binding = ChatListItemBinding
                    .inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        }

        private MyViewHolder(ChatListItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
        public void bindTo(Post post, Context context, Boolean isTitle) {
            mBinding.avatar.setTag(post);
            if(mBinding.getViewModel() == null){
                mBinding.setViewModel(new ItemChatViewModel(context, post));
                if(isTitle){
                    mBinding.getViewModel().setTitleVisibility(View.VISIBLE);
                }
            } else {
                mBinding.getViewModel().setPost(post);

            }
            mBinding.executePendingBindings();
        }

        public ChatListItemBinding getmBinding() {
            return mBinding;
        }


    }



}
