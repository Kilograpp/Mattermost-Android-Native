package com.kilogramm.mattermost.view.addchat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.kilogramm.mattermost.utils.ColorGenerator;

import java.util.ArrayList;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by melkshake on 19.10.16.
 */

public class AddExistingChannelsAdapter extends
        RealmRecyclerViewAdapter<ChannelsDontBelong, AddExistingChannelHolder> {

    private OnChannelItemClickListener mChannelClickListener;
    private ColorGenerator mColorGenerator;
    private ArrayList<Integer> mBackgroundColors;

    public AddExistingChannelsAdapter(@NonNull Context context,
                                      @Nullable OrderedRealmCollection<ChannelsDontBelong> data,
                                      boolean autoUpdate,
                                      OnChannelItemClickListener listener) {
        super(context, data, autoUpdate);
        this.mChannelClickListener = listener;
        mBackgroundColors = new ArrayList<>();
    }

    @Override
    public AddExistingChannelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return AddExistingChannelHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(AddExistingChannelHolder holder, int position) {
        mColorGenerator = ColorGenerator.MATERIAL;

        if (getData() != null) {
            for (int i = 0; i < getData().size(); i++) {
                mBackgroundColors.add(mColorGenerator.getRandomColor());
            }

            holder.bindTo(getData().get(position), mBackgroundColors.get(position));

            holder.getmBinding().getRoot().setOnClickListener(v -> {
                if (mChannelClickListener != null) {
                    mChannelClickListener.onChannelItemClick(
                            getData().get(position).getId(),
                            getData().get(position).getName(),
                            holder.getmTypeChannel());
                }
            });
        }
    }

    public interface OnChannelItemClickListener {
        void onChannelItemClick(String joinChannelId, String channelName, String type);
    }
}
