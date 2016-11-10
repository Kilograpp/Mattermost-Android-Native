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
public class MainRxAcivity extends BaseActivity<MainRxPresenter> {

    public static final String TAG = "MainRxAcivity";

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        MattermostService.Helper.create(this).startService();
        initView();
    }

    private void initView() {

        binding.buttonNext.setOnClickListener(view -> getPresenter().request(getStringUrl()));

        binding.urlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //TODO Check use case
                getPresenter().checkEnterUrl(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public String getStringUrl() {
        return binding.urlEditText.getText().toString();
    }

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

    public void setShowNextButton(boolean show) {
        binding.buttonNext.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setTextUrl(String url) {
        binding.urlEditText.setText(url);
        binding.urlEditText.setSelection(binding.urlEditText.length());
    }

    public void showEditTextErrorMessage() {
        binding.editTextInputLayout.setError(getString(R.string.main_url_error));
    }

    public void hideKeyboard() {
        hideKeyboard(this);
    }


    public static void start(Context context, Integer flags) {
        Intent starter = new Intent(context, MainRxAcivity.class);
        if (flags != null) {
            starter.setFlags(flags);
        }
        context.startActivity(starter);
    }
}
