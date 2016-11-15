package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.app.Service;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.rxtest.left_menu.OnLeftMenuClickListener;
import com.kilogramm.mattermost.rxtest.left_menu.holders.DirectHolder;

import io.realm.RealmAD;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 14.11.2016.
 */

public class DirectListAdapter extends RealmAD<Channel, DirectHolder> {

    private static final String TAG = "DIRECT_LIST_ADAPTER";
    private final Context context;
    private final RealmResults<UserStatus> userStatuses;
    private final RealmResults<Member> members;

    private int mSelectedItem = -1;
    private LayoutInflater inflater;
    private OnLeftMenuClickListener itemClickListener;

    public DirectListAdapter(RealmResults<Channel> adapterData,
                             Context context,
                             OnLeftMenuClickListener listener,
                             RealmResults<Member> members,
                             RealmResults<UserStatus> userStatuses) {
        super(adapterData);
        this.context = MattermostApp.get(context);
        this.inflater = (LayoutInflater) this.context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.userStatuses = userStatuses;
        this.userStatuses.addChangeListener(element -> notifyDataSetChanged());
        this.itemClickListener = listener;
        this.members = members;
    }

    @Override
    public DirectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DirectHolder holder = DirectHolder.create(inflater, parent);
        return holder;
    }

    @Override
    public void onBindViewHolder(DirectHolder holder, int position) {
        Channel channel = getData().get(position);
        UserStatus userStatus = getUserStatus(channel.getUser().getId());
        Member member = getMember(channel.getId());


        holder.setClickListener(v -> {
            Log.d(TAG, "onClickItem() holder");
            if (itemClickListener != null) {
                itemClickListener.onChannelClick(channel.getId(), channel.getUser().getUsername(),
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

        holder.bindTo(channel, context, userStatus, member);
    }

    private Member getMember(String channelId) {
        RealmQuery<Member> query = members.where().equalTo("channelId", channelId);
        if (query.count() > 0) {
            return query.findFirst();
        } else {
            return null;
        }
    }

    private UserStatus getUserStatus(String userId){
        RealmQuery<UserStatus> query = userStatuses.where().equalTo("id", userId);
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
