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
import com.kilogramm.mattermost.databinding.ActivityHeaderChannelBinding;
import com.kilogramm.mattermost.presenter.channel.HeaderPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 18.10.16.
 */
@RequiresPresenter(HeaderPresenter.class)
public class HeaderChannelActivity extends BaseActivity<HeaderPresenter> {
    private static final String CHANNEL_HEADER = "CHANNEL_HEADER";
    private static final String CHANNEL_ID = "channel_id";
    private ActivityHeaderChannelBinding binding;
    private MenuItem saveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_header_channel);
        setToolbar();
        getPresenter().initPresenter(
                getIntent().getStringExtra(CHANNEL_HEADER),
                getIntent().getStringExtra(CHANNEL_ID));
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
                getPresenter().setHeader(binding.editTextHeader.getText().toString());
                getPresenter().requestUpdateHeader();
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
        getMenuInflater().inflate(R.menu.save_toolbar, menu);
        return true;
    }

    public void requestSave(String s) {
        saveItem.setVisible(true);
        binding.progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }



    public static void start(Activity activity, String header, String channelId) {
        Intent starter = new Intent(activity, HeaderChannelActivity.class);
        starter.putExtra(CHANNEL_HEADER, header);
        starter.putExtra(CHANNEL_ID, channelId);
        activity.startActivity(starter);
    }

    private void initData() {
        binding.editTextHeader.setText(getPresenter().getHeader());

        binding.editTextHeader.setOnFocusChangeListener((v, hasFocus) -> {
            binding.imageViewClear.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
        });

        binding.imageViewClear.setOnClickListener(view -> binding.editTextHeader.setText(""));
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.channel_header_toolbar), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

}
