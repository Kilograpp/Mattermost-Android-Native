package com.kilogramm.mattermost.view.channel;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemAddMembersBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.realm.RealmViewHolder;

/**
 * Created by melkshake on 12.01.17.
 */

public class AddMembersAdapterNotRealm extends RecyclerView.Adapter<AddMembersAdapterNotRealm.AddMembersHolder> {

    private static final String TAG = "AdapterNOTRealm";
    private OnItemClickListener onItemClickListener;
    private List<User> usersNotFromChannel;

    public AddMembersAdapterNotRealm(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public AddMembersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        ItemAddMembersBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_add_members,
                parent,
                false);
        return new AddMembersHolder(binding);
    }

    @Override
    public void onBindViewHolder(AddMembersHolder holder, int position) {
        User user = usersNotFromChannel.get(position);
        Log.d(TAG, "onBindViewHolder: " + user.getId());
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

    @Override
    public int getItemCount() {
        return usersNotFromChannel == null ? 0 : this.usersNotFromChannel.size();
    }

    public void setUsersNotFromChannel(List<User> usersNotFromChannel) {
        this.usersNotFromChannel = usersNotFromChannel;
    }

    public String getImageUrl(User user) {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/users/"
                + user.getId()
                + "/image";
    }

    public void updateData(List<User> usersNotInChannel) {
        setUsersNotFromChannel(usersNotInChannel);
        notifyDataSetChanged();
    }

    public static class AddMembersHolder extends RealmViewHolder {
        ItemAddMembersBinding binding;

        private AddMembersHolder(ItemAddMembersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String id);
    }
}
