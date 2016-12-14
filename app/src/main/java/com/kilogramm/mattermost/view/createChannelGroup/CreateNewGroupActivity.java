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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 01.11.16.
 */

@RequiresPresenter(CreateNewGroupPresenter.class)
public class CreateNewGroupActivity extends BaseActivity<CreateNewGroupPresenter> {
    private ActivityCreateChannelGroupBinding mBinding;
    private ColorGenerator mColorGenerator;
    private boolean isDisableCreate = false;
    private String mGroupName;

    public static final String sTYPE = "sTYPE";
    public static final String sGROUP_TYPE = "P";
    public static final String sGROUP_NAME = "sCHANNEL_NAME";
    public static final String sCREATED_GROUP_ID = "sCREATED_CHANNEL_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_channel_group);
        this.init();
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
                BaseActivity.hideKeyboard(this);
                this.finish();
                break;
            case R.id.action_create:
                if (!isDisableCreate) {
                    return false;
                }
                if (mBinding.tvChannelName.getText().length() == 0) {
                    getPresenter().sendShowError(getResources().getString(R.string.create_new_channel_error));
                } else {
                    this.createGroup();
                }
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void finishActivity(String createdGroupId, String groupName) {
        Intent finish = new Intent(this, CreateNewGroupActivity.class)
                .putExtra(sCREATED_GROUP_ID, createdGroupId)
                .putExtra(sGROUP_NAME, groupName)
                .putExtra(sTYPE, sGROUP_TYPE);
        setResult(Activity.RESULT_OK, finish);
        this.finish();
    }

    public void setProgressVisibility(Boolean bool) {
        mBinding.progressBar.setVisibility(bool ? View.VISIBLE : View.GONE);
    }

    public static void startActivityForResult(Fragment fragment, Integer requestCode) {
        Intent starter = new Intent(fragment.getActivity(), CreateNewGroupActivity.class);
        starter.putExtra(sTYPE, sGROUP_TYPE);
        fragment.startActivityForResult(starter, requestCode);
    }

    private void createGroup() {
        getPresenter().requestCreateGroup(
                mGroupName,
                mBinding.tvChannelName.getText().toString(),
                mBinding.header.getText().toString(),
                mBinding.purpose.getText().toString());

        BaseActivity.hideKeyboard(this);
    }

    private void init() {
        mBinding.tvChannelName.setHint(getResources().getString(R.string.create_new_group_name_hint));
        mBinding.privateGroupHint.setVisibility(View.VISIBLE);
        setupToolbar(getString(R.string.create_new_ch_gr_toolbar_gr), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);

        mColorGenerator = ColorGenerator.MATERIAL;
        mBinding.newChannelAvatar.getBackground()
                .setColorFilter(mColorGenerator.getRandomColor(), PorterDuff.Mode.MULTIPLY);

        mBinding.tvChannelName.addTextChangedListener(mGroupNameWatcher);
    }

    private String makeNewGroupUrl(String input) {
        Pattern allowedSymbols = Pattern.compile("[^0-9a-zA-Z\\-\\ ]");
        Matcher allowedMatcher = allowedSymbols.matcher(input);

        input = Transliterator.transliterate(input.toLowerCase());
        if (input.contains(" ")) {
            input = input.replaceAll("\\s", "-");
        }

        if (allowedMatcher.find()) {
            mBinding.textViewCustomHint.setText(getResources().getString(R.string.create_new_ch_gr_handler_rec));
            mBinding.textViewCustomHint.setTextColor(getResources().getColor(R.color.error_color));
            isDisableCreate = false;
        } else if (!allowedMatcher.find() || input.length() == 0) {
            mBinding.textViewCustomHint.setText(getResources().getString(R.string.create_new_ch_gr_handler_description));
            mBinding.textViewCustomHint.setTextColor(getResources().getColor(R.color.grey));
            isDisableCreate = true;
        }

        return input;
    }

    private final TextWatcher mGroupNameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mBinding.tvChannelName.getText().length() == 0) {
                mBinding.newChannelAvatar.setText("");
            } else {
                mBinding.newChannelAvatar.setText(String.valueOf(mBinding.tvChannelName.getText().toString().charAt(0)));
            }

            mGroupName = makeNewGroupUrl(mBinding.tvChannelName.getText().toString());
            mBinding.editTextHandle.setText(mGroupName);
        }
    };
}
