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

    private FragmentEmailNotificationBinding mBinding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_email_notification,
                container, false);

        initSetting();
        initOnClick();
        mBinding.textViewDescription.setText(String.format("%s %s %s",
                getText(R.string.email_description_start),
                MattermostPreference.getInstance().getSiteName(),
                getText(R.string.email_description_end)));
        return mBinding.getRoot();
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

    private void initOnClick() {
        mBinding.cardViewImmediately.setOnClickListener(this);
        mBinding.cardViewNever.setOnClickListener(this);
    }

    private void initSetting() {
        notifyEmailSetting();
        if (getPresenter().getEmailSetting().equals(getString(R.string.email_immediately))) {
            mBinding.imageViewSelectImmediately.setVisibility(View.VISIBLE);
        } else {
            mBinding.imageViewSelectNever.setVisibility(View.VISIBLE);
        }
    }

    private void notifyEmailSetting() {
        mBinding.imageViewSelectImmediately.setVisibility(View.INVISIBLE);
        mBinding.imageViewSelectNever.setVisibility(View.INVISIBLE);
    }

}

