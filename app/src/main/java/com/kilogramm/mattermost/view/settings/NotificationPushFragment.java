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

    private FragmentNotificationMobilePushBinding mBinding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification_mobile_push,
                container, false);

        initSettingPush();
        initSettingPushStatus();
        initOnClick();

        return mBinding.getRoot();
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

    private void initOnClick() {
        mBinding.cardViewAll.setOnClickListener(this);
        mBinding.cardViewMention.setOnClickListener(this);
        mBinding.cardViewNone.setOnClickListener(this);

        mBinding.cardViewOnline.setOnClickListener(this);
        mBinding.cardViewAway.setOnClickListener(this);
        mBinding.cardViewOffline.setOnClickListener(this);
    }

    private void initSettingPush() {
        notifPushSetting();
        switch (getPresenter().getPushSetting()) {
            case "all":
                mBinding.imageViewSelectAll.setVisibility(View.VISIBLE);
                break;
            case "mention":
                mBinding.imageViewSelectMentionsDirect.setVisibility(View.VISIBLE);
                break;
            case "none":
                mBinding.imageViewSelectNever.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void notifPushSetting() {
        mBinding.imageViewSelectAll.setVisibility(View.INVISIBLE);
        mBinding.imageViewSelectMentionsDirect.setVisibility(View.INVISIBLE);
        mBinding.imageViewSelectNever.setVisibility(View.INVISIBLE);
    }

    private void initSettingPushStatus() {
        notifyPushStatusSetting();
        switch (getPresenter().getPushStatusSetting() == null ? "" : getPresenter().getPushStatusSetting()) {
            case "online":
                mBinding.imageViewSelectAllTrigger.setVisibility(View.VISIBLE);
                break;
            case "away":
                mBinding.imageViewSelectAwayOff.setVisibility(View.VISIBLE);
                break;
            case "offline":
                mBinding.imageViewSelectOff.setVisibility(View.VISIBLE);
                break;
            case "":
                break;
        }
    }

    private void notifyPushStatusSetting() {
        mBinding.imageViewSelectAllTrigger.setVisibility(View.INVISIBLE);
        mBinding.imageViewSelectAwayOff.setVisibility(View.INVISIBLE);
        mBinding.imageViewSelectOff.setVisibility(View.INVISIBLE);
    }

}

