package com.kilogramm.mattermost.view.authorization;

import android.database.Cursor;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.database.repository.TeamsRepository;
import com.kilogramm.mattermost.databinding.ItemTeamBinding;
import com.kilogramm.mattermost.utils.ColorGenerator;

/**
 * Created by Evgeny on 09.02.2017.
 */

public class TeamViewHolder extends RecyclerView.ViewHolder {

    private ItemTeamBinding mBinding;

    private TeamViewHolder(ItemTeamBinding binding) {
        super(binding.getRoot());
        this.mBinding = binding;
    }

    public static TeamViewHolder create(LayoutInflater inflater, ViewGroup parent) {
        ItemTeamBinding binding = ItemTeamBinding.inflate(inflater, parent, false);
        return new TeamViewHolder(binding);
    }

    public void bindTo(Cursor mCursor) {
        if (mCursor.moveToPosition(getAdapterPosition())) {
            String displayName = mCursor.getString(mCursor.getColumnIndex(TeamsRepository.FIELD_NAME));
            if (!TextUtils.isEmpty(displayName)) {
                mBinding.timeIcon.setText(String.valueOf(displayName.charAt(0)));
                mBinding.timeIcon.getBackground()
                        .setColorFilter(ColorGenerator.MATERIAL.getRandomColor(), PorterDuff.Mode.MULTIPLY);
                mBinding.timeName.setText(displayName);
            }
        }
    }
}
