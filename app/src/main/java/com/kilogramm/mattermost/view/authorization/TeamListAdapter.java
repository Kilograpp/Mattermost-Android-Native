package com.kilogramm.mattermost.view.authorization;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemTeamBinding;
import com.kilogramm.mattermost.model.entity.Team;

import java.util.Random;

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
        if (team.getDisplayName().length() != 0) {
            holder.binding.timeIcon.setText(String.valueOf(team.getDisplayName().charAt(0)));
            holder.binding.timeIcon.getBackground()
                    .setColorFilter(getRandomColor(), PorterDuff.Mode.MULTIPLY);
            holder.binding.timeName.setText(team.getDisplayName());
        }
        holder.binding.getRoot().setOnClickListener(view ->
                onItemClickListener.onItemClick(team.getId()));
    }

    int getRandomColor() {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        return color;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemTeamBinding binding;

        public ViewHolder(ItemTeamBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String id);
    }

}
