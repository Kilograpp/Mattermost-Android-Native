package com.kilogramm.mattermost.adapters.command;

import android.app.Service;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.model.entity.CommandObject;

import java.util.List;

/**
 * Created by Evgeny on 12.12.2016.
 */

public class CommandAdapter extends RecyclerView.Adapter<CommandHolder> {


    private final LayoutInflater inflater;
    private List<CommandObject> data;
    private CommandClickListener listener;

    public CommandAdapter(Context context, List<CommandObject> data, CommandClickListener clickHandler) {
        this.inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        this.data = data;
        this.listener = clickHandler;
    }

    @Override
    public CommandHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return CommandHolder.create(inflater, parent);
    }

    @Override
    public void onBindViewHolder(CommandHolder holder, int position) {
        holder.bindTo(data.get(position), listener);
        if (position == getItemCount() - 1)
            holder.mBinding.viewLine.setVisibility(View.INVISIBLE);
        else
            holder.mBinding.viewLine.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateDate(List<CommandObject> commandList) {
        data = commandList;
        notifyDataSetChanged();
    }
}
