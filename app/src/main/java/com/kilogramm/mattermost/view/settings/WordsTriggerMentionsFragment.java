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

    private void initChangeListener() {
        binding.selectFirstName.setOnCheckedChangeListener((compoundButton, b) ->
                getPresenter().setFirstNameTrigger(b));
        binding.selectChannelMentioned.setOnCheckedChangeListener((compoundButton, b) ->
                getPresenter().setChannelTrigger(b));

        CompoundButton.OnCheckedChangeListener mention_keysListener = (compoundButton, b) -> setMentionsKeys();
        binding.selectUserName.setOnCheckedChangeListener(mention_keysListener);
        binding.selectUserNameMentioned.setOnCheckedChangeListener(mention_keysListener);
    }

    private void initTriggers() {
        binding.titleFirstName.setText(binding.titleFirstName.getText()
                + " \"" + getPresenter().getFirstName() + "\"");
        binding.selectFirstName.setChecked(getPresenter().isFirstNameTrigger());

        binding.titleUserName.setText(binding.titleUserName.getText()
                + " \"" + getPresenter().getUserName() + "\"");
        binding.selectUserName.setChecked(getPresenter().isUserName());

        binding.titleUserNameMentioned.setText(binding.titleUserNameMentioned.getText()
                + " \"" + getPresenter().getUserNameMentioned() + "\"");
        binding.selectUserNameMentioned.setChecked(getPresenter().isUserNameMentioned());

        binding.selectChannelMentioned.setChecked(getPresenter().isChannelTrigger());

        binding.otherMention.setText(getPresenter().getOtherMentionsKeys());
    }

    public String getAllMentions() {
        String mentionKeys = "";
        if (binding.selectUserName.isChecked())
            mentionKeys = mentionKeys + getPresenter().getUserName() + ",";
        if (binding.selectUserNameMentioned.isChecked())
            mentionKeys = mentionKeys + getPresenter().getUserNameMentioned() + ",";
        if (binding.otherMention.getText().toString().trim().length() > 0)
            mentionKeys = mentionKeys + binding.otherMention.getText().toString();
        return mentionKeys;
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
}

