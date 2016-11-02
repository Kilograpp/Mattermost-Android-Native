package com.kilogramm.mattermost.view.channel;

import android.content.Context;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.AllMembersItemBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.utils.ListRecyclerViewAD;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.realm.RealmViewHolder;

/**
 * Created by ngers on 01.11.16.
 */

public class AllMembersAdapter extends ListRecyclerViewAD<User,AllMembersAdapter.MyViewHolder> {

    OnItemClickListener onItemClickListener;
    int countItem = -1;

    public AllMembersAdapter(Context context ,OnItemClickListener onItemClickListener, List<User> data, int countItem) {
        super(context, data);
        this.onItemClickListener = onItemClickListener;
        this.countItem = countItem;
    }

    public AllMembersAdapter(Context context ,OnItemClickListener onItemClickListener, List<User> data) {
        super(context, data);
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AllMembersItemBinding binding = createBinding(parent, R.layout.all_members_item);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = getItem(position);
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

    @Override
    public int getItemCount() {
        if (countItem != -1)
            return countItem;
        return getrData().size();
    }

    public static class MyViewHolder extends RealmViewHolder {
        AllMembersItemBinding binding;

        private MyViewHolder(AllMembersItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String id);
    }
}
