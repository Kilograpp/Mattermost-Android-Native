package com.kilogramm.mattermost.view.menu.channelList;

import android.content.Context;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.ui.CheckableLinearLayout;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class AdapterMenuChannelList extends RealmRecyclerViewAdapter<Channel, MenuChannelListHolder> {

    private static final String TAG = "AdapterMenuDirectList";

    private Context context;
    private RecyclerView mRecyclerView;
    private MenuChannelListFragment.OnChannelItemClickListener channelItemClickListener;
    private MenuChannelListFragment.OnSelectedItemChangeListener selectedItemChangeListener;
    private int selecteditem = -1;

    public AdapterMenuChannelList(@NonNull Context context, @Nullable OrderedRealmCollection<Channel> data,
                                  RecyclerView mRecyclerView, MenuChannelListFragment.OnChannelItemClickListener listener) {
        super(context, data, true);
        this.context = context;
        this.mRecyclerView = mRecyclerView;
        this.channelItemClickListener = listener;
    }

    static Object DATA_INVALIDATION = new Object();

    private boolean isForDataBinding(List<Object> payloads) {
        if (payloads == null || payloads.size() == 0) {
            return false;
        }
        for (Object obj : payloads) {
            if (obj != DATA_INVALIDATION) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MenuChannelListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MenuChannelListHolder holder = MenuChannelListHolder.create(inflater, parent);
        holder.getmBinding().addOnRebindCallback(new OnRebindCallback() {
            public boolean onPreBind(ViewDataBinding binding) {
                return mRecyclerView != null && mRecyclerView.isComputingLayout();
            }
            public void onCanceled(ViewDataBinding binding) {
                if (mRecyclerView == null || mRecyclerView.isComputingLayout()) {
                    return;
                }
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position, DATA_INVALIDATION);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(MenuChannelListHolder holder, int position) {
        Channel channel = getData().get(position);
        holder.getmBinding().getRoot()
                .setOnClickListener(v -> {
                    Log.d(TAG, "onClickItem() holder");
                    if(channelItemClickListener!=null){
                        channelItemClickListener.onChannelClick(channel.getId(), channel.getType(), channel.getDisplayName());
                        ((CheckableLinearLayout) holder.getmBinding().getRoot()).setChecked(true);
                        setSelecteditem(holder.getAdapterPosition());
                        onChangeSelected();
                    }
                });
        if(holder.getAdapterPosition() == selecteditem){
            ((CheckableLinearLayout) holder.getmBinding().getRoot()).setChecked(true);
        } else {
            ((CheckableLinearLayout) holder.getmBinding().getRoot()).setChecked(false);
        }
        holder.bindTo(channel, context);
    }

    @Override
    public void onBindViewHolder(MenuChannelListHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if(isForDataBinding(payloads)){
            holder.getmBinding().executePendingBindings();
        } else {
            onBindViewHolder(holder, position);
        }
    }

    public int getSelecteditem() {
        return selecteditem;
    }

    public void setSelecteditem(int selecteditem) {
        this.selecteditem = selecteditem;
        onChangeSelected();
        notifyDataSetChanged();
    }

    private void onChangeSelected() {
        if(selectedItemChangeListener!=null){
            selectedItemChangeListener.onChangeSelected(selecteditem);
        }
    }

    public void setSelectedItemChangeListener(MenuChannelListFragment.OnSelectedItemChangeListener selectedItemChangeListener) {
        this.selectedItemChangeListener = selectedItemChangeListener;
    }
}
