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
import android.widget.ImageView;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.SetNameBinding;
import com.kilogramm.mattermost.presenter.channel.NamePresenter;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by ngers on 18.10.16.
 */
@RequiresPresenter(NamePresenter.class)
public class NameActivity extends BaseActivity<NamePresenter> {
    private static final String CHANNEL_ID = "channel_id";
    private SetNameBinding binding;
    MenuItem saveItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.set_name);
        setToolbar();
        getPresenter().initPresenter(getIntent().getStringExtra(CHANNEL_ID));
        initData();
    }

    private void initData() {
        binding.displayName.setText(getPresenter().getChannel().getDisplayName());
        binding.name.setText(getPresenter().getChannel().getName());

        binding.displayName.addTextChangedListener(setTextWatcher(binding.btnClearDisplayName));
        binding.name.addTextChangedListener(setTextWatcher(binding.btnClearName));

        if(getPresenter().getChannel().getName().equals("town-square")){
            binding.name.setEnabled(false);
            binding.handleDescription.setText("Handle - Cannot be changed for the default channel");
        }

        binding.btnClearName.setOnClickListener(view -> binding.name.setText(""));
        binding.btnClearDisplayName.setOnClickListener(view -> binding.displayName.setText(""));
    }

    private TextWatcher setTextWatcher(ImageView view) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0)
                    view.setVisibility(View.VISIBLE);
                else
                    view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }


    public void requestSave(String s) {
        saveItem.setVisible(true);
        binding.progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

    }

    private void setToolbar() {
        setupToolbar(getString(R.string.channel_name_toolbar), true);
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
                getPresenter().setDisplayName(binding.displayName.getText().toString());
                getPresenter().setName(binding.name.getText().toString());
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

    public static void start(Activity activity, String channelId) {
        Intent starter = new Intent(activity, NameActivity.class);
        starter.putExtra(CHANNEL_ID, channelId);
        activity.startActivity(starter);
    }

}
