package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.TopMembersItemBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.userstatus.StatusByIdSpecification;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatusRepository;
import com.kilogramm.mattermost.utils.ListRecyclerViewAD;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.realm.RealmViewHolder;

/**
 * Created by ngers on 01.11.16.
 */

public class TopMembersAdapter extends ListRecyclerViewAD<User, TopMembersAdapter.MyViewHolder> {

    OnItemClickListener onItemClickListener;

    public TopMembersAdapter(Context context, OnItemClickListener onItemClickListener, List<User> data) {
        super(context, data);
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TopMembersItemBinding binding = createBinding(parent, R.layout.top_members_item);
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

    @Override
    public int getItemCount() {
        return 5;
    }

    public String getStatus(String id) {
        try {
            return UserStatusRepository.query(new StatusByIdSpecification(id)).first().getStatus();
        }catch (IndexOutOfBoundsException e){
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
