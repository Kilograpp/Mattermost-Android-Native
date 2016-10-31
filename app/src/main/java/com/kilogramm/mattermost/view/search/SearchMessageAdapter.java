package com.kilogramm.mattermost.view.search;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;


import com.kilogramm.mattermost.model.entity.post.Post;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by melkshake on 03.10.16.
 */

public class SearchMessageAdapter extends RealmRecyclerViewAdapter<Post, SearchMessageHolder> {

    private OnJumpClickListener jumpClickListener;
    private String terms;
    private Context context;

    public SearchMessageAdapter(@NonNull Context context,
                                @Nullable OrderedRealmCollection<Post> data,
                                boolean autoUpdate,
                                OnJumpClickListener listener,
                                String terms) {
        super(context, data, autoUpdate);
        this.jumpClickListener = listener;
        this.terms = terms;
        this.context = context;
    }

    @Override
    public SearchMessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return SearchMessageHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(SearchMessageHolder holder, int position) {
        holder.bindTo(getData().get(position), context, terms);

        String messageId = getData().get(position).getId();
        String channelId = getData().get(position).getChannelId();

        holder.getmBinding().getRoot().setOnClickListener(v -> {
            if (jumpClickListener != null) {
                jumpClickListener.onJumpClick(
                        messageId,
                        channelId,
                        holder.getmBinding().chatName.getText().toString(),
                        holder.getTypeChannel());
            }
        });
    }

    public interface OnJumpClickListener {
        void onJumpClick(String messageId, String channelId, String channelName, String type);
    }
}
