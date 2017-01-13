package com.kilogramm.mattermost.rxtest.autocomplete_list.holder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.AutocompleteItemLayoutBinding;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.rxtest.autocomplete_list.model.AutoCompleteItem;
import com.squareup.picasso.Picasso;

/**
 * Created by Evgeny on 11.01.2017.
 */

public class AutoCompleteItemHolder extends RecyclerView.ViewHolder {

    private AutocompleteItemLayoutBinding mBinding;

    private AutoCompleteItemHolder(AutocompleteItemLayoutBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public static AutoCompleteItemHolder create(LayoutInflater inflater, ViewGroup parent) {
        AutocompleteItemLayoutBinding binding = AutocompleteItemLayoutBinding.inflate(inflater, parent, false);
        return new AutoCompleteItemHolder(binding);
    }


    public void bindTo(AutoCompleteItem item, OnItemClickListener onItemClickListener){
        User user = item.getUser();
        if (user.getId() != null) {
            mBinding.circleImageViewAvatar.setTag(user);
            mBinding.textViewUserNikname.setText("@" + user.getUsername());
            mBinding.textViewUserName.setText(String.format(" %s %s",
                    user.getFirstName() != null ? user.getFirstName() : "",
                    user.getLastName() != null ? user.getLastName() : ""));

            Picasso.with(mBinding.getRoot().getContext())
                    .load(getImageUrl(user))
                    .resize(60, 60)
                    .error(mBinding.getRoot().getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .placeholder(mBinding.getRoot().getContext()
                            .getResources()
                            .getDrawable(R.drawable.ic_person_grey_24dp))
                    .into(mBinding.circleImageViewAvatar);

            mBinding.getRoot().setOnClickListener(view ->
                    onItemClickListener.onItemClick(user.getUsername()));
        }
    }

    public String getImageUrl(User user) {
        return "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/users/"
                + user.getId()
                + "/image";
    }

    public void setViewLineVisibility(int visibility){
        mBinding.viewLine.setVisibility(visibility);
    }

    public interface OnItemClickListener{
        void onItemClick(String name);
    }
}
