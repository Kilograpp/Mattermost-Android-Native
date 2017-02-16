package com.kilogramm.mattermost.rxtest.left_menu.adapters;

import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.UserMember;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
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
import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evgeny on 17.01.2017.
 */

public class AdapterDirectMenuLeft extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "AdapterDirectMenuLeft";
    public static final int POST_TIME = 1500;

    private LayoutInflater mInflater;
    private Context mContext;

    private LinkedHashMap<String, IDirect> mData = new LinkedHashMap<>();
    private List<IDirect> mAdapterData = new ArrayList<>();

    private int mSelectedItem = -1;
    private OnLeftMenuClickListener mItemClickListener;
    private AtomicBoolean isWork = new AtomicBoolean(false);
    private Handler handler;
    private UpdateTask updateTask;

    public AdapterDirectMenuLeft(RealmResults<Channel> data,
                                 Context context,
                                 OnLeftMenuClickListener mItemClickListener) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.mItemClickListener = mItemClickListener;
        handler = new Handler(Looper.myLooper());
        Log.d(TAG, "AdapterDirectMenuLeft() called with: data = [" + data + "], context = [" + context + "], mItemClickListener = [" + mItemClickListener + "]");
        update();
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
                    mItemClickListener.onChannelClick(item.getChannelId(), item.getUsername(),
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

    private List<IDirect> sort(){
        if(mData.size()!=0) {
            Collection<IDirect> directs = mData.values();
            List<IDirect> list = Stream.of(directs)
                    .filter(iDirect -> {
                        if(iDirect.getType() == IDirect.TYPE_ITEM){
                            DirectItem di = ((DirectItem)iDirect);
                            buildIDirectItem(di,di.getChannelId());
                            return true;
                        }return false;
                    })
                    .groupBy(directItem -> ((DirectItem) directItem).isInTeam())
                    .sortBy(entry -> !entry.getKey())
                    .flatMap(entry -> {
                        List<IDirect> value = entry.getValue();
                        Collections.sort(value, (o1, o2) -> {
                            if(TextUtils.isEmpty(((DirectItem) o1).getUsername()) || TextUtils.isEmpty(((DirectItem) o2).getUsername())) return 0;
                            else return ((DirectItem) o1).getUsername().compareTo(((DirectItem) o2).getUsername());
                        }
                        );
                        if (!entry.getKey()) {
                            value.add(0, new DirectHeader("Outside this team"));
                        }
                        return Stream.of(value);
                    }).collect(Collectors.toList());


            return list;
        } else {
            return null;
        }
    }

    public void update(){
        update(true);
    }

    public void update(boolean fullUpdate){
        if(isWork.get() == false) {
            Log.d(TAG, "update() called with: channels = [update(RealmResults<Channel> channels)]");
            isWork.set(true);
            addOrUpdateAsyncV2()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnError(Throwable::printStackTrace)
                    .subscribe(iDirects -> {
                        if (iDirects != null) {
                            if (fullUpdate) {
                                mAdapterData.clear();
                                mAdapterData.addAll(iDirects);
                                notifyDataSetChanged();
                                Log.d(TAG, "notifyDataSetChanged()");
                            } else {
                                DirectItem directItem;
                                for (int i = 0; i < getItemCount(); i++) {
                                    IDirect iDirect = mAdapterData.get(i);
                                    if (iDirect.getType() == IDirect.TYPE_ITEM) {
                                        directItem = (DirectItem) iDirect;
                                        if (directItem.isUpdate()) {
                                            notifyItemChanged(i);
                                            directItem.setUpdate(false);
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "update: iDirect is null");
                        }
                        isWork.set(false);
                    });
        }else{
            Log.d(TAG,"addOrUpdateAsyncV2 overload");
            if(updateTask == null) updateTask = new UpdateTask();
            handler.removeCallbacks(updateTask);
            updateTask.fullUpdate.set(updateTask.fullUpdate.get() || fullUpdate);
            handler.postDelayed(updateTask,POST_TIME);
        }
    }



    public Observable<List<IDirect>> addOrUpdateAsyncV2(){
        return Observable.create(subscriber -> {
            try {

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                List<Channel> channels = realm.copyFromRealm(ChannelRepository.query(new ChannelRepository.ChannelListDirectMenu()));
                realm.commitTransaction();
                realm.close();

                Log.d(TAG,  "mData size = " + mData.size());
                List<String> newIds = Stream.of(channels).map(channel -> channel.getId()).collect(Collectors.toList());
                Set<String> oldIds = new HashSet<>(mData.keySet());
                oldIds.removeAll(newIds);
                for (String oldId : oldIds) {
                    mData.remove(oldId);
                }
                if (channels != null && channels.size() != 0) {
                    for (Channel channel : channels) {
                        if (!mData.containsKey(channel.getId())) {
                            DirectItem item = buildIDirectItem(new DirectItem(),channel.getId());
                            item.setUpdate(false);
                            if(!TextUtils.isEmpty(item.getUsername())) mData.put(channel.getId(), item);
                        }
                    }
                }
                Log.d(TAG, "addOrUpdate: data size = " + mData.size());
                subscriber.onNext(sort());
                subscriber.onCompleted();
            } catch (Exception e){
                subscriber.onError(e);
            }
        });
    }

    private DirectItem buildIDirectItem(DirectItem item, String channelId){

        Realm realmO  = Realm.getDefaultInstance();
        realmO.executeTransaction(realm -> {
            String userId = "";
            RealmResults<Channel> chennels = realm.where(Channel.class).equalTo("id",channelId).findAll();
            if(chennels.size()!=0) {
                item.setChannelId(chennels.first().getId());
                item.setTotalMessageCount(chennels.first().getTotalMsgCount());
                userId = chennels.first().getName().replace(MattermostPreference.getInstance().getMyUserId(), "");
            }else item.setChannelId(channelId);

            userId = userId.replace("__", "");
            RealmResults<User> users = realm.where(User.class).equalTo("id", userId).findAll();
            if(users.size()!=0){
                item.setUserId(users.first().getId());
                item.setUsername(users.first().getUsername());
            } else {
                item.setUserId(null);
                item.setUsername(null);
            }
            RealmResults<UserStatus> statuses = realm.where(UserStatus.class).equalTo("id", userId).findAll();
            if(statuses.size()!=0){
                if(item.getStatus()!=null && !item.getStatus().equals(statuses.first().getStatus())) item.setUpdate(true);
                item.setStatus(statuses.first().getStatus());
            } else {
                item.setStatus(UserStatus.OFFLINE);
            }
            RealmResults<Member> members = realm.where(Member.class).equalTo("channelId",channelId).findAll();
            if(members.size()!=0){

                if(item.getMentionCount()!= members.first().getMentionCount()) item.setUpdate(true);
                item.setMentionCount(members.first().getMentionCount());

                if( item.getMsgCount()!= members.first().getMsgCount()) item.setUpdate(true);
                item.setMsgCount(members.first().getMsgCount());
            } else {
                item.setMentionCount(0);
            }
            RealmResults<UserMember> userMembers = realm.where(UserMember.class).equalTo("userId", userId).findAll();
            item.setInTeam(userMembers.size()!=0);
        });
        realmO.close();
        return item;
    }

    public void setSelectedItem(int selecteditem) {
        this.mSelectedItem = selecteditem;
        notifyDataSetChanged();
    }

    public int getPositionById(String id) {
        for (IDirect iDirect : this.mAdapterData) {
            if(iDirect instanceof DirectItem){
                if(((DirectItem) iDirect).getChannelId().equals(id)){
                    return this.mAdapterData.contains(iDirect)
                            ? this.mAdapterData.indexOf(iDirect)
                            : -1;
                }
            }
        }
        return -1;
    }

    public void invalidateStatus(){
        update(false);
    }

    public void invalidateMember() {
        update(false);
    }

    class UpdateTask implements Runnable{
        AtomicBoolean fullUpdate = new AtomicBoolean(false);
        @Override
        public void run() {
            Log.d(TAG, "UpdateTask start");
            update(fullUpdate.get());
        }
    }
}
