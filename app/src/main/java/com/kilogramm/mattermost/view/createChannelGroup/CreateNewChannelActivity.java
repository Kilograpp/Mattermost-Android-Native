package com.kilogramm.mattermost.view.createChannelGroup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityCreateChannelGroupBinding;
import com.kilogramm.mattermost.presenter.CreateNewChannelPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 01.11.16.
 */

@RequiresPresenter(CreateNewChannelPresenter.class)
public class CreateNewChannelActivity extends BaseActivity<CreateNewChannelPresenter> {
    public static final String TYPE = "TYPE";

    private ActivityCreateChannelGroupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_channel_group);
        setupToolbar(getString(R.string.create_new_ch_gr_toolber_ch), true);
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
                finishActivity();
                break;

            case R.id.action_create:
                if (binding.tvChannelName.getText().length() != 0) {
                        getPresenter().requestCreateChannel(binding.tvChannelName.getText().toString(),
                                binding.header.getText().toString(),
                                binding.purpose.getText().toString());
                        break;
                } else {
                    String errorText = "     Channel name is required \n";
                    getPresenter().sendShowError(errorText);
                }
                BaseActivity.hideKeyboard(this);
                break;
            default:
                super.onOptionsItemSelected(item);

        }
        return true;
    }

    public void finishActivity() {
        this.finish();
    }

    public static void startActivityForResult(Activity context, Integer requestCode) {
        Intent starter = new Intent(context, CreateNewChannelActivity.class);
        starter.putExtra(TYPE, "O");
        context.startActivityForResult(starter, requestCode);
    }
}
