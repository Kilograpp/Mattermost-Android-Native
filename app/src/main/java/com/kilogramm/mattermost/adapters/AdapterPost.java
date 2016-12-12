package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.databinding.ChatListItemBinding;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.view.chat.OnItemClickListener;
import com.kilogramm.mattermost.view.chat.PostViewHolder;

import java.util.Calendar;
import java.util.Date;

import io.realm.RealmAD;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 10.10.2016.
 */
public class AdapterPost extends RealmAD<Post, PostViewHolder> {
    public static final String TAG = "AdapterPost";

    private PostViewHolder mHolder;

    public static final int ITEM = -1;
    public static final int LOADING_TOP = -2;
    public static final int LOADING_BOTTOM = -3;

    private LayoutInflater inflater;
    private Context context;
    private OnItemClickListener<String> listener;

    private Boolean isTopLoading = false;
    private Boolean isBottomLoading = false;

    private String highlitedPost;

    public AdapterPost(Context context, RealmResults adapterData, OnItemClickListener<String> listener) {
        super(adapterData);
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && isTopLoading) {
            Log.d(TAG, "Loading_Top = " + position);
            return LOADING_TOP;
        } else if (position == getItemCount() - 1 && isBottomLoading) {
            Log.d(TAG, "Loading_Bottom = " + (getItemCount() - 1));
            return LOADING_BOTTOM;
        } else {
            return ITEM;
        }
    }

    @Override
    public int getItemCount() {
        int count = super.getItemCount();

        if (isBottomLoading) {
            count++;
        }
        if (isTopLoading) {
            count++;
        }
        //Log.d(TAG,"super.getItemCount() = "+super.getItemCount() + "\n count = " + count);
        return count;
    }

    public void setLoadingTop(Boolean enabled) {
        Log.d(TAG, "setLoadingTop(" + enabled + ");");
        if (isTopLoading != enabled) {
            isTopLoading = enabled;
            getItemCount();
            if (enabled) {
                notifyItemInserted(0);
            } else {
                notifyItemRemoved(0);
            }
        }
    }

    public void setLoadingBottom(Boolean enabled) {
        Log.d(TAG, "setLoadingBottom(" + enabled + ");");
        if (isBottomLoading != enabled) {
            isBottomLoading = enabled;
            getItemCount();
            if (enabled) {
                notifyItemInserted(getItemCount());
            } else {
                notifyItemRemoved(getItemCount());
            }
        }
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM:
                Log.d(TAG, "bindItem ");
                return PostViewHolder.createItem(inflater, parent);
            case LOADING_TOP:
                Log.d(TAG, "bindTop ");
                return PostViewHolder.createLoadingTop(inflater, parent);
            case LOADING_BOTTOM:
                Log.d(TAG, "bindBot ");
                return PostViewHolder.createLoadingBottom(inflater, parent);
            default:
                return PostViewHolder.createItem(inflater, parent);
        }
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        this.mHolder = holder;

        if (getItemViewType(position) == ITEM) {
            int pos = isTopLoading ? position - 1 : position;
            Post post = getData().get(isTopLoading ? position - 1 : position);
            Calendar curDate = Calendar.getInstance();
            Calendar preDate = Calendar.getInstance();
            Post prePost;
            Boolean isTitle = false;
            Post root = null;
            if (pos - 1 >= 0) {
                prePost = getData().get(pos - 1);
                curDate.setTime(new Date(post.getCreateAt()));
                preDate.setTime(new Date(prePost.getCreateAt()));
                if (curDate.get(Calendar.DAY_OF_MONTH) != preDate.get(Calendar.DAY_OF_MONTH)) {
                    isTitle = true;
                }
                if (post.getRootId() != null
                        && post.getRootId().length() > 0
                        && getData().where().equalTo("id", post.getRootId()).findAll().size() != 0) {
                    root = getData().where().equalTo("id", post.getRootId()).findFirst();
                }
            }
            if (pos - 1 == -1) {
                isTitle = true;
            }
            Post postAbove;
            if (holder.getAdapterPosition() >= 1 && post != null) {
                postAbove = getItem(holder.getAdapterPosition() - 1);
                if (!post.isSystemMessage() && !isTitle && post.getProps() == null) {
                    if (postAbove != null
                            && postAbove.getUser() != null && post.getUser() != null
                            && postAbove.getUser().getId().equals(post.getUser().getId())) {
                        ((ChatListItemBinding) holder.getmBinding()).time.setVisibility(View.GONE);
                        ((ChatListItemBinding) holder.getmBinding()).nick.setVisibility(View.GONE);
                        ((ChatListItemBinding) holder.getmBinding()).avatar.setVisibility(View.GONE);
                    } else {
                        ((ChatListItemBinding) holder.getmBinding()).time.setVisibility(View.VISIBLE);
                        ((ChatListItemBinding) holder.getmBinding()).nick.setVisibility(View.VISIBLE);
                        ((ChatListItemBinding) holder.getmBinding()).avatar.setVisibility(View.VISIBLE);
                    }
                } else {
                    ((ChatListItemBinding) holder.getmBinding()).time.setVisibility(View.VISIBLE);
                    ((ChatListItemBinding) holder.getmBinding()).nick.setVisibility(View.VISIBLE);
                    ((ChatListItemBinding) holder.getmBinding()).avatar.setVisibility(View.VISIBLE);
                }
            }

            holder.bindToItem(post, context, isTitle, root, listener);
            holder.changeChatItemBackground(context, highlitedPost != null && highlitedPost.equals(post.getId()));
        } else {
            holder.bindToLoadingBottom();
        }
    }

    public Boolean getTopLoading() {
        return isTopLoading;
    }

    public Boolean getBottomLoading() {
        return isBottomLoading;
    }


    public int getPositionById(String id) {
        int count = super.getPositionById(id);

        if (isTopLoading) {
            count++;
        }
        //Log.d(TAG,"super.getItemCount() = "+super.getItemCount() + "\n count = " + count);
        return count;
    }

    public String getHighlitedPost() {
        return highlitedPost;
    }

    public void setHighlitedPost(String highlitedPost) {
        this.highlitedPost = highlitedPost;
    }
}
