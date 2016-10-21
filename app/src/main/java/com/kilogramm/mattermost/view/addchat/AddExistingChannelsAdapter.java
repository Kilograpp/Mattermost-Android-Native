package com.kilogramm.mattermost.view.addchat;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemMoreChannelBinding;
import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;

import java.util.ArrayList;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmViewHolder;

/**
 * Created by melkshake on 19.10.16.
 */

public class AddExistingChannelsAdapter extends
        RealmRecyclerViewAdapter<ChannelsDontBelong, AddExistingChannelsAdapter.MyViewHolder> {

    private OnChannelItemClickListener channelClickListener;

    private ColorGenerator colorGenerator;
    private int backgroundColor;

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
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return MyViewHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        colorGenerator = ColorGenerator.MATERIAL;
        for (int i = 0; i < getData().size(); i++) {
            backgroundColors.add(colorGenerator.getRandomColor());
        }
//        backgroundColor = colorGenerator.getRandomColor();

        holder.bindTo(getData().get(position), backgroundColors.get(position));

        holder.getmBinding().getRoot().setOnClickListener(v -> {
            if (channelClickListener != null) {
                channelClickListener.onChannelItemClick(
                        getData().get(position).getId(),
                        getData().get(position).getName(),
                        holder.getTypeChannel());
            }
        });
    }

    public static class MyViewHolder extends RealmViewHolder {
        private ItemMoreChannelBinding moreBinding;
        private String typeChannel;

        private MyViewHolder(ItemMoreChannelBinding binding) {
            super(binding.getRoot());
            moreBinding = binding;
        }

        public static MyViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            return new MyViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_more_channel, parent, false));
        }

        public void bindTo(ChannelsDontBelong channelDontBelong, int backgroundColor) {
            String firstLetter = String.valueOf(channelDontBelong.getName().charAt(0)).toUpperCase();
            TextDrawable textDrawable = TextDrawable.builder().buildRound(firstLetter, backgroundColor);
            moreBinding.avatarChannel.setImageDrawable(textDrawable);

            typeChannel = channelDontBelong.getType();
            moreBinding.tvChannelName.setText(channelDontBelong.getName().toUpperCase());
        }

        public String getTypeChannel() {
            return typeChannel;
        }

        public ItemMoreChannelBinding getmBinding() {
            return moreBinding;
        }
    }

    public interface OnChannelItemClickListener {
        void onChannelItemClick(String joinChannelId, String channelName, String type);
    }
}
