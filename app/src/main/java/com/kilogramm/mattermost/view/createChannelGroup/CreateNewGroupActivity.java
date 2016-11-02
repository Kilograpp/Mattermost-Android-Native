package com.kilogramm.mattermost.view.createChannelGroup;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityCreateChannelGroupBinding;
import com.kilogramm.mattermost.presenter.CreateNewGroupPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 01.11.16.
 */
@RequiresPresenter(CreateNewGroupPresenter.class)
public class CreateNewGroupActivity extends BaseActivity<CreateNewGroupPresenter> {
    public static final String TYPE = "TYPE";
    public static final String groupType = "P";
    public static final String GROUP_NAME = "CHANNEL_NAME";
    public static final String CREATED_GROUP_ID = "CREATED_CHANNEL_ID";

    private ActivityCreateChannelGroupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_channel_group);
        setupToolbar(getString(R.string.create_new_ch_gr_toolber_gr), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_channel_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_create:
                this.createGroup();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    public static void startActivityForResult(Activity context, Integer requestCode) {
        Intent starter = new Intent(context, CreateNewGroupActivity.class);
        starter.putExtra(TYPE, groupType);
        context.startActivityForResult(starter, requestCode);
    }

    public void finishActivity(String createdGroupId, String groupName) {
        Intent finish = new Intent(this, CreateNewGroupActivity.class)
                .putExtra(CREATED_GROUP_ID, createdGroupId)
                .putExtra(GROUP_NAME, groupName)
                .putExtra(TYPE, groupType);
        setResult(Activity.RESULT_OK, finish);
        this.finish();
    }

    public void setProgressVisibility(Boolean bool) {
        binding.progressBar.setVisibility(bool ? View.VISIBLE : View.GONE);
    }

    private void createGroup() {
        if (binding.tvChannelName.getText().length() != 0) {
            getPresenter().requestCreateGroup(
                    this.makeName(binding.tvChannelName.getText().toString()),
                    binding.tvChannelName.getText().toString(),
                    binding.header.getText().toString(),
                    binding.purpose.getText().toString());
        } else {
            getPresenter().sendShowError(getResources().getString(R.string.create_new_group_error));
        }
        BaseActivity.hideKeyboard(this);
    }

    private String makeName(String groupName) {
        if (groupName.contains(" ")) {
            return groupName.replaceAll("\\s", "-").toLowerCase();
        } else {
            return groupName.toLowerCase();
        }
    }
}
