package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemAddMembersBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.squareup.picasso.Picasso;

import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmViewHolder;

/**
 * Created by ngers on 01.11.16.
 */

public class AddMembersAdapter extends RealmRecyclerViewAdapter<User,AddMembersAdapter.MyViewHolder> {

    OnItemClickListener onItemClickListener;

    public AddMembersAdapter(@NonNull Context context, OnItemClickListener onItemClickListener) {
        super(context, null, true);
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemAddMembersBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_add_members,
                parent,
                false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = getItem(position);
        if (user.getId() != null) {
            holder.binding.textViewMemberName.setText(user.getUsername());

            Picasso.with(holder.binding.getRoot().getContext())
                    .load(getImageUrl(user))
                    .resize(60, 60)
                    .error(holder.binding.getRoot().getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .placeholder(holder.binding.getRoot().getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .into(holder.binding.circleImageViewMemberIcon);

            holder.binding.getRoot().setOnClickListener(view ->
                    onItemClickListener.onItemClick(user.getId()));
        }
    }

    public String getImageUrl(User user) {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/users/"
                + user.getId()
                + "/image";
    }

    public static class MyViewHolder extends RealmViewHolder {
        ItemAddMembersBinding binding;
        private MyViewHolder(ItemAddMembersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(String id);
    }
}
