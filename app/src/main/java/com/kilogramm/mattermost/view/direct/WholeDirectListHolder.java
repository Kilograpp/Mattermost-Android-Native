package com.kilogramm.mattermost.view.direct;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ItemDirectListBinding;
import com.kilogramm.mattermost.model.entity.user.User;
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

    public void bindTo(User user, boolean isShow, Boolean changed) {

        directBinding.directProfileName.setText(user.getUsername());


        if (user.getFirstName().length() == 0 && user.getLastName().length() == 0)
            directBinding.emailProfile.setVisibility(View.GONE);
        else
            directBinding.emailProfile.setText(String.format("%s %s",
                    user.getFirstName(),
                    user.getLastName()));

        if (changed != null) {
            directBinding.selectDirect.setChecked(changed);
        } else
            directBinding.selectDirect.setChecked(isShow);

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
