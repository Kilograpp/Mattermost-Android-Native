package com.kilogramm.mattermost.view.menu.pivateList;

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
import com.kilogramm.mattermost.view.menu.channelList.MenuChannelListHolder;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class AdapterMenuPrivateList extends RealmRecyclerViewAdapter<Channel, MenuChannelListHolder> {

    private static final String TAG = "AdapterMenuPrivateList";

    private Context context;
    private RecyclerView mRecyclerView;
    private MenuPrivateListFragment.OnPrivateItemClickListener privateItemClickListener;
    private MenuPrivateListFragment.OnSelectedItemChangeListener selectedItemChangeListener;
    private int selecteditem = -1;

    public AdapterMenuPrivateList(@NonNull Context context, @Nullable OrderedRealmCollection<Channel> data,
                                  RecyclerView mRecyclerView, MenuPrivateListFragment.OnPrivateItemClickListener listener) {
        super(context, data, true);
        this.context = context;
        this.mRecyclerView = mRecyclerView;
        this.privateItemClickListener = listener;
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
                    if(privateItemClickListener !=null){
                        privateItemClickListener.onPrivatelClick(channel.getId(), channel.getDisplayName(), channel.getType());
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

    public void setSelectedItemChangeListener(MenuPrivateListFragment.OnSelectedItemChangeListener selectedItemChangeListener) {
        this.selectedItemChangeListener = selectedItemChangeListener;
    }
}
