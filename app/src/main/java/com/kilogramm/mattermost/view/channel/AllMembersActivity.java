package com.kilogramm.mattermost.view.channel;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.MembersListBinding;
import com.kilogramm.mattermost.model.entity.SaveData;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelByNameSpecification;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.presenter.channel.AllMembersPresenter;
import com.kilogramm.mattermost.rxtest.GeneralRxActivity;
import com.kilogramm.mattermost.view.BaseActivity;

import io.realm.OrderedRealmCollection;
import io.realm.RealmResults;
import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 01.11.16.
 */
@RequiresPresenter(AllMembersPresenter.class)
public class AllMembersActivity extends BaseActivity<AllMembersPresenter> {
    private static final String CHANNEL_ID = "channel_id";

    MembersListBinding binding;
    AllMembersAdapter allMembersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.members_list);
        setToolbar();
        initiationData();
    }

    private void initiationData() {
        allMembersAdapter = new AllMembersAdapter(
                this,
                id -> openDirect(id));
        binding.list.setAdapter(allMembersAdapter);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        getPresenter().initPresenter(getIntent().getStringExtra(CHANNEL_ID));

        binding.searchText.addTextChangedListener(getMassageTextWatcher());
        binding.btnClear.setOnClickListener(view -> {
            binding.searchText.setText("");
            hideKeyboard(this);
        });
    }

    private void openDirect(String id) {
        String userId = MattermostPreference.getInstance().getMyUserId();
        if (!userId.equals(id)) {
            RealmResults<Channel> channels = ChannelRepository.query(new ChannelByNameSpecification(null, id));
            if (channels.size() > 0) {
                MattermostPreference.getInstance().setLastChannelId(
                        channels.first().getId()
                );
                startGeneralActivity();
            } else startDialog(id);
        }
    }

    public void startGeneralActivity() {
        GeneralRxActivity.start(this, null);
    }

    private void startDialog(String userTalkToId) {
        SaveData saveData = new SaveData(
                userTalkToId,
                MattermostPreference.getInstance().getMyUserId(),
                true,
                "direct_channel_show");

        getPresenter().requestSaveData(saveData, userTalkToId);

    }


    public TextWatcher getMassageTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() > 0) {
                    updateDataList(getPresenter().getMembers(charSequence.toString()));
                    binding.btnClear.setVisibility(View.VISIBLE);
                } else {
                    updateDataList(getPresenter().getMembers());
                    binding.btnClear.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    public void updateDataList(OrderedRealmCollection<User> realmResult) {
        allMembersAdapter.updateData(realmResult);
        if (realmResult.size() == 0) {
            binding.listEmpty.setVisibility(View.VISIBLE);
        } else
            binding.listEmpty.setVisibility(View.INVISIBLE);
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.all_members_toolbar), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    public static void start(Activity activity, String channelId) {
        Intent starter = new Intent(activity, AllMembersActivity.class);
        starter.putExtra(CHANNEL_ID, channelId);
        activity.startActivity(starter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
