package com.kilogramm.mattermost.view.settings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.FragmentNotificationTriggerBinding;
import com.kilogramm.mattermost.presenter.settings.NotificationPresenter;
import com.kilogramm.mattermost.view.fragments.BaseFragment;

/**
 * Created by Evgeny on 24.08.2016.
 */
public class WordsTriggerMentionsFragment extends BaseFragment {

    private FragmentNotificationTriggerBinding mBinding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification_trigger,
                container, false);
        initTriggers();
        initChangeListener();

        return mBinding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setMentionsKeys();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void setMentionsKeys(){
        getPresenter().setMentionsKeys(getAllMentions());
    }

    @Override
    public NotificationPresenter getPresenter() {
        return ((NotificationActivity) getActivity()).getPresenter();
    }

    public String getAllMentions() {
        String mentionKeys = "";
        if (mBinding.checkBoxSelectUserName.isChecked())
            mentionKeys = mentionKeys + getPresenter().getUserName() + ",";
        if (mBinding.checkBoxSelectUserNameMentioned.isChecked())
            mentionKeys = mentionKeys + getPresenter().getUserNameMentioned() + ",";
        if (mBinding.editTextOtherMention.getText().toString().trim().length() > 0)
            mentionKeys = mentionKeys + mBinding.editTextOtherMention.getText().toString();
        return mentionKeys;
    }

    private void initChangeListener() {
        mBinding.checkBoxSelectFirstName.setOnCheckedChangeListener((compoundButton, b) ->
                getPresenter().setFirstNameTrigger(b));
        mBinding.checkBoxSelectChannelMentioned.setOnCheckedChangeListener((compoundButton, b) ->
                getPresenter().setChannelTrigger(b));

        CompoundButton.OnCheckedChangeListener mention_keysListener = (compoundButton, b) -> setMentionsKeys();
        mBinding.checkBoxSelectUserName.setOnCheckedChangeListener(mention_keysListener);
        mBinding.checkBoxSelectUserNameMentioned.setOnCheckedChangeListener(mention_keysListener);
    }

    private void initTriggers() {
        mBinding.textViewTitleFirstName.setText(mBinding.textViewTitleFirstName.getText()
                + " \"" + getPresenter().getFirstName() + "\"");
        mBinding.checkBoxSelectFirstName.setChecked(getPresenter().isFirstNameTrigger());

        mBinding.textViewTitleUserName.setText(mBinding.textViewTitleUserName.getText()
                + " \"" + getPresenter().getUserName() + "\"");
        mBinding.checkBoxSelectUserName.setChecked(getPresenter().isUserName());

        mBinding.textViewTitleUserNameMentioned.setText(mBinding.textViewTitleUserNameMentioned.getText()
                + " \"" + getPresenter().getUserNameMentioned() + "\"");
        mBinding.checkBoxSelectUserNameMentioned.setChecked(getPresenter().isUserNameMentioned());

        mBinding.checkBoxSelectChannelMentioned.setChecked(getPresenter().isChannelTrigger());

        mBinding.editTextOtherMention.setText(getPresenter().getOtherMentionsKeys());
    }
}

