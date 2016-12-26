package com.kilogramm.mattermost.view.channel;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityNameChannelBinding;
import com.kilogramm.mattermost.presenter.channel.NamePresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 18.10.16.
 */
@RequiresPresenter(NamePresenter.class)
public class NameChannelActivity extends BaseActivity<NamePresenter> {
    private static final String CHANNEL_ID = "channel_id";
    private ActivityNameChannelBinding binding;
    MenuItem saveItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_name_channel);
        getPresenter().initPresenter(getIntent().getStringExtra(CHANNEL_ID));
        initData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                if (checkFields(binding.editTextDisplayName.getText().toString(),
                        binding.editTextName.getText().toString())) {
                    Toast.makeText(this, "Unable to save", Toast.LENGTH_SHORT).show();
                    break;
                }
                getPresenter().setDisplayName(binding.editTextDisplayName.getText().toString());
                getPresenter().setName(binding.editTextName.getText().toString());
                getPresenter().requestUpdateChannel();
                saveItem = item;
                item.setVisible(false);
                binding.progressBar.setVisibility(View.VISIBLE);
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_toolbar, menu);
        return true;
    }


    public void requestSave(String s) {
        saveItem.setVisible(true);
        binding.progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public static void start(Activity activity, String channelId) {
        Intent starter = new Intent(activity, NameChannelActivity.class);
        starter.putExtra(CHANNEL_ID, channelId);
        activity.startActivity(starter);
    }

    private void setToolbar() {
        setupToolbar(getString(getPresenter().getChannel().getType().equals("O") ?
                R.string.channel_name_toolbar : R.string.group_name_toolbar), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

    private void initData() {
        setToolbar();
        binding.editTextDisplayName.setText(getPresenter().getChannel().getDisplayName());
        binding.editTextName.setText(getPresenter().getChannel().getName());

        if (getPresenter().getChannel().getName().equals("town-square")) {
            binding.editTextName.setEnabled(false);
            binding.textViewHandleDescription.setText("Handle - Cannot be changed for the default channel");
        }

        binding.buttonClearName.setOnClickListener(view -> binding.editTextName.setText(""));
        binding.buttonClearDisplayName.setOnClickListener(view -> binding.editTextDisplayName.setText(""));

        binding.editTextName.setOnFocusChangeListener((v, hasFocus) -> {
            binding.buttonClearName.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
        });
        binding.editTextDisplayName.setOnFocusChangeListener((v, hasFocus) -> {
            binding.buttonClearDisplayName.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
        });
    }


    private boolean checkFields(String displayName, String name) {
        boolean isError = false;
        if (displayName.length() == 0) {
            binding.textViewErrorDisplayName.setVisibility(View.VISIBLE);
            isError = true;
        } else binding.textViewErrorDisplayName.setVisibility(View.INVISIBLE);
        if (name.length() == 0) {
            binding.textViewHandleError.setText(getString(R.string.channel_error));
            binding.textViewHandleError.setTextColor(getResources().getColor(R.color.red_error_send_massage));
            isError = true;
        } else {
            binding.textViewHandleError.setText(getString(R.string.channel_info_handle_info));
            binding.textViewHandleError.setTextColor(getResources().getColor(R.color.grey));
        }
        return isError;
    }


}
