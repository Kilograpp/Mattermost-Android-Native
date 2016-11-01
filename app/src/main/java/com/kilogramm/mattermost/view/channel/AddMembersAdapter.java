package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.AddMembersItemBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.squareup.picasso.Picasso;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmViewHolder;

/**
 * Created by ngers on 01.11.16.
 */

public class AddMembersAdapter extends RealmRecyclerViewAdapter<User,AddMembersAdapter.MyViewHolder> {

    OnItemClickListener onItemClickListener;

    public AddMembersAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<User> data,
                             OnItemClickListener onItemClickListener) {
        super(context, data, true);
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AddMembersItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.add_members_item,
                parent,
                false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = getData().get(position);
        if (user.getId() != null) {
            holder.binding.memberName.setText(user.getUsername());

            Picasso.with(holder.binding.getRoot().getContext())
                    .load(getImageUrl(user))
                    .resize(60, 60)
                    .error(holder.binding.getRoot().getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .placeholder(holder.binding.getRoot().getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .into(holder.binding.memberIcon);

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
        AddMembersItemBinding binding;
        private MyViewHolder(AddMembersItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(String id);
    }
}
