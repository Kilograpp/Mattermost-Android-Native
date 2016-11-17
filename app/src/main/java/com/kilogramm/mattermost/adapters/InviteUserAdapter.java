package com.kilogramm.mattermost.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.InviteMemberItemBinding;
import com.kilogramm.mattermost.model.fromnet.InviteObject;

/**
 * Created by kepar on 17.11.16.
 */

public class InviteUserAdapter extends HeaderFooterRecyclerArrayAdapter<InviteUserAdapter.ViewHolder, InviteObject> {

    Context context;

    public InviteUserAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateGenericViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.create(LayoutInflater.from(context), parent);
    }

    @Override
    public void onBindGenericViewHolder(ViewHolder holder, int position) {
        holder.binding.memberNumberLabel.setText(context.getString(R.string.member) + (position + 1));
        if(position == 0) {
            holder.binding.delete.setVisibility(View.GONE);
        } else {
            holder.binding.delete.setVisibility(View.VISIBLE);
            holder.binding.delete.setOnClickListener(v -> removeItem(position));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private InviteMemberItemBinding binding;

        public ViewHolder(InviteMemberItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public static ViewHolder create(LayoutInflater inflater, ViewGroup parent) {
            InviteMemberItemBinding binding = InviteMemberItemBinding.inflate(inflater, parent, false);
            return new ViewHolder(binding);
        }
    }
}