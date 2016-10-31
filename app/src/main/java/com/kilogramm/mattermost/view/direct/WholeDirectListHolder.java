package com.kilogramm.mattermost.view.direct;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDirectListBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;
import com.squareup.picasso.Picasso;

import io.realm.RealmViewHolder;

/**
 * Created by Evgeny on 31.10.2016.
 */

public class WholeDirectListHolder extends RealmViewHolder {

    private ItemDirectListBinding directBinding;

    private WholeDirectListHolder(ItemDirectListBinding binding) {
        super(binding.getRoot());
        directBinding = binding;
    }

    public static WholeDirectListHolder create(LayoutInflater inflater, ViewGroup parent) {
        return new WholeDirectListHolder(DataBindingUtil.inflate(inflater, R.layout.item_direct_list, parent, false));
    }

    public void bindTo(Context context, User user, UserStatus userStatus) {

        directBinding.directProfileName.setText(user.getUsername());

        StringBuilder stringBuilder = new StringBuilder("(" + user.getEmail() + ")");
        directBinding.emailProfile.setText(stringBuilder);

        if (userStatus != null) {
            directBinding.status.setImageDrawable(drawStatusIcon(context, userStatus));
        } else {
            directBinding.status.setImageDrawable(context.getResources().getDrawable(R.drawable.status_offline_drawable));
        }

        Picasso.with(directBinding.avatarDirect.getContext())
                .load(getImageUrl(user.getId()))
                .resize(60, 60)
                .error(directBinding.avatarDirect.getContext()
                        .getResources()
                        .getDrawable(R.drawable.ic_person_grey_24dp))
                .placeholder(directBinding.avatarDirect.getContext()
                        .getResources()
                        .getDrawable(R.drawable.ic_person_grey_24dp))
                .into(directBinding.avatarDirect);

        directBinding.executePendingBindings();
    }

    public ItemDirectListBinding getmBinding() {
        return directBinding;
    }

    public static Drawable drawStatusIcon(Context context, UserStatus status) {
        switch (status.getStatus()){
            case UserStatus.ONLINE:
                return context.getResources().getDrawable(R.drawable.status_online_drawable);
            case UserStatus.OFFLINE:
                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
            case UserStatus.AWAY:
                return context.getResources().getDrawable(R.drawable.status_away_drawable);
            case UserStatus.REFRESH:
                return context.getResources().getDrawable(R.drawable.status_refresh_drawable);
            default:
                return context.getResources().getDrawable(R.drawable.status_offline_drawable);
        }
    }

    public static String getImageUrl(String userId) {
        if (userId != null) {
            return "https://"
                    + MattermostPreference.getInstance().getBaseUrl()
                    + "/api/v3/users/"
                    + userId
                    + "/image";
        } else {
            return "";
        }
    }
}
