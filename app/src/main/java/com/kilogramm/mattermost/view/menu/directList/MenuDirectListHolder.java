package com.kilogramm.mattermost.view.menu.directList;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDirectionProfileChannelBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;

/**
 * Created by Evgeny on 31.10.2016.
 */

public class MenuDirectListHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "MenuDirectListHolder";
    private ItemDirectionProfileChannelBinding mBinding;

    public static MenuDirectListHolder create(LayoutInflater inflater, ViewGroup parent) {
        ItemDirectionProfileChannelBinding binding = ItemDirectionProfileChannelBinding
                .inflate(inflater, parent, false);
        return new MenuDirectListHolder(binding);
    }

    private MenuDirectListHolder(ItemDirectionProfileChannelBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public void bindTo(Channel channel, Context context, UserStatus status, Member member) {
        mBinding.channelName.setText(channel.getUser().getUsername());
       /* results.addChangeListener(element -> {*/
            Log.d(TAG, "OnChange Direct");
            if(member != null){
                mBinding.unreadedMessage.setText(member.getMentionCount()!=0
                        ? member.getMentionCount().toString()
                        : "");
                if(!member.getMsgCount().equals(channel.getTotalMsgCount())) {
                    setBold(context);
                } else {
                    setDefault(context);
                }
            }
            mBinding.unreadedMessage.setText(member!=null
                    ? member.getMentionCount()!=0
                        ? member.getMentionCount().toString()
                        : ""
                    : "");
       /* });
        mBinding.unreadedMessage.setText(results.size()!=0
                ? results.first().getMentionCount()!=0
                    ? results.first().getMentionCount().toString()
                    : ""
                : "");*/
        if (mBinding.linearLayout.isChecked()) {
            mBinding.channelName.setTextColor(context.getResources().getColor(R.color.black));
            mBinding.unreadedMessage.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            mBinding.channelName.setTextColor(context.getResources().getColor(R.color.white));
            mBinding.unreadedMessage.setTextColor(context.getResources().getColor(R.color.white));
        }
        if(status!=null){
            mBinding.channelName
                    .setCompoundDrawablesWithIntrinsicBounds(getStatusIconDrawable(status, context),
                            null, null, null);
        } else {
            mBinding.channelName
                    .setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.status_offline_drawable),
                            null, null, null);
        }

        mBinding.executePendingBindings();
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
    public ItemDirectionProfileChannelBinding getmBinding() {
        return mBinding;
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
}