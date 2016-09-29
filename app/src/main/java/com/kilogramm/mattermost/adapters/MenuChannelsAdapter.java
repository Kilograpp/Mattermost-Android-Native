package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;


import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemChannelBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.viewmodel.menu.ItemChannelViewModel;


import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by Evgeny on 03.08.2016.
 */
public class MenuChannelsAdapter extends RealmBaseAdapter<Channel> implements ListAdapter{

    private static final String TAG = "MenuChannelsAdapter";

    private Context context;

    public MenuChannelsAdapter(@Nullable OrderedRealmCollection<Channel> data, Context context) {
        super(context, data);
        this.context = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder viewHolder;
        if (convertView == null) {
            ItemChannelBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.item_channel,
                    parent,
                    false);
            convertView = binding.getRoot();
            viewHolder = new MyViewHolder(binding);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MyViewHolder) convertView.getTag();
        }
        Channel item = adapterData.get(position);
        if(viewHolder.binding.getViewModel() == null){
            viewHolder.binding.setViewModel(new ItemChannelViewModel(context, item));
        } else {
            viewHolder.binding.getViewModel().setChannel(item);
        }
        return convertView;
    }

    class MyViewHolder{

        final ItemChannelBinding binding;

        public MyViewHolder(ItemChannelBinding binding) {
            this.binding  = binding;
        }
    }
}
