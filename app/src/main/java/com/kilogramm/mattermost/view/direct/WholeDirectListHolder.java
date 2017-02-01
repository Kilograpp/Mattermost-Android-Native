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

    private ItemDirectListBinding mDirectBinding;

    private WholeDirectListHolder(ItemDirectListBinding binding) {
        super(binding.getRoot());
        mDirectBinding = binding;
    }

    public static WholeDirectListHolder create(LayoutInflater inflater, ViewGroup parent) {
        return new WholeDirectListHolder(DataBindingUtil.inflate(inflater, R.layout.item_direct_list, parent, false));
    }

    public void bindTo(User user, boolean isShow, Boolean changed) {
        mDirectBinding.textViewDirectProfileName.setText(user.getUsername());
        if (user.getFirstName().length() == 0 && user.getLastName().length() == 0)
            mDirectBinding.textViewEmailProfile.setVisibility(View.GONE);
        else {
            mDirectBinding.textViewEmailProfile.setVisibility(View.VISIBLE);
            mDirectBinding.textViewEmailProfile.setText(String.format("%s %s",
                    user.getFirstName(),
                    user.getLastName()));
        }

        if (changed != null) {
            mDirectBinding.checkBoxSelectDirect.setChecked(changed);
        } else
            mDirectBinding.checkBoxSelectDirect.setChecked(isShow);

        Picasso.with(mDirectBinding.circleImageAvatarDirect.getContext())
                .load(getImageUrl(user.getId()))
                .resize(60, 60)
                .error(mDirectBinding.circleImageAvatarDirect.getContext()
                        .getResources()
                        .getDrawable(R.drawable.ic_person_grey_24dp))
                .placeholder(mDirectBinding.circleImageAvatarDirect.getContext()
                        .getResources()
                        .getDrawable(R.drawable.ic_person_grey_24dp))
                .into(mDirectBinding.circleImageAvatarDirect);

        mDirectBinding.executePendingBindings();
    }

    public ItemDirectListBinding getmBinding() {
        return mDirectBinding;
    }

    public static String getImageUrl(String userId) {
        return userId != null
                ? "https://" +
                  MattermostPreference.getInstance().getBaseUrl() +
                  "/api/v3/users/" +
                  userId +
                  "/image"
                : "";
    }
}
