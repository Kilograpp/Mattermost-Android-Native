package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemTopMembersBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.userstatus.StatusByIdSpecification;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.squareup.picasso.Picasso;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmViewHolder;

/**
 * Created by ngers on 01.11.16.
 */

public class AllMembersAdapter extends RealmRecyclerViewAdapter<User, AllMembersAdapter.MyViewHolder> {

    OnItemClickListener onItemClickListener;
    boolean isToop;

    public AllMembersAdapter(Context context, OnItemClickListener onItemClickListener,
                             OrderedRealmCollection<User> realmResult) {
        super(context, realmResult, true);
        this.onItemClickListener = onItemClickListener;
    }


    public AllMembersAdapter(Context context, OnItemClickListener onItemClickListener,
                             OrderedRealmCollection<User> realmResult, boolean isTop) {
        super(context, realmResult, true);
        this.onItemClickListener = onItemClickListener;
        this.isToop = isTop;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemTopMembersBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_top_members,
                parent,
                false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = getItem(position);
        if (user.getId() != null) {
            holder.binding.textViewMemberName.setText(user.getUsername());
            if (user.getRoles().equals("admin")) {
                holder.binding.textViewStatus.setText(user.getRoles());
                holder.binding.textViewStatus.setTextColor(holder.binding.getRoot().getContext()
                        .getResources().getColor(R.color.title_grey));
            } else if (getStatus(user.getId()).equals(UserStatus.ONLINE)) {
                holder.binding.textViewStatus.setText(UserStatus.ONLINE);
            } else {
                holder.binding.textViewStatus.setText("");
            }

            if (position == getItemCount() - 1) {
                holder.binding.viewLine.setVisibility(View.INVISIBLE);
            } else holder.binding.viewLine.setVisibility(View.VISIBLE);

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

    @Override
    public int getItemCount() {
        if (isToop)
            return getData().size() > 5 ? 5 : getData().size();
        else
            return getData().size();
    }

    public String getImageUrl(User user) {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/users/"
                + user.getId()
                + "/image";
    }

    public String getStatus(String id) {
        try {
            return UserStatusRepository.query(new StatusByIdSpecification(id)).first().getStatus();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static class MyViewHolder extends RealmViewHolder {
        ItemTopMembersBinding binding;

        private MyViewHolder(ItemTopMembersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String id);
    }
}
