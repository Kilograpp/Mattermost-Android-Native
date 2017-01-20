package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.app.Service;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.UserMember;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.rxtest.left_menu.OnLeftMenuClickListener;
import com.kilogramm.mattermost.rxtest.left_menu.direct.DirectHeader;
import com.kilogramm.mattermost.rxtest.left_menu.direct.DirectItem;
import com.kilogramm.mattermost.rxtest.left_menu.direct.IDirect;
import com.kilogramm.mattermost.rxtest.left_menu.holders.AdapterDirectHeaderHolder;
import com.kilogramm.mattermost.rxtest.left_menu.holders.AdapterDirectItemHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 17.01.2017.
 */

public class AdapterDirectMenuLeft extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "AdapterDirectMenuLeft";

    private LayoutInflater mInflater;
    private Context mContext;

    private LinkedHashMap<String, IDirect> mData = new LinkedHashMap<>();
    private List<IDirect> mAdapterData = new ArrayList<>();

    private int mSelectedItem = -1;
    private OnLeftMenuClickListener mItemClickListener;

    public AdapterDirectMenuLeft(RealmResults<Channel> data,
                                 Context context,
                                 OnLeftMenuClickListener mItemClickListener) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.mItemClickListener = mItemClickListener;
        addOrUpdate(data);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == IDirect.TYPE_HEADER){
            return AdapterDirectHeaderHolder.create(mInflater, parent);
        } else {
            return AdapterDirectItemHolder.create(mInflater, parent);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapterData.get(position).getType();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == IDirect.TYPE_HEADER) {
            DirectHeader header = (DirectHeader) mAdapterData.get(position);
            AdapterDirectHeaderHolder viewHolder = (AdapterDirectHeaderHolder) holder;
            viewHolder.bindTo(header);
        } else {
            DirectItem item = (DirectItem) mAdapterData.get(position);
            AdapterDirectItemHolder viewHolder = (AdapterDirectItemHolder) holder;
            viewHolder.setClickListener(v -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onChannelClick(item.channelId, item.username,
                            Channel.DIRECT);
                }
                setSelectedItem(holder.getAdapterPosition());
                viewHolder.setChecked(true);
            });
            if (viewHolder.getAdapterPosition() == mSelectedItem) {
                viewHolder.setChecked(true);
            } else {
                viewHolder.setChecked(false);
            }
            viewHolder.bindTo(item , mContext);
        }
    }

    @Override
    public int getItemCount() {
        return mAdapterData.size();
    }

    private void sort(){
        if(mData.size()!=0) {
            Collection<IDirect> directs = mData.values();
            List<IDirect> list = Stream.of(directs)
                    .filter(iDirect -> iDirect instanceof DirectItem)
                    .groupBy(directItem -> ((DirectItem) directItem).inTeam)
                    .sortBy(entry -> !entry.getKey())
                    .flatMap(entry -> {
                        List<IDirect> value = entry.getValue();
                        Collections.sort(value, (o1, o2) -> ((DirectItem) o1).username.compareTo(((DirectItem) o2).username));
                        if (!entry.getKey()) {
                            value.add(0, new DirectHeader("Outside this team"));
                        }
                        return Stream.of(value);
                    }).collect(Collectors.toList());
            mAdapterData.clear();
            mAdapterData.addAll(list);
            /*mHandler.removeCallbacks(sendNotifyDataSetChange);
            mHandler.postDelayed(sendNotifyDataSetChange,100);*/
            //notifyDataSetChanged();
        }
    }


    public void addOrUpdate(RealmResults<Channel> channels){

        Log.d(TAG, "addOrUpdate: " + Thread.currentThread().getId() + "mData size = " + mData.size());
        List<String> newIds = Stream.of(channels).map(channel -> channel.getId()).collect(Collectors.toList());
        Set<String> oldIds = new HashSet<>(mData.keySet());
        oldIds.removeAll(newIds);
        for (String oldId : oldIds) {
            mData.remove(oldId);
        }
        if(channels!=null && channels.size()!=0){
            for (Channel channel : channels) {
                if(!mData.containsKey(channel.getId())){
                    DirectItem item = buildIDirectItem(channel);
                    mData.put(channel.getId(),item);
                }
            }
        }
        Log.d(TAG, "addOrUpdate: data size = " + mData.size());
        sort();

    }

    private DirectItem buildIDirectItem(Channel channel){
        long startTime = System.currentTimeMillis();
        DirectItem item = new DirectItem();
        item.channelId = channel.getId();
        item.totalMessageCount = channel.getTotalMsgCount();
        Realm realmO  = Realm.getDefaultInstance();
        realmO.executeTransaction(realm -> {
            String userId = channel.getName().replace(MattermostPreference.getInstance().getMyUserId(), "");
            userId = userId.replace("__", "");
            RealmResults<User> users = realm.where(User.class).equalTo("id", userId).findAll();
            if(users.size()!=0){
                item.userId = users.first().getId();
                item.username = users.first().getUsername();
            } else {
                item.userId = null;
                item.username = null;
            }
            RealmResults<UserStatus> statuses = realm.where(UserStatus.class).equalTo("id", userId).findAll();
            if(statuses.size()!=0){
                item.status = statuses.first().getStatus();
            } else {
                item.status = UserStatus.OFFLINE;
            }
            RealmResults<Member> members = realm.where(Member.class).equalTo("channelId",channel.getId()).findAll();
            if(members.size()!=0){
                item.mentionCount = members.first().getMentionCount();
                item.msgCount = members.first().getMsgCount();
            } else {
                item.mentionCount = 0;
            }
            RealmResults<UserMember> userMembers = realm.where(UserMember.class).equalTo("userId", userId).findAll();
            item.inTeam = userMembers.size()!=0;
        });
        realmO.close();
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        Log.d(TAG, "addOrUpdate: " + duration +  "milsec");
        return item;
    }

    public void printData(Collection<IDirect> iDirects){
        for (IDirect iDirect : iDirects) {
            Log.d(TAG, "printData: " + iDirect.toString());

        }
    }

    public List<IDirect> getAdapterData() {
        return this.mAdapterData;
    }

    public void setSelectedItem(int selecteditem) {
        this.mSelectedItem = selecteditem;
        notifyDataSetChanged();
    }

    public int getPositionById(String id) {
        for (IDirect iDirect : this.mAdapterData) {
            if(iDirect instanceof DirectItem){
                if(((DirectItem) iDirect).channelId.equals(id)){
                    return this.mAdapterData.contains(iDirect)
                            ? this.mAdapterData.indexOf(iDirect)
                            : -1;
                }
            }
        }
        return -1;
    }

    public void invalidateStatus(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<UserStatus> newStatuses = realm.where(UserStatus.class).findAll();
        for (IDirect iDirect : this.mAdapterData) {
            if(iDirect.getType()==IDirect.TYPE_ITEM){
                realm.executeTransaction(realm1 -> {
                    UserStatus status = newStatuses.where()
                            .equalTo("id",((DirectItem) iDirect).userId)
                            .findFirst();
                    if(status!=null){
                        if(!status.getStatus().equals(((DirectItem) iDirect).status)){
                            ((DirectItem) iDirect).status = status.getStatus();
                            Log.d(TAG, "invalidateStatus: Status changed: " + iDirect.toString());
                            notifyItemChanged(mAdapterData.indexOf(iDirect));
                        }
                    }
                });
            }
        }
        realm.close();
    }

    public void invalidateMember() {
        for (IDirect iDirect : this.mAdapterData) {
            if(iDirect instanceof DirectItem){
                Realm realmO = Realm.getDefaultInstance();
                realmO.executeTransaction(realm -> {
                    Member member = realm.where(Member.class).equalTo("channelId",((DirectItem) iDirect).channelId).findFirst();
                    Channel channel = realm.where(Channel.class).equalTo("id", ((DirectItem) iDirect).channelId).findFirst();
                    if(member!=null && channel!=null &&
                            (member.getMentionCount() != ((DirectItem) iDirect).mentionCount
                                    || member.getMsgCount() != ((DirectItem) iDirect).msgCount
                                    || channel.getTotalMsgCount() != ((DirectItem) iDirect).totalMessageCount)){
                        ((DirectItem) iDirect).mentionCount = member.getMentionCount();
                        ((DirectItem) iDirect).msgCount = member.getMsgCount();
                        ((DirectItem) iDirect).totalMessageCount = channel.getTotalMsgCount();
                        Log.d(TAG, "invalidateStatus: Member changed: " + iDirect.toString());
                        notifyItemChanged(mAdapterData.indexOf(iDirect));
                    }
                });
                realmO.close();
            }
        }
    }

    public void invalidateUsermember() {
        for (IDirect iDirect : this.mAdapterData) {
            if(iDirect instanceof DirectItem){
                Realm realmO = Realm.getDefaultInstance();
                realmO.executeTransaction(realm -> {
                    RealmResults<UserMember> userMembers = realm.where(UserMember.class).equalTo("userId", ((DirectItem) iDirect).userId).findAll();
                    if(((DirectItem) iDirect).inTeam != (userMembers.size()!=0)){
                        ((DirectItem) iDirect).inTeam = userMembers.size()!=0;
                        notifyItemChanged(mAdapterData.indexOf(iDirect));
                    }
                });
                realmO.close();
            }
        }
    }
}
