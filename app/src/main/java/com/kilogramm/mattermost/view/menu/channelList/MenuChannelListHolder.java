package com.kilogramm.mattermost.view.menu.channelList;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemChannelBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;

/**
 * Created by Evgeny on 17.10.2016.
 */
public class MenuChannelListHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "MenuChannelListHolder";
    private ItemChannelBinding mBinding;

    public static MenuChannelListHolder create(LayoutInflater inflater, ViewGroup parent) {
        ItemChannelBinding binding = ItemChannelBinding.inflate(inflater, parent, false);
        return new MenuChannelListHolder(binding);
    }

    private MenuChannelListHolder(ItemChannelBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public void bindTo(Channel channel, Context context, Member member) {
        mBinding.channelName.setText(channel.getDisplayName());
       /* results.addChangeListener(element -> {*/
        Log.d(TAG, "OnChange Direct");
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
        mBinding.unreadedMessage.setText(member!=null
                ? member.getMentionCount()!=0
                    ? member.getMentionCount().toString()
                    : ""
                : "");
        /*});
        mBinding.unreadedMessage.setText(results.size()!=0
                ? results.first().getMentionCount()!=0
                        ? results.first().getMentionCount().toString()
                        : ""
                : "");*/
        if(mBinding.linearLayout.isChecked()){
            mBinding.channelName.setTextColor(context.getResources().getColor(R.color.black));
            mBinding.unreadedMessage.setTextColor(context.getResources().getColor(R.color.black));
        } else {
            mBinding.channelName.setTextColor(context.getResources().getColor(R.color.white));
            mBinding.unreadedMessage.setTextColor(context.getResources().getColor(R.color.white));
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

    public ItemChannelBinding getmBinding() {
        return mBinding;
    }
}
