package com.kilogramm.mattermost.rxtest.left_menu.holders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDirectionProfileChannelBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.ui.CheckableLinearLayout;

/**
 * Created by Evgeny on 14.11.2016.
 */

public class DirectHolder extends BaseLeftHolder {

    private static final String TAG = "DIRECT_HOLDER";
    private ItemDirectionProfileChannelBinding mBinding;

    private DirectHolder(ItemDirectionProfileChannelBinding binding) {
        super(binding.getRoot());
        this.mBinding = binding;
    }

    public static DirectHolder create(LayoutInflater inflater, ViewGroup parent) {
        ItemDirectionProfileChannelBinding binding = ItemDirectionProfileChannelBinding
                .inflate(inflater, parent, false);
        return new DirectHolder(binding);
    }

    public void setChecked(Boolean bool){
        ((CheckableLinearLayout) mBinding.getRoot()).setChecked(bool);
    }

    public void bindTo(Channel channel, Context context, UserStatus status, Member member) {
        mBinding.channelName.setText(channel.getUser().getUsername());
       /* results.addChangeListener(element -> {*/
        if(member != null){
            mBinding.unreadedMessage.setText(member.getMentionCount()!=0
                    ? member.getMentionCount().toString()
                    : "");
            if(!member.getMsgCount().equals(channel.getTotalMsgCount())) {
                setBold(context, mBinding.channelName, mBinding.unreadedMessage);
            } else {
                setDefault(context, mBinding.channelName, mBinding.unreadedMessage);
            }
        }
        if(mBinding.linearLayout.isChecked()){
            setTextBlack(context, mBinding.channelName, mBinding.unreadedMessage);
        } else {
            setTextWhite(context, mBinding.channelName, mBinding.unreadedMessage);
        }
        if(status!=null){
            mBinding.channelName
                    .setCompoundDrawablesWithIntrinsicBounds(getStatusIconDrawable(status, context),
                            null, null, null);
        } else {
            mBinding.channelName
                    .setCompoundDrawablesWithIntrinsicBounds(context.getResources()
                            .getDrawable(R.drawable.status_offline_drawable), null, null, null);
        }

        mBinding.executePendingBindings();
    }

    public Drawable getStatusIconDrawable(UserStatus status, Context context) {
        switch (status.getStatus()){
            case UserStatus.ONLINE:
                return context.getResources().getDrawable(R.drawable.status_online_drawable);
            case UserStatus.OFFLINE:
                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
            case UserStatus.AWAY:
                return context.getResources().getDrawable(R.drawable.status_away_drawable);
            case UserStatus.REFRESH:
                return context.getResources().getDrawable(R.drawable.status_refresh_drawable);
            default:
                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
        }
    }

    public void setClickListener(View.OnClickListener clickListener) {
        mBinding.getRoot().setOnClickListener(clickListener);
    }
}
