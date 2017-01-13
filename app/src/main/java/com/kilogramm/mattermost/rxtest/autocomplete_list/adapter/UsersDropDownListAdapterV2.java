package com.kilogramm.mattermost.rxtest.autocomplete_list.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.fromnet.AutocompleteUsers;
import com.kilogramm.mattermost.rxtest.autocomplete_list.holder.AutoCompleteHeaderHolder;
import com.kilogramm.mattermost.rxtest.autocomplete_list.holder.AutoCompleteItemHolder;
import com.kilogramm.mattermost.rxtest.autocomplete_list.model.AutoCompleteHeader;
import com.kilogramm.mattermost.rxtest.autocomplete_list.model.AutoCompleteItem;
import com.kilogramm.mattermost.rxtest.autocomplete_list.model.AutoCompleteListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evgeny on 11.01.2017.
 */

public class UsersDropDownListAdapterV2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DRDLA2";
    private List<AutoCompleteListItem> mItems;
    private final LayoutInflater mInflater;
    private AutoCompleteItemHolder.OnItemClickListener onItemClickListener;

    public UsersDropDownListAdapterV2(AutocompleteUsers users,
                                      Context context,
                                      AutoCompleteItemHolder.OnItemClickListener onItemClickListener) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mItems = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
        if(users!=null)
        setData(users);
    }

    private void setData(AutocompleteUsers users) {
        clearData();

        if(users.getInChannel()!=null && users.getInChannel().size()!=0) {
            AutoCompleteHeader inChannelHeader = new AutoCompleteHeader();
            inChannelHeader.setHeader("Channels Members");
            mItems.add(inChannelHeader);
        }

        for (User user : users.getInChannel()) {
            AutoCompleteItem inItem = new AutoCompleteItem();
            inItem.setUser(user);
            mItems.add(inItem);
        }

        AutoCompleteHeader specialChannelHeader = new AutoCompleteHeader();
        specialChannelHeader.setHeader("Special Mentions");
        mItems.add(specialChannelHeader);


        AutoCompleteItem all = new AutoCompleteItem();
        all.setUser(new User("materMostAll", "all", "Notifies everyone in the channel, use in Town Square to notify the whole team"));
        mItems.add(all);
        AutoCompleteItem channel = new AutoCompleteItem();
        channel.setUser(new User("materMostChannel", "channel", "Notifies everyone in the channel"));
        mItems.add(channel);

        if(users.getOutChannel()!=null && users.getOutChannel().size()!=0) {
            AutoCompleteHeader outChannelHeader = new AutoCompleteHeader();
            outChannelHeader.setHeader("Not in Channel");
            mItems.add(outChannelHeader);
        }
        for (User user : users.getOutChannel()) {
            AutoCompleteItem outItem = new AutoCompleteItem();
            outItem.setUser(user);
            mItems.add(outItem);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == AutoCompleteListItem.TYPE_HEADER){
            return AutoCompleteHeaderHolder.create(mInflater, parent);
        } else {
            return AutoCompleteItemHolder.create(mInflater, parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int type = getItemViewType(position);
        if (type == AutoCompleteListItem.TYPE_HEADER) {
            AutoCompleteHeader header = (AutoCompleteHeader) mItems.get(position);
            AutoCompleteHeaderHolder holder = (AutoCompleteHeaderHolder) viewHolder;
            holder.bindTo(header);
        } else {
            AutoCompleteItem item = (AutoCompleteItem) mItems.get(position);
            AutoCompleteItemHolder holder = (AutoCompleteItemHolder) viewHolder;
            holder.bindTo(item,onItemClickListener);
            if(position == getItemCount() - 1)
                holder.setViewLineVisibility(View.INVISIBLE);
            else
                holder.setViewLineVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + mItems.size());
        return mItems.size();
    }

    public void updateData(AutocompleteUsers autocompleteUsers){
        Log.d(TAG, "updateData: " + autocompleteUsers);
        if(autocompleteUsers!=null) {
            setData(autocompleteUsers);
            notifyDataSetChanged();
        } else {
            clearData();
        }
    }

    private void clearData() {
        mItems.clear();
        notifyDataSetChanged();
    }
}
