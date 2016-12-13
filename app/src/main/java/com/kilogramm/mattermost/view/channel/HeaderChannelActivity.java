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
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.presenter.channel.HeaderPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 18.10.16.
 */
@RequiresPresenter(HeaderPresenter.class)
public class HeaderChannelActivity extends BaseActivity<HeaderPresenter> {
    private static final String CHANNEL = "CHANNEL";
    private ActivityHeaderChannelBinding mBinding;
    private MenuItem mSaveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_header_channel);
        getPresenter().initPresenter(
                getIntent().getParcelableExtra(CHANNEL));
        initData();
        setToolbar();
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
                getPresenter().setHeader(mBinding.editTextHeader.getText().toString());
                getPresenter().requestUpdateHeader();
                mSaveItem = item;
                item.setVisible(false);
                mBinding.progressBar.setVisibility(View.VISIBLE);
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
        mSaveItem.setVisible(true);
        mBinding.progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    public static void start(Activity activity, Channel channel) {
        Intent starter = new Intent(activity, HeaderChannelActivity.class);
        starter.putExtra(CHANNEL, channel);
        activity.startActivity(starter);
    }

    private void initData() {
        mBinding.editTextHeader.setText(getPresenter().getHeader());

        mBinding.editTextHeader.setOnFocusChangeListener((v, hasFocus) -> {
            mBinding.imageViewClear.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
        });

        mBinding.imageViewClear.setOnClickListener(view -> mBinding.editTextHeader.setText(""));
    }

    private void setToolbar() {
        setupToolbar(getString(getPresenter().getTypeChannel().equals("O") ?
                R.string.channel_header_toolbar : R.string.group_header_toolbar), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }

}
