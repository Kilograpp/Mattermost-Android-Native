package com.kilogramm.mattermost.view.addchat;

import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemMoreChannelBinding;
import com.kilogramm.mattermost.model.entity.channel.ChannelsDontBelong;
import com.vdurmont.emoji.EmojiParser;

import io.realm.RealmViewHolder;

/**
 * Created by Evgeny on 31.10.2016.
 */

public class AddExistingChannelHolder extends RealmViewHolder {
    private ItemMoreChannelBinding mMoreBinding;
    private String mTypeChannel;

    private AddExistingChannelHolder(ItemMoreChannelBinding binding) {
        super(binding.getRoot());
        mMoreBinding = binding;
    }

    public void bindTo(ChannelsDontBelong channelDontBelong, int backgroundColor) {
        String firstLetter = String.valueOf(channelDontBelong.getName().charAt(0)).toUpperCase();
//        mMoreBinding.avatarChannel.setText(firstLetter);
        mMoreBinding.avatarChannel.setText(EmojiParser.removeAllEmojis(channelDontBelong.getDisplayName()).toUpperCase());
        mMoreBinding.avatarChannel.getBackground()
                .setColorFilter(backgroundColor, PorterDuff.Mode.MULTIPLY);

        mTypeChannel = channelDontBelong.getType();
        mMoreBinding.tvChannelName.setText(channelDontBelong.getDisplayName());
    }

    public String getmTypeChannel() {
        return mTypeChannel;
    }

    public ItemMoreChannelBinding getmBinding() {
        return mMoreBinding;
    }

    public static AddExistingChannelHolder create(LayoutInflater inflater, ViewGroup parent) {
        return new AddExistingChannelHolder(DataBindingUtil.inflate(inflater, R.layout.item_more_channel, parent, false));
    }
}