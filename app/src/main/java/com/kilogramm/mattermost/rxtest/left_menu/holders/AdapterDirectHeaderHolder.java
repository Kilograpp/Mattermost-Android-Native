package com.kilogramm.mattermost.rxtest.left_menu.holders;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kilogramm.mattermost.databinding.OutsideLayoutBinding;
import com.kilogramm.mattermost.rxtest.left_menu.direct.DirectHeader;

/**
 * Created by Evgeny on 18.01.2017.
 */
public class AdapterDirectHeaderHolder extends BaseLeftHolder{

    private OutsideLayoutBinding mBinding;

    private AdapterDirectHeaderHolder(OutsideLayoutBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public static AdapterDirectHeaderHolder create(LayoutInflater inflater, ViewGroup parent) {
        OutsideLayoutBinding binding = OutsideLayoutBinding.inflate(inflater, parent, false);
        return new AdapterDirectHeaderHolder(binding);
    }

    public void bindTo(DirectHeader header){
        mBinding.headerTitle.setText(header.getHeaderTitle());
    }
}
