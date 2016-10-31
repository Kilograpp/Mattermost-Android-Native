package com.kilogramm.mattermost.view.menu.channelList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemChannelBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;

/**
 * Created by Evgeny on 17.10.2016.
 */
public class MenuChannelListHolder extends RecyclerView.ViewHolder {

    private ItemChannelBinding mBinding;

    public static MenuChannelListHolder create(LayoutInflater inflater, ViewGroup parent) {
        ItemChannelBinding binding = ItemChannelBinding.inflate(inflater, parent, false);
        return new MenuChannelListHolder(binding);
    }

    private MenuChannelListHolder(ItemChannelBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public void bindTo(Channel channel, Context context) {
        mBinding.channelName.setText(channel.getDisplayName());
        if(mBinding.linearLayout.isChecked()){
            mBinding.channelName.setTextColor(context.getResources().getColor(R.color.black));
            mBinding.unreadedMessage.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            mBinding.channelName.setTextColor(context.getResources().getColor(R.color.white));
            mBinding.unreadedMessage.setTextColor(context.getResources().getColor(R.color.white));
        }
        mBinding.executePendingBindings();
    }

    public ItemChannelBinding getmBinding() {
        return mBinding;
    }
}
