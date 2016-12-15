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
import com.kilogramm.mattermost.presenter.CreateNewChannelPresenter;
import com.kilogramm.mattermost.utils.ColorGenerator;
import com.kilogramm.mattermost.utils.Transliterator;
import com.kilogramm.mattermost.view.BaseActivity;
import com.vdurmont.emoji.EmojiParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nucleus.factory.RequiresPresenter;

/**
 * Created by melkshake on 01.11.16.
 */

@RequiresPresenter(CreateNewChannelPresenter.class)
public class CreateNewChannelActivity extends BaseActivity<CreateNewChannelPresenter> {
    private ActivityCreateChannelGroupBinding mBinding;
    private ColorGenerator mColorGenerator;
    private boolean isCreateClickable = false;
    private boolean isHandlerTouched = false;
    private String mChannelName;

    public static final String sTYPE = "sTYPE";
    public static final String sCHANNEL_TYPE = "O";
    public static final String CHANNEL_NAME = "sCHANNEL_NAME";
    public static final String sCREATED_CHANNEL_ID = "sCREATED_CHANNEL_ID";

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
                if (!isCreateClickable) {
                    return false;
                }
                if (mBinding.tvChannelName.getText().length() == 0) {
                    getPresenter().sendShowError(getResources().getString(R.string.create_new_channel_error));
                } else {
                    this.createChannel();
                }
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void finishActivity(String createdChannelId, String channelName) {
        Intent finish = new Intent(this, CreateNewGroupActivity.class)
                .putExtra(sCREATED_CHANNEL_ID, createdChannelId)
                .putExtra(CHANNEL_NAME, channelName)
                .putExtra(sTYPE, sCHANNEL_TYPE);
        setResult(Activity.RESULT_OK, finish);
        this.finish();
    }

    public void setProgressVisibility(Boolean bool) {
        mBinding.progressBar.setVisibility(bool ? View.VISIBLE : View.GONE);
    }

    private void init() {
        mBinding.tvChannelName.setHint(getResources().getString(R.string.create_new_channel_name_hint));
        setupToolbar(getString(R.string.create_new_ch_gr_toolbar_ch), true);
        setColorScheme(R.color.colorPrimary, R.color.colorPrimaryDark);

        mColorGenerator = ColorGenerator.MATERIAL;
        mBinding.newChannelAvatar.getBackground()
                .setColorFilter(mColorGenerator.getRandomColor(), PorterDuff.Mode.MULTIPLY);

        mBinding.tvChannelName.addTextChangedListener(mChannelNameWatcher);
        mBinding.editTextHandle.addTextChangedListener(mChannelHandler);
    }

    private void createChannel() {
        getPresenter().requestCreateChannel(
                mChannelName,
                mBinding.tvChannelName.getText().toString(),
                mBinding.header.getText().toString(),
                mBinding.purpose.getText().toString());

        BaseActivity.hideKeyboard(this);
    }

    private void createChannelAvatar() {
        if (mBinding.tvChannelName.getText().length() == 0) {
            mBinding.newChannelAvatar.setText("");
        } else {
            mBinding.newChannelAvatar.setText(EmojiParser.removeAllEmojis(
                    mBinding.tvChannelName.getText().toString().toUpperCase()));
        }
    }

    public static void startActivityForResult(Fragment fragment, Integer requestCode) {
        Intent starter = new Intent(fragment.getActivity(), CreateNewChannelActivity.class);
        starter.putExtra(sTYPE, "O");
        fragment.startActivityForResult(starter, requestCode);
    }

    protected final TextWatcher mChannelNameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            createChannelAvatar();

            String transliteratedInput = Transliterator.transliterate(s.toString().toLowerCase());
            if (transliteratedInput.contains(" ")) {
                transliteratedInput = transliteratedInput.replaceAll("\\s", "-");
            }

            if (!isHandlerTouched) {
                checkUserInput(transliteratedInput);
//                mBinding.editTextHandle.setText(mChannelName);
                mBinding.editTextHandle.setText(transliteratedInput);
            }

            mChannelName = mBinding.editTextHandle.getText().toString();
        }
    };

    private final TextWatcher mChannelHandler = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkUserInput(s.toString());

            if (getCurrentFocus() == mBinding.editTextHandle) {
                isHandlerTouched = true;
            }
            if (s.length() == 0) {
                isHandlerTouched = false;
            }
        }
    };

    private void checkUserInput(String input) {
        Pattern notAllowedSymbols = Pattern.compile("[^0-9a-z\\-]");
        Matcher notAllowedMatcher = notAllowedSymbols.matcher(input);

        if (notAllowedMatcher.find()) {
            mBinding.textViewCustomHint.setText(getResources().getString(R.string.create_new_ch_gr_handler_rec));
            mBinding.textViewCustomHint.setTextColor(getResources().getColor(R.color.error_color));
            isCreateClickable = false;
        } else if (!notAllowedMatcher.find() || input.length() == 0) {
            mBinding.textViewCustomHint.setText(getResources().getString(R.string.create_new_ch_gr_handler_rec));
            mBinding.textViewCustomHint.setTextColor(getResources().getColor(R.color.grey));
            isCreateClickable = true;
        }
    }
}