package com.kilogramm.mattermost.view.settings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentEmailNotificationBinding;
import com.kilogramm.mattermost.presenter.settings.NotificationPresenter;
import com.kilogramm.mattermost.view.fragments.BaseFragment;

/**
 * Created by Evgeny on 24.08.2016.
 */
public class NotificationEmailFragment extends BaseFragment implements View.OnClickListener {

    private FragmentEmailNotificationBinding binding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_email_notification,
                container, false);

        initSetting();
        initOnClick();
        binding.description.setText(String.format("%s %s %s",
                getText(R.string.email_description_start),
                MattermostPreference.getInstance().getSiteName(),
                getText(R.string.email_description_end)));
        return binding.getRoot();
    }

    private void initOnClick() {
        binding.immediately.setOnClickListener(this);
        binding.never.setOnClickListener(this);
    }

    private void initSetting() {
        notifyEmailSetting();
        if (getPresenter().getEmailSetting().equals(getString(R.string.email_immediately))) {
            binding.selectImmediately.setVisibility(View.VISIBLE);
        } else {
            binding.selectNever.setVisibility(View.VISIBLE);
        }
    }

    private void notifyEmailSetting() {
        binding.selectImmediately.setVisibility(View.INVISIBLE);
        binding.selectNever.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onClick(View view) {
        getPresenter().setEmailSetting(view.getTag().toString());
        initSetting();
    }

    @Override
    public NotificationPresenter getPresenter() {
        return ((NotificationActivity) getActivity()).getPresenter();
    }
}

