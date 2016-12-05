package com.kilogramm.mattermost.rxtest.left_menu.holders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.databinding.ItemChannelBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.ui.CheckableLinearLayout;

/**
 * Created by Evgeny on 14.11.2016.
 */

public class ChannelHolder extends BaseLeftHolder {

    private static final String TAG = "CAHNNEL_HOLDER";

    private ItemChannelBinding mBinding;

    public ChannelHolder(ItemChannelBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public static ChannelHolder create(LayoutInflater inflater, ViewGroup parent) {
        ItemChannelBinding binding = ItemChannelBinding.inflate(inflater, parent, false);
        return new ChannelHolder(binding);
    }

    public void setChecked(Boolean bool) {
        ((CheckableLinearLayout) mBinding.getRoot()).setChecked(bool);
    }

    public void bindTo(Channel channel, Context context, Member member) {
        mBinding.channelName.setText(channel.getDisplayName());
        if (member != null) {
            mBinding.unreadedMessage.setText(member.getMentionCount() != 0
                    ? member.getMentionCount().toString()
                    : "");
            if (!member.getMsgCount().equals(channel.getTotalMsgCount())) {
                setBold(context, mBinding.channelName, mBinding.unreadedMessage);
            } else {
                setDefault(context, mBinding.channelName, mBinding.unreadedMessage);
            }
        }
        if (mBinding.linearLayout.isChecked()) {
            setTextBlack(context, mBinding.channelName, mBinding.unreadedMessage);
        } else {
            setTextWhite(context, mBinding.channelName, mBinding.unreadedMessage);
        }
        mBinding.executePendingBindings();
    }

    public void setClickListener(View.OnClickListener clickListener) {
        mBinding.getRoot().setOnClickListener(clickListener);
    }
}
