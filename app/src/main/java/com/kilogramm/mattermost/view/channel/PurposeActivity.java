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
import com.kilogramm.mattermost.databinding.ActivityPurposeBinding;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.presenter.channel.PurposePresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 18.10.16.
 */
@RequiresPresenter(PurposePresenter.class)
public class PurposeActivity extends BaseActivity<PurposePresenter> {
    private static final String CHANNEL = "CHANNEL";
    private ActivityPurposeBinding mBinding;
    private MenuItem mSaveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_purpose);
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
                getPresenter().setHeader(mBinding.editTextPurpose.getText().toString());
                getPresenter().requestUpdatePurpose();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_toolbar, menu);
        return true;
    }

    public void requestSave(String s) {
        mSaveItem.setVisible(true);
        mBinding.progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public static void start(Activity activity, Channel channel) {
        Intent starter = new Intent(activity, PurposeActivity.class);
        starter.putExtra(CHANNEL, channel);
        activity.startActivity(starter);
    }

    private void initData() {
        mBinding.editTextPurpose.setText(getPresenter().getPurpose());
        mBinding.editTextPurpose.setOnFocusChangeListener((v, hasFocus) -> {
            mBinding.buttonClear.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
        });
        mBinding.buttonClear.setOnClickListener(view -> mBinding.editTextPurpose.setText(""));
    }

    private void setToolbar() {
        setupToolbar(getString(getPresenter().getChannelType().equals("O") ?
                R.string.channel_purpose_toolbar : R.string.group_purpose_toolbar), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);
    }


}
