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
public class NotificationPushFragment extends BaseFragment implements View.OnClickListener {

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

        initSettingPush();
        initSettingPushStatus();
        initOnClick();

        return binding.getRoot();
    }

    private void initOnClick() {
        binding.all.setOnClickListener(this);
        binding.mention.setOnClickListener(this);
        binding.none.setOnClickListener(this);

        binding.online.setOnClickListener(this);
        binding.away.setOnClickListener(this);
        binding.offline.setOnClickListener(this);
    }

    private void initSettingPush() {
        notifPushSetting();
        switch (getPresenter().getPushSetting()) {
            case "all":
                binding.selectAll.setVisibility(View.VISIBLE);
                break;
            case "mention":
                binding.selectMentionsDirect.setVisibility(View.VISIBLE);
                break;
            case "none":
                binding.selectNever.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void notifPushSetting(){
        binding.selectAll.setVisibility(View.INVISIBLE);
        binding.selectMentionsDirect.setVisibility(View.INVISIBLE);
        binding.selectNever.setVisibility(View.INVISIBLE);
    }

    private void initSettingPushStatus() {
        notifyPushStatusSetting();
        switch (getPresenter().getPushStatusSetting()) {
            case "online":
                binding.selectAllTriger.setVisibility(View.VISIBLE);
                break;
            case "away":
                binding.selectAwayOff.setVisibility(View.VISIBLE);
                break;
            case "offline":
                binding.selectOff.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void notifyPushStatusSetting(){
        binding.selectAllTriger.setVisibility(View.INVISIBLE);
        binding.selectAwayOff.setVisibility(View.INVISIBLE);
        binding.selectOff.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onClick(View view) {
        String[] tag = view.getTag().toString().split(" ");
        switch (tag[1]) {
            case "push":
                getPresenter().setPushSetting(tag[0]);
                initSettingPush();
                break;
            case "push_status":
                getPresenter().setPushStatusSetting(tag[0]);
                initSettingPushStatus();
                break;
        }
    }

    @Override
    public NotificationPresenter getPresenter() {
        return ((NotificationActivity) getActivity()).getPresenter();
    }
}

