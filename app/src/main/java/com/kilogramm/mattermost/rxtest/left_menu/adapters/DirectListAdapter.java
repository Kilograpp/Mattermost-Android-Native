package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.app.Service;
import android.content.Context;
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


public class DirectListAdapter extends RealmAD<Channel, DirectHolder> {

    private final Context mContext;
    private final RealmResults<UserStatus> mUserStatuses;
    private final RealmResults<Member> mMembers;

    private int mSelectedItem = -1;
    private LayoutInflater mInflater;
    private OnLeftMenuClickListener mItemClickListener;

    public DirectListAdapter(RealmResults<Channel> adapterData,
                             Context context,
                             OnLeftMenuClickListener listener,
                             RealmResults<Member> members,
                             RealmResults<UserStatus> userStatuses) {
        super(adapterData);
        this.mContext = MattermostApp.get(context);
        this.mInflater = (LayoutInflater) this.mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.mUserStatuses = userStatuses;
        this.mUserStatuses.addChangeListener(element -> notifyDataSetChanged());
        this.mItemClickListener = listener;
        this.mMembers = members;
    }

    @Override
    public DirectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return DirectHolder.create(mInflater, parent);
    }

    @Override
    public void onBindViewHolder(DirectHolder holder, int position) {
        Channel channel = getData().get(position);
        UserStatus userStatus;
        try {
            userStatus = getUserStatus(channel.getUser().getId());
        } catch (NullPointerException e){
            userStatus = new UserStatus("0", UserStatus.ONLINE);
        }
        Member member = getMember(channel.getId());


        holder.setClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onChannelClick(channel.getId(), channel.getUser().getUsername(),
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

        holder.bindTo(channel, mContext, userStatus, member);
    }

    public void setSelectedItem(int selecteditem) {
        this.mSelectedItem = selecteditem;
        notifyDataSetChanged();
    }

    private Member getMember(String channelId) {
        RealmQuery<Member> query = mMembers.where().equalTo("channelId", channelId);
        if (query.count() > 0) {
            return query.findFirst();
        } else {
            return null;
        }
    }

    private UserStatus getUserStatus(String userId) {
        RealmQuery<UserStatus> query = mUserStatuses.where().equalTo("id", userId);
        if (query.count() > 0) {
            return query.findFirst();
        } else {
            return null;
        }
    }


}
