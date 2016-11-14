package com.kilogramm.mattermost.rxtest.left_menu.holders;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemChannelBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.ui.CheckableLinearLayout;

/**
 * Created by Evgeny on 14.11.2016.
 */

public class ChannelHolder extends RecyclerView.ViewHolder {

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

    public void setChecked(Boolean bool){
        ((CheckableLinearLayout) mBinding.getRoot()).setChecked(bool);
    }

    public void bindTo(Channel channel, Context context, Member member) {
        mBinding.channelName.setText(channel.getDisplayName());
        if(member != null){
            mBinding.unreadedMessage.setText(member.getMentionCount()!=0
                    ? member.getMentionCount().toString()
                    : "");
            if(!member.getMsgCount().equals(channel.getTotalMsgCount())){
                setBold(context);
            } else {
                setDefault(context);
            }
        }
        if(mBinding.linearLayout.isChecked()){
            setTextBlack(context);
        } else {
            setTextWhite(context);
        }
        mBinding.executePendingBindings();
    }

    private void setTextWhite(Context context) {
        mBinding.channelName.setTextColor(context.getResources().getColor(R.color.white));
        mBinding.unreadedMessage.setTextColor(context.getResources().getColor(R.color.white));
    }

    private void setTextBlack(Context context) {
        mBinding.channelName.setTextColor(context.getResources().getColor(R.color.black));
        mBinding.unreadedMessage.setTextColor(context.getResources().getColor(R.color.black));
    }

    private void setDefault(Context context) {
        mBinding.channelName.setTypeface(Typeface.DEFAULT);
        mBinding.channelName.setTextSize(12f);
        mBinding.channelName.setTextColor(context.getResources().getColor(R.color.very_light_grey));
        mBinding.unreadedMessage.setTypeface(Typeface.DEFAULT);
        mBinding.unreadedMessage.setTextSize(12f);
        mBinding.unreadedMessage.setTextColor(context.getResources().getColor(R.color.very_light_grey));
    }

    private void setBold(Context context) {
        mBinding.channelName.setTypeface(null, Typeface.BOLD);
        mBinding.channelName.setTextSize(15f);
        mBinding.channelName.setTextColor(context.getResources().getColor(R.color.white));
        mBinding.unreadedMessage.setTypeface(null, Typeface.BOLD);
        mBinding.unreadedMessage.setTextSize(15f);
        mBinding.unreadedMessage.setTextColor(context.getResources().getColor(R.color.white));
    }

    public void setClickListener(View.OnClickListener clickListener) {
        mBinding.getRoot().setOnClickListener(clickListener);
    }
}
