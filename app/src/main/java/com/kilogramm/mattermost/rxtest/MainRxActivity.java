package com.kilogramm.mattermost.rxtest;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityMainBinding;
import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.view.BaseActivity;

import nucleus.factory.RequiresPresenter;

/**
 * Created by Evgeny on 03.10.2016.
 */
@RequiresPresenter(MainRxPresenter.class)
public class MainRxActivity extends BaseActivity<MainRxPresenter> {

    public static final String TAG = "MainRxActivity";

    private final int DELAY = 2500;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        MattermostService.Helper.create(this).startService();
        initView();

        Intent intentService = new Intent(this, MattermostService.class);
        startService(intentService);
    }

    private void initView() {
        binding.buttonNext.setOnClickListener(view -> {
            getPresenter().request(getStringUrl());
            hideKeyboard();
        });
        binding.urlEditText.addTextChangedListener(textWatcher);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            setShowNextButton(false);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            binding.urlEditText.postDelayed(() -> {
                if (checkEnteredUrl(getStringUrl())) {
                    hideEditTextErrorMessage();
                } else {
                    showEditTextErrorMessage();
                    setShowNextButton(false);
                }
            }, DELAY);
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (checkEnteredUrl(getStringUrl())) {
                setShowNextButton(true);
            }
        }
    };

    // presenter methods

    public void showLoginActivity() {
        Log.d(TAG, "showLoginActivity");
        LoginRxActivity.startActivity(this, null);
    }

    public void showChatActivity() {
        GeneralRxActivity.start(this,
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    public void setShowProgress(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setTextUrl(String url) {
        binding.urlEditText.setText(url);
        binding.urlEditText.setSelection(binding.urlEditText.length());
    }

    //

    public String getStringUrl() {
        return binding.urlEditText.getText().toString();
    }

    public void showEditTextErrorMessage() {
        binding.editTextInputLayout.setError(getString(R.string.main_url_error));
    }

    private boolean checkEnteredUrl(String url) {
        return getPresenter().isValidUrl(url);
    }

    private void setShowNextButton(boolean show) {
        binding.buttonNext.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void hideEditTextErrorMessage() {
        binding.editTextInputLayout.setError("");
    }

    private void hideKeyboard() {
        hideKeyboard(this);
    }

    public static void start(Context context, Integer flags) {
        Intent starter = new Intent(context, MainRxActivity.class);
        if (flags != null) {
            starter.setFlags(flags);
        }
        context.startActivity(starter);
    }
}
