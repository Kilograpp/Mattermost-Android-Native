package com.kilogramm.mattermost.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemUserDropDownListBinding;
import com.kilogramm.mattermost.model.entity.User;
import com.squareup.picasso.Picasso;

import io.realm.RealmResults;

/**
 * Created by ngers on 16.09.16.
 */
public class UsersDropDownListAdapter extends RecyclerView.Adapter<UsersDropDownListAdapter.ViewHolder> {
    private RealmResults<User> users;

    public UsersDropDownListAdapter(RealmResults<User> users) {
        this.users = users;
    }

    public void setUsers(RealmResults<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public UsersDropDownListAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemUserDropDownListBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_user_drop_down_list,
                parent,
                false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = users.get(position);
        if (user.getId() != null) {
            holder.binding.avatar.setTag(user);
            holder.binding.userNikname.setText(user.getUsername());
            if (user.getFirstName() != null || user.getLastName() != null)
                holder.binding.userName.setText(String.format(" - %s%s",
                        user.getFirstName() != null ? user.getFirstName() : "",
                        user.getLastName() != null ? user.getLastName() : ""
                ));
            Picasso.with(holder.binding.getRoot().getContext())
                    .load(getImageUrl(user))
                    .resize(60, 60)
                    .error(holder.binding.getRoot().getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .placeholder(holder.binding.getRoot().getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .into(holder.binding.avatar);
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
        return users!=null? users.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemUserDropDownListBinding binding;

        public ViewHolder(ItemUserDropDownListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}
