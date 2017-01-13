package com.kilogramm.mattermost.rxtest.autocomplete_list.holder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.databinding.AutocompleteHeaderLayoutBinding;
import com.kilogramm.mattermost.rxtest.autocomplete_list.model.AutoCompleteHeader;

/**
 * Created by Evgeny on 11.01.2017.
 */

public class AutoCompleteHeaderHolder extends RecyclerView.ViewHolder {

    private AutocompleteHeaderLayoutBinding mBinding;

    private AutoCompleteHeaderHolder(AutocompleteHeaderLayoutBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public static AutoCompleteHeaderHolder create(LayoutInflater inflater, ViewGroup parent) {
        AutocompleteHeaderLayoutBinding binding = AutocompleteHeaderLayoutBinding.inflate(inflater, parent, false);
        return new AutoCompleteHeaderHolder(binding);
    }

    public void bindTo(AutoCompleteHeader header){
        mBinding.headerTitle.setText(header.getHeader());
    }

}
