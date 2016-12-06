package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.app.Service;
import android.content.Context;
import android.util.Log;
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

/**
 * Created by Evgeny on 14.11.2016.
 */

public class ChannelListAdapter extends RealmAD<Channel, ChannelHolder> {

    private static final String TAG = "CHANNEL_LIST_ADAPTER";

    private final Context context;

    private int mSelectedItem = -1;
    private LayoutInflater inflater;
    private OnLeftMenuClickListener channelItemClickListener;
    private RealmResults<Member> members;

    public ChannelListAdapter(RealmResults<Channel> adapterData,
                              Context context, RealmResults<Member> members,
                              OnLeftMenuClickListener listener) {
        super(adapterData);
        this.context = MattermostApp.get(context);
        this.inflater = (LayoutInflater) this.context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.members = members;
        this.channelItemClickListener = listener;
    }

    @Override
    public ChannelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ChannelHolder holder = ChannelHolder.create(inflater, parent);
        return holder;
    }

    @Override
    public void onBindViewHolder(ChannelHolder holder, int position) {
        Channel channel = getData().get(position);
        Member member = getMember(channel.getId());

        holder.setClickListener(v -> {
            Log.d(TAG, "onClickItem() holder");
            Log.d(TAG, "TYPE = " + channel.getType());
            if (channelItemClickListener != null) {
                channelItemClickListener.onChannelClick(channel.getId(), channel.getDisplayName(),
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
        holder.bindTo(channel, context, member);
    }

    private Member getMember(String channelId) {
        RealmQuery<Member> query = members.where().equalTo("channelId", channelId);
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