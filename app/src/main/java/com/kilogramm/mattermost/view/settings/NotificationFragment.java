package com.kilogramm.mattermost.view.settings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentNotificationBinding;
import com.kilogramm.mattermost.presenter.settings.NotificationPresenter;
import com.kilogramm.mattermost.view.fragments.BaseFragment;

/**
 * Created by Evgeny on 24.08.2016.
 */
public class NotificationFragment extends BaseFragment<NotificationPresenter> {

    private FragmentNotificationBinding binding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification,
                container, false);
        View view = binding.getRoot();

        binding.mobPush.setOnClickListener(view1 ->
                ((NotificationActivity) getActivity()).openMobilePushNotification());

        binding.wordTrigger.setOnClickListener(view1 ->
                ((NotificationActivity) getActivity()).openWordsTriggerMentions());

        binding.email.setOnClickListener(view1 ->
                ((NotificationActivity) getActivity()).openEmailNotification());

        binding.descriptionWordsTrigger.setText(getPresenter().getMentionsAll());
        binding.descriptionMobPush.setText(getMobPushNotificationDescriptions());
        binding.emailSetting.setText(getPresenter().getEmailSetting().toString());

        return view;
    }

    public String getMobPushNotificationDescriptions() {
        String pushSetting = null;
        switch (getPresenter().getPushSetting()) {
            case "all":
                pushSetting = getString(R.string.mob_push_all);
                break;
            case "mention":
                pushSetting = getString(R.string.mob_push_mentions_direct);
                break;
            case "none":
                pushSetting = getString(R.string.mob_push_never);
                break;
        }
        String pushStatus = null;
        switch (getPresenter().getPushStatusSetting() == null ? "" : getPresenter().getPushStatusSetting()) {
            case "online":
                pushStatus = getString(R.string.mob_push_on_away_off);
                break;
            case "away":
                pushStatus = getString(R.string.mob_push_away_off);
                break;
            case "offline":
                pushStatus = getString(R.string.mob_push_off);
                break;
            case "":
                break;
        }

        return String.format("%s when %s", pushSetting, pushStatus.toLowerCase());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public NotificationPresenter getPresenter() {
        return ((NotificationActivity) getActivity()).getPresenter();
    }
}

