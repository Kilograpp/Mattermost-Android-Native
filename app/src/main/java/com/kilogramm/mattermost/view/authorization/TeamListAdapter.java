package com.kilogramm.mattermost.view.authorization;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemTeamBinding;
import com.kilogramm.mattermost.model.entity.team.Team;
import com.kilogramm.mattermost.utils.ColorGenerator;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by ngers on 16.09.16.
 */

public class TeamListAdapter extends RealmRecyclerViewAdapter<Team, TeamListAdapter.ViewHolder> {

    private OnItemClickListener onItemClickListener;

    public TeamListAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Team> data, OnItemClickListener onItemClickListener) {
        super(context, data, true);
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemTeamBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_team,
                parent,
                false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Team team = getData().get(position);
        holder.bindTo(team);
        holder.binding.getRoot().setOnClickListener(view ->
                onItemClickListener.onItemClick(team.getId()));
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemTeamBinding binding;

        public ViewHolder(ItemTeamBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindTo(Team team){
            if (team.getDisplayName().length() != 0) {
                binding.timeIcon.setText(String.valueOf(team.getDisplayName().charAt(0)));
                binding.timeIcon.getBackground()
                        .setColorFilter(ColorGenerator.MATERIAL.getRandomColor(), PorterDuff.Mode.MULTIPLY);
                binding.timeName.setText(team.getDisplayName());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String id);
    }

}
