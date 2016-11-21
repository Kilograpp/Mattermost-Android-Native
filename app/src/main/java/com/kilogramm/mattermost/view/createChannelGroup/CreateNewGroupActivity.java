package com.kilogramm.mattermost.view.createChannelGroup;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityCreateChannelGroupBinding;
import com.kilogramm.mattermost.presenter.CreateNewGroupPresenter;
import com.kilogramm.mattermost.utils.ColorGenerator;
import com.kilogramm.mattermost.utils.Transliterator;
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
    private ColorGenerator colorGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_channel_group);
        this.init();
    }

    private void init() {
        binding.tvChannelName.setHint(getResources().getString(R.string.create_new_group_name_hint));
        setupToolbar(getString(R.string.create_new_ch_gr_toolbar_gr), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);

        colorGenerator = ColorGenerator.MATERIAL;
        binding.newChannelAvatar.getBackground()
                .setColorFilter(colorGenerator.getRandomColor(), PorterDuff.Mode.MULTIPLY);

        binding.tvChannelName.addTextChangedListener(textingWatcher);
    }

    private final TextWatcher textingWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (binding.tvChannelName.getText().length() == 0) {
                binding.newChannelAvatar.setText("");
            } else {
                binding.newChannelAvatar.setText(String.valueOf(binding.tvChannelName.getText().toString().charAt(0)));
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_channel_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                BaseActivity.hideKeyboard(this);
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

    public static void startActivityForResult(Fragment fragment, Integer requestCode) {
        Intent starter = new Intent(fragment.getActivity(), CreateNewGroupActivity.class);
        starter.putExtra(TYPE, groupType);
        fragment.startActivityForResult(starter, requestCode);
    }

    private String makeName(String groupName) {
        groupName = Transliterator.transliterate(groupName);

        if (groupName.contains(" ")) {
            return groupName.replaceAll("\\s", "-").toLowerCase();
        } else {
            return groupName.toLowerCase();
        }
    }
}
