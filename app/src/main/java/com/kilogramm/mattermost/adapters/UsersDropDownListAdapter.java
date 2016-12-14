package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemUserDropDownListBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.squareup.picasso.Picasso;

import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by ngers on 16.09.16.
 */

public class UsersDropDownListAdapter extends RealmRecyclerViewAdapter<User, UsersDropDownListAdapter.ViewHolder> {

    private OnItemClickListener onItemClickListener;
    public UsersDropDownListAdapter(@NonNull Context context,
                                    OnItemClickListener onItemClickListener) {
        super(context,null,true);
        this.onItemClickListener = onItemClickListener;
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
        User user = getData().get(position);
        if (user.getId() != null) {
            holder.mBinding.circleImageViewAvatar.setTag(user);
            holder.mBinding.textViewUserNikname.setText("@" + user.getUsername());
            holder.mBinding.textViewUserName.setText(String.format(" %s %s",
                    user.getFirstName() != null ? user.getFirstName() : "",
                    user.getLastName() != null ? user.getLastName() : ""));

            Picasso.with(holder.mBinding.getRoot().getContext())
                    .load(getImageUrl(user))
                    .resize(60, 60)
                    .error(holder.mBinding.getRoot().getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .placeholder(holder.mBinding.getRoot().getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .into(holder.mBinding.circleImageViewAvatar);

            holder.mBinding.getRoot().setOnClickListener(view ->
                    onItemClickListener.onItemClick(user.getUsername()));
            if(position == getItemCount() - 1)
                holder.mBinding.viewLine.setVisibility(View.INVISIBLE);
            else
                holder.mBinding.viewLine.setVisibility(View.VISIBLE);
        }
    }

    public String getImageUrl(User user) {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/users/"
                + user.getId()
                + "/image";
    }


    public interface OnItemClickListener{
        void onItemClick(String name);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemUserDropDownListBinding mBinding;

        public ViewHolder(ItemUserDropDownListBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }
    }
}
