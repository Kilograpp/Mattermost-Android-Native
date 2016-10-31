package com.kilogramm.mattermost.view.addchat;

import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.kilogramm.mattermost.databinding.ItemMoreChannelBinding;

import io.realm.RealmViewHolder;

/**
 * Created by Evgeny on 31.10.2016.
 */

public class AddExistingChannelHolder extends RealmViewHolder {
    private ItemMoreChannelBinding moreBinding;
    private String typeChannel;

    private AddExistingChannelHolder(ItemMoreChannelBinding binding) {
        super(binding.getRoot());
        moreBinding = binding;
    }

    public static AddExistingChannelHolder create(LayoutInflater inflater, ViewGroup parent) {
        return new AddExistingChannelHolder(DataBindingUtil.inflate(inflater, R.layout.item_more_channel, parent, false));
    }

    public void bindTo(ChannelsDontBelong channelDontBelong, int backgroundColor) {
        String firstLetter = String.valueOf(channelDontBelong.getName().charAt(0)).toUpperCase();
        moreBinding.avatarChannel.setText(firstLetter);
        moreBinding.avatarChannel.getBackground()
                .setColorFilter(backgroundColor, PorterDuff.Mode.MULTIPLY);

        typeChannel = channelDontBelong.getType();
        moreBinding.tvChannelName.setText(channelDontBelong.getDisplayName().toUpperCase());
    }

    public String getTypeChannel() {
        return typeChannel;
    }

    public ItemMoreChannelBinding getmBinding() {
        return moreBinding;
    }
}