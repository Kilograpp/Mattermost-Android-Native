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

    private FragmentNotificationTriggerBinding binding;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification_trigger,
                container, false);
        initTriggers();
        initChangeListener();

        return binding.getRoot();
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
        if (binding.checkBoxSelectUserName.isChecked())
            mentionKeys = mentionKeys + getPresenter().getUserName() + ",";
        if (binding.checkBoxSelectUserNameMentioned.isChecked())
            mentionKeys = mentionKeys + getPresenter().getUserNameMentioned() + ",";
        if (binding.editTextOtherMention.getText().toString().trim().length() > 0)
            mentionKeys = mentionKeys + binding.editTextOtherMention.getText().toString();
        return mentionKeys;
    }

    private void initChangeListener() {
        binding.checkBoxSelectFirstName.setOnCheckedChangeListener((compoundButton, b) ->
                getPresenter().setFirstNameTrigger(b));
        binding.checkBoxSelectChannelMentioned.setOnCheckedChangeListener((compoundButton, b) ->
                getPresenter().setChannelTrigger(b));

        CompoundButton.OnCheckedChangeListener mention_keysListener = (compoundButton, b) -> setMentionsKeys();
        binding.checkBoxSelectUserName.setOnCheckedChangeListener(mention_keysListener);
        binding.checkBoxSelectUserNameMentioned.setOnCheckedChangeListener(mention_keysListener);
    }

    private void initTriggers() {
        binding.textViewTitleFirstName.setText(binding.textViewTitleFirstName.getText()
                + " \"" + getPresenter().getFirstName() + "\"");
        binding.checkBoxSelectFirstName.setChecked(getPresenter().isFirstNameTrigger());

        binding.textViewTitleUserName.setText(binding.textViewTitleUserName.getText()
                + " \"" + getPresenter().getUserName() + "\"");
        binding.checkBoxSelectUserName.setChecked(getPresenter().isUserName());

        binding.textViewTitleUserNameMentioned.setText(binding.textViewTitleUserNameMentioned.getText()
                + " \"" + getPresenter().getUserNameMentioned() + "\"");
        binding.checkBoxSelectUserNameMentioned.setChecked(getPresenter().isUserNameMentioned());

        binding.checkBoxSelectChannelMentioned.setChecked(getPresenter().isChannelTrigger());

        binding.editTextOtherMention.setText(getPresenter().getOtherMentionsKeys());
    }
}

