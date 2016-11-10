package com.kilogramm.mattermost.view.channel;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.SetHeaderBinding;
import com.kilogramm.mattermost.presenter.channel.HeaderPresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 18.10.16.
 */
@RequiresPresenter(HeaderPresenter.class)
public class HeaderActivity extends BaseActivity<HeaderPresenter> {
    private static final String CHANNEL_HEADER = "CHANNEL_HEADER";
    private static final String CHANNEL_ID = "channel_id";
    private SetHeaderBinding binding;
    MenuItem saveItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.set_header);
        setToolbar();
        getPresenter().initPresenter(
                getIntent().getStringExtra(CHANNEL_HEADER),
                getIntent().getStringExtra(CHANNEL_ID));
        initData();
    }

    private void initData() {
        binding.header.setText(getPresenter().getHeader());
        binding.header.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0)
                    binding.btnClear.setVisibility(View.VISIBLE);
                else
                    binding.btnClear.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.btnClear.setOnClickListener(view -> binding.header.setText(""));
    }


    public void requestSave(String s) {
        saveItem.setVisible(true);
        binding.progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void setToolbar() {
        setupToolbar(getString(R.string.channel_header_toolbar), true);
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
                getPresenter().setHeader(binding.header.getText().toString());
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_toolbar, menu);
        return true;
    }

    public static void start(Activity activity, String header, String channelId) {
        Intent starter = new Intent(activity, HeaderActivity.class);
        starter.putExtra(CHANNEL_HEADER, header);
        starter.putExtra(CHANNEL_ID, channelId);
        activity.startActivity(starter);
    }

}
