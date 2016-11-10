package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.TopMembersItemBinding;
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
        TopMembersItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.top_members_item,
                parent,
                false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = getItem(position);
        if (user.getId() != null) {
            holder.binding.memberName.setText(user.getUsername());
            if (user.getRoles().equals("admin")) {
                holder.binding.status.setText(user.getRoles());
                holder.binding.status.setTextColor(holder.binding.getRoot().getContext()
                        .getResources().getColor(R.color.title_grey));
            } else if (getStatus(user.getId()).equals(UserStatus.ONLINE)) {
                holder.binding.status.setText(UserStatus.ONLINE);
            } else {
                holder.binding.status.setText("");
            }

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

    @Override
    public int getItemCount() {
        if (isToop)
            return getData().size() > 5 ? 5 : getData().size();
        else
            return getData().size();
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
        TopMembersItemBinding binding;

        private MyViewHolder(TopMembersItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String id);
    }
}
