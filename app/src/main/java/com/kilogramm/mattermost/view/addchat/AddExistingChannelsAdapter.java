package com.kilogramm.mattermost.view.addchat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.kilogramm.mattermost.utils.ColorGenerator;

import java.util.ArrayList;
import java.util.Objects;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by melkshake on 19.10.16.
 */

public class AddExistingChannelsAdapter extends
        RealmRecyclerViewAdapter<ChannelsDontBelong, AddExistingChannelHolder> {

    private OnChannelItemClickListener channelClickListener;

    private ColorGenerator colorGenerator;
    private ArrayList<Integer> backgroundColors;

    public AddExistingChannelsAdapter(@NonNull Context context,
                                      @Nullable OrderedRealmCollection<ChannelsDontBelong> data,
                                      boolean autoUpdate,
                                      OnChannelItemClickListener listener) {
        super(context, data, autoUpdate);
        this.channelClickListener = listener;
        backgroundColors = new ArrayList<>();
    }

    @Override
    public AddExistingChannelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return AddExistingChannelHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(AddExistingChannelHolder holder, int position) {
        colorGenerator = ColorGenerator.MATERIAL;

        if (getData() != null) {
            for (int i = 0; i < getData().size(); i++) {
                backgroundColors.add(colorGenerator.getRandomColor());
            }

            holder.bindTo(getData().get(position), backgroundColors.get(position));

            holder.getmBinding().getRoot().setOnClickListener(v -> {
                if (channelClickListener != null) {
                    channelClickListener.onChannelItemClick(
                            getData().get(position).getId(),
                            getData().get(position).getDisplayName(),
                            holder.getTypeChannel());
                }
            });
        }
    }

    public interface OnChannelItemClickListener {
        void onChannelItemClick(String joinChannelId, String channelName, String type);
    }
}
