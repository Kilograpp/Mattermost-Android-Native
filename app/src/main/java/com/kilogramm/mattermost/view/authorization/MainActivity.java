package com.kilogramm.mattermost.view.authorization;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.service.MattermostService;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.databinding.ActivityMainBinding;
import com.kilogramm.mattermost.presenter.MainPresenter;
import com.kilogramm.mattermost.view.BaseActivity;
import com.kilogramm.mattermost.view.menu.GeneralActivity;

import nucleus.factory.RequiresPresenter;

@RequiresPresenter(MainPresenter.class)
public class MainActivity extends BaseActivity<MainPresenter> {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        initView();

        MattermostService.Helper.create(this).startService();

    }

    private void initView(){

        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPresenter().checkConnetHost(getStringUrl());
            }
        });

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

    private String getStringUrl(){
        return binding.urlEditText.getText().toString();
    }

    public void showLoginActivity(){
        LoginActivity.startActivity(this, null);
    }

    public void showChatActivity(){
        GeneralActivity.start(this,
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    public void setShowProgress(boolean show){
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setShowNextButton(boolean show){
        binding.buttonNext.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    public void showErrorText(String text){
        Toast.makeText(getApplicationContext(), text ,Toast.LENGTH_SHORT).show();
    }

    public void setTextUrl(String url){
        binding.urlEditText.setText(url);
        binding.urlEditText.setSelection(binding.urlEditText.length());
    }

    public void hideKeyboard(){
        hideKeyboard(this);
    }

    public static void start(Context context, Integer flags) {
        Intent starter = new Intent(context, MainActivity.class);
        if(flags != null){
            starter.setFlags(flags);
        }
        context.startActivity(starter);
    }
}
