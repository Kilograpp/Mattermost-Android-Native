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
    private static final String TAG = "AdapterPost";

    private static final int ITEM = -1;
    private static final int LOADING_TOP = -2;
    private static final int LOADING_BOTTOM = -3;

    private LayoutInflater mInflater;
    private Context mContext;
    private OnItemClickListener<String> mListener;

    private Boolean isTopLoading = false;
    private Boolean isBottomLoading = false;

    /**
     * String for {@link Post#id} of the highlighted post. Post will be highlighted if user come to this chat
     * by tap on this post in search results
     */
    private String mHighlightedPostId;

    public AdapterPost(Context context, RealmResults adapterData, OnItemClickListener<String> listener) {
        super(adapterData);
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mListener = listener;
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
        return count;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM:
                Log.d(TAG, "bindItem ");
                return PostViewHolder.createItem(mInflater, parent);
            case LOADING_TOP:
                Log.d(TAG, "bindTop ");
                return PostViewHolder.createLoadingTop(mInflater, parent);
            case LOADING_BOTTOM:
                Log.d(TAG, "bindBot ");
                return PostViewHolder.createLoadingBottom(mInflater, parent);
            default:
                return PostViewHolder.createItem(mInflater, parent);
        }
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM) {
            int pos = isTopLoading ? position - 1 : position;
            if (getData().get(pos).isValid()) {
                Post post = getData().get(pos);
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
                            showAllViews(holder);
                        }
                    } else {
                        showAllViews(holder);
                    }
                } else {
                    showAllViews(holder);
                }

                holder.bindToItem(post, mContext, isTitle, root, mListener);
                holder.changeChatItemBackground(mContext, mHighlightedPostId != null && mHighlightedPostId.equals(post.getId()));
            } else {
                Log.d(TAG, " NOT VALID onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");
            }
        } else {
            holder.bindToLoadingBottom();
        }
    }

    /**
     * Shows avatar, time and nickname for post.
     *
     * @param holder
     */
    private void showAllViews(PostViewHolder holder) {
        ((ChatListItemBinding) holder.getmBinding()).time.setVisibility(View.VISIBLE);
        ((ChatListItemBinding) holder.getmBinding()).nick.setVisibility(View.VISIBLE);
        ((ChatListItemBinding) holder.getmBinding()).avatar.setVisibility(View.VISIBLE);
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
        return count;
    }

    public String getmHighlightedPostId() {
        return mHighlightedPostId;
    }

    /**
     * Set id for {@link Post}, that must be highlighted.
     *
     * @param mHighlightedPostId id of the post that must be highlighted
     */
    public void setmHighlightedPostId(String mHighlightedPostId) {
        this.mHighlightedPostId = mHighlightedPostId;
    }
}
