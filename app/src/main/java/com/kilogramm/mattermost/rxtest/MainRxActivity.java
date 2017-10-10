package com.kilogramm.mattermost.rxtest;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.kilogramm.mattermost.MattermostPreference;
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
            setShowNextButton(false);
            getPresenter().request(getStringUrl());
            hideKeyboard();
        });

        // get url from preferences
        String urlFromPreferences = MattermostPreference.getInstance().getBaseUrl();
        if (urlFromPreferences != null){
            if (!urlFromPreferences.startsWith("https://") || !urlFromPreferences.startsWith("http://")){
                urlFromPreferences = "https://" + urlFromPreferences;
            }
            binding.urlEditText.setText(urlFromPreferences);
            if (checkEnteredUrl(getStringUrl()) || (binding.urlEditText.getText().length() == 0)){
                setShowNextButton(true);
            }
        }

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
                if (checkEnteredUrl(getStringUrl()) || (binding.urlEditText.getText().length() == 0)) {
                    hideEditTextErrorMessage();
                } else {
                    showEditTextErrorMessage();
                    setShowNextButton(false);
                }
            }, 2500);
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

    /**
     * At current time is used only for setting debug url "https://mattermost.kilograpp.com"
     *
     * @param url url for debug mode. At current time it's "https://mattermost.kilograpp.com"
     */
    public void setTextUrl(String url) {
        binding.urlEditText.setText(url);
        binding.urlEditText.setSelection(binding.urlEditText.length());
    }

    public String getStringUrl() {
        return binding.urlEditText.getText().toString();
    }

    public void showEditTextErrorMessage() {
        binding.editTextInputLayout.setError(getString(R.string.main_url_error));
    }

    private boolean checkEnteredUrl(String url) {
        return getPresenter().isValidUrl(url);
    }

    public void setShowNextButton(boolean show) {
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
