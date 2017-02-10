package com.kilogramm.mattermost.rxtest.left_menu.holders;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDirectionProfileChannelBinding;
import com.kilogramm.mattermost.model.entity.user_v2.UserV2;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.rxtest.left_menu.direct.DirectItem;
import com.kilogramm.mattermost.ui.CheckableLinearLayout;

/**
 * Created by Evgeny on 18.01.2017.
 */

public class AdapterDirectItemHolder extends BaseLeftHolder {

    private ItemDirectionProfileChannelBinding mBinding;

    private AdapterDirectItemHolder(ItemDirectionProfileChannelBinding binding) {
        super(binding.getRoot());
        this.mBinding = binding;
    }

    public static AdapterDirectItemHolder create(LayoutInflater inflater, ViewGroup parent) {
        ItemDirectionProfileChannelBinding binding = ItemDirectionProfileChannelBinding
                .inflate(inflater, parent, false);
        return new AdapterDirectItemHolder(binding);
    }

    public void setChecked(Boolean bool) {
        ((CheckableLinearLayout) mBinding.getRoot()).setChecked(bool);
    }

    public void setClickListener(View.OnClickListener clickListener) {
        mBinding.getRoot().setOnClickListener(clickListener);
    }

    private Drawable getStatusIconDrawable(String status, Context context) {
        Resources resources = context.getResources();
        switch (status) {
            case UserStatus.ONLINE:
                return resources.getDrawable(R.drawable.status_online_drawable);
            case UserStatus.OFFLINE:
                return resources.getDrawable(R.drawable.status_offline_drawable);
            case UserStatus.AWAY:
                return resources.getDrawable(R.drawable.status_away_drawable);
            case UserStatus.REFRESH:
                return resources.getDrawable(R.drawable.status_refresh_drawable);
            default:
                return resources.getDrawable(R.drawable.status_offline_drawable);
        }
    }

    public void bindTo(DirectItem item, Context mContext) {
        mBinding.channelName.setText(item.username);
        mBinding.unreadedMessage.setText(item.mentionCount != 0
                ? "" + item.mentionCount
                : "");
        if (item.msgCount != item.totalMessageCount) {
            setBold(mContext, mBinding.channelName, mBinding.unreadedMessage);
        } else {
            setDefault(mContext, mBinding.channelName, mBinding.unreadedMessage);
        }

        if (mBinding.linearLayout.isChecked()) {
            setTextBlack(mContext, mBinding.channelName, mBinding.unreadedMessage);
        } else {
            setTextWhite(mContext, mBinding.channelName, mBinding.unreadedMessage);
        }
        mBinding.channelName
                .setCompoundDrawablesWithIntrinsicBounds(getStatusIconDrawable(item.status, mContext),
                        null, null, null);

        mBinding.executePendingBindings();
    }

    public void bindTo(UserV2 user, Context mContext) {
        mBinding.channelName.setText(user.getUsername());
//        mBinding.unreadedMessage.setText(item.mentionCount != 0
//                ? "" + item.mentionCount
//                : "");
//        if (item.msgCount != item.totalMessageCount) {
//            setBold(mContext, mBinding.channelName, mBinding.unreadedMessage);
//        } else {
//            setDefault(mContext, mBinding.channelName, mBinding.unreadedMessage);
//        }

//        if (mBinding.linearLayout.isChecked()) {
//            setTextBlack(mContext, mBinding.channelName, mBinding.unreadedMessage);
//        } else {
//            setTextWhite(mContext, mBinding.channelName, mBinding.unreadedMessage);
//        }
        mBinding.channelName
                .setCompoundDrawablesWithIntrinsicBounds(getStatusIconDrawable(user.getStatus(), mContext),
                        null, null, null);

        mBinding.executePendingBindings();
    }


}
