package com.kilogramm.mattermost.adapters.command;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.databinding.ItemCommandBinding;
import com.kilogramm.mattermost.model.entity.CommandObject;

/**
 * Created by Evgeny on 12.12.2016.
 */
public class CommandHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "CommandHolder";
    private ItemCommandBinding mBinding;

    private CommandHolder(ItemCommandBinding binding) {
        super(binding.getRoot());
        this.mBinding = binding;
    }

    public static CommandHolder create(LayoutInflater inflater, ViewGroup parent) {
        ItemCommandBinding binding = ItemCommandBinding
                .inflate(inflater, parent, false);
        return new CommandHolder(binding);
    }

    public void bindTo(CommandObject command, CommandClickListener clickListener){
        mBinding.command.setText(String.format("%s %s", command.getCommand(), command.getHint()));
        mBinding.description.setText(command.getDescription());
        mBinding.getRoot().setOnClickListener(v -> {
            clickListener.onCommandClick(command);
        });
    }

}
