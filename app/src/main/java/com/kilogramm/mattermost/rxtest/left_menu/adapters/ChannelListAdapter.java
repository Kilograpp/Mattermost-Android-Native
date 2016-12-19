package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.rxtest.left_menu.OnLeftMenuClickListener;
import com.kilogramm.mattermost.rxtest.left_menu.holders.ChannelHolder;

import io.realm.RealmAD;
import io.realm.RealmQuery;
import io.realm.RealmResults;


public class ChannelListAdapter extends RealmAD<Channel, ChannelHolder> {

    private final Context mContext;

    private int mSelectedItem = -1;
    private LayoutInflater mInflater;
    private OnLeftMenuClickListener mChannelItemClickListener;
    private RealmResults<Member> mMembers;

    public ChannelListAdapter(RealmResults<Channel> adapterData,
                              Context context, RealmResults<Member> members,
                              OnLeftMenuClickListener listener) {
        super(adapterData);
        this.mContext = MattermostApp.get(context);
        this.mInflater = (LayoutInflater) this.mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.mMembers = members;
        this.mChannelItemClickListener = listener;
    }

    @Override
    public ChannelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ChannelHolder.create(mInflater, parent);
    }

    @Override
    public void onBindViewHolder(ChannelHolder holder, int position) {
        Channel channel = getData().get(position);
        Member member = getMember(channel.getId());

        holder.setClickListener(v -> {
            if (mChannelItemClickListener != null) {
                mChannelItemClickListener.onChannelClick(channel.getId(), channel.getDisplayName(),
                        channel.getType());
            }
            setSelectedItem(holder.getAdapterPosition());
            holder.setChecked(true);
        });
        if (holder.getAdapterPosition() == mSelectedItem) {
            holder.setChecked(true);
        } else {
            holder.setChecked(false);
        }
        holder.bindTo(channel, mContext, member);
    }

    private Member getMember(String channelId) {
        RealmQuery<Member> query = mMembers.where().equalTo("channelId", channelId);
        if (query.count() > 0) {
            return query.findFirst();
        } else {
            return null;
        }
    }

    public void setSelectedItem(int selecteditem) {
        this.mSelectedItem = selecteditem;
        notifyDataSetChanged();
    }

}