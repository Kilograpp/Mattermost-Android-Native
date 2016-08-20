package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDirectionProfileChannelBinding;
import com.kilogramm.mattermost.model.entity.Channel;
import com.kilogramm.mattermost.viewmodel.menu.ItemDiretionProfileViewModel;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by Evgeny on 17.08.2016.
 */
public class MenuDirectionProfileAdapter extends RealmBaseAdapter<Channel> implements ListAdapter {

    private static final String TAG = "MenuDirectProfAdapter";

    private Context context;

    public MenuDirectionProfileAdapter(@Nullable OrderedRealmCollection<Channel> data, Context context) {
        super(context, data);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder viewHolder;
        if (convertView == null) {
            ItemDirectionProfileChannelBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.item_direction_profile_channel,
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
            viewHolder.binding.setViewModel(new ItemDiretionProfileViewModel(context, item));
        } else {
            viewHolder.binding.getViewModel().setChannel(item);
        }
        return convertView;
    }

    class MyViewHolder {

        final ItemDirectionProfileChannelBinding binding;

        public MyViewHolder(ItemDirectionProfileChannelBinding binding) {
            this.binding  = binding;
        }

    }
}