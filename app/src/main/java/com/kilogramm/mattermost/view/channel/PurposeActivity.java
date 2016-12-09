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
import com.kilogramm.mattermost.databinding.SetPurposeBinding;
import com.kilogramm.mattermost.presenter.channel.PurposePresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 18.10.16.
 */
@RequiresPresenter(PurposePresenter.class)
public class PurposeActivity extends BaseActivity<PurposePresenter> {
    private static final String CHANNEL_PURPOSE = "CHANNEL_PURPOSE";
    private static final String CHANNEL_ID = "channel_id";
    private SetPurposeBinding binding;
    MenuItem saveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.set_purpose);
        setToolbar();
        getPresenter().initPresenter(
                getIntent().getStringExtra(CHANNEL_PURPOSE),
                getIntent().getStringExtra(CHANNEL_ID));
        initData();
    }

    private void initData() {
        binding.purpose.setText(getPresenter().getPurpose());
        binding.purpose.setOnFocusChangeListener((v, hasFocus) -> {
            binding.btnClear.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
        });
        binding.btnClear.setOnClickListener(view -> binding.purpose.setText(""));
    }


    public void requestSave(String s) {
        saveItem.setVisible(true);
        binding.progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.channel_purpose_toolbar), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
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
                getPresenter().setHeader(binding.purpose.getText().toString());
                getPresenter().requestUpdatePurpose();
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

    public static void start(Activity activity, String purpose, String channelId) {
        Intent starter = new Intent(activity, PurposeActivity.class);
        starter.putExtra(CHANNEL_PURPOSE, purpose);
        starter.putExtra(CHANNEL_ID, channelId);
        activity.startActivity(starter);
    }

}
