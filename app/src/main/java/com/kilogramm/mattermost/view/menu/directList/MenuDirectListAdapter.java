package com.kilogramm.mattermost.view.menu.directList;

import android.content.Context;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.ui.CheckableLinearLayout;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 18.08.2016.
 */
public class MenuDirectListAdapter extends RealmRecyclerViewAdapter<Channel,MenuDirectListHolder> {

    private static final String TAG = "MenuDirectListAdapter";

    private Context context;
    private RecyclerView mRecyclerView;
    private MenuDirectListFragment.OnDirectItemClickListener directItemClickListener;
    private MenuDirectListFragment.OnSelectedItemChangeListener selectedItemChangeListener;
    private int selecteditem = -1;
    private RealmResults<UserStatus> userStatuses;

    public MenuDirectListAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Channel> data,
                                 RecyclerView mRecyclerView, MenuDirectListFragment.OnDirectItemClickListener listener,
                                 RealmResults<UserStatus> userStatuses) {
        super(context, data, true);
        this.context = context;
        this.mRecyclerView = mRecyclerView;
        this.userStatuses = userStatuses;
        this.userStatuses.addChangeListener(element -> notifyDataSetChanged());
        this.directItemClickListener = listener;
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
    public MenuDirectListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MenuDirectListHolder holder = MenuDirectListHolder.create(inflater, parent);
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
    public void onBindViewHolder(MenuDirectListHolder holder, int position) {
        Channel channel = getData().get(position);
        UserStatus userStatus = null;

        RealmQuery<UserStatus> byId = userStatuses.where().equalTo("id", channel.getUser().getId());
        if (byId.count() != 0) {
            userStatus = byId.findFirst();
        }

        holder.getmBinding().getRoot()
                .setOnClickListener(v -> {
                    Log.d(TAG, "onClickItem() holder");
                    if(directItemClickListener!=null){
                        directItemClickListener.onDirectClick(channel.getId(), channel.getType(), channel.getUser().getUsername());
                        ((CheckableLinearLayout) holder.getmBinding().getRoot()).setChecked(true);
                        setSelecteditem(holder.getAdapterPosition());
                        onChangeSelected();
                    }
                });
        if (holder.getAdapterPosition() == selecteditem) {
            ((CheckableLinearLayout) holder.getmBinding().getRoot()).setChecked(true);
        } else {
            ((CheckableLinearLayout) holder.getmBinding().getRoot()).setChecked(false);
        }

        holder.bindTo(channel, context, userStatus);
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
        if (selectedItemChangeListener != null) {
            selectedItemChangeListener.onChangeSelected(selecteditem);
        }
    }

    public void setSelectedItemChangeListener(MenuDirectListFragment.OnSelectedItemChangeListener selectedItemChangeListener) {
        this.selectedItemChangeListener = selectedItemChangeListener;
    }

}
