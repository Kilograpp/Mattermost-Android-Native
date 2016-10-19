package com.kilogramm.mattermost.view.settings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentNotificationMobilePushBinding;
import com.kilogramm.mattermost.presenter.settings.NotificationPresenter;
import com.kilogramm.mattermost.view.fragments.BaseFragment;

/**
 * Created by Evgeny on 24.08.2016.
 */

public class NotificationMobilePusheFragment extends BaseFragment<NotificationPresenter> {

    private FragmentNotificationMobilePushBinding binding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification_mobile_push,
                container, false);
        View view = binding.getRoot();
//        binding.setUp.setOnClickListener(view1 ->
//                ((NotificationActivity) getActivity()).openNotification());
        return view;
    }





}

