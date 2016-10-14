package com.kilogramm.mattermost.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import com.kilogramm.mattermost.R;

import icepick.Icepick;
import nucleus.presenter.Presenter;
import nucleus.view.NucleusAppCompatActivity;

/**
 * Created by Evgeny on 26.07.2016.
 */
public abstract class BaseActivity<P extends Presenter> extends NucleusAppCompatActivity<P> {

    private Toolbar toolbar;

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        this.toolbar = toolbar;
    }

    //====================== standart toolbar =======================================
    public void setTitleActivity(String title) {
        try {
            getSupportActionBar().setTitle(title);
            //((TextView) toolbar.findViewById(R.id.toolbarTitle)).setText(title);
        } catch (Exception e) {
            getSupportActionBar().setTitle(title);
        }
    }

    public void setColorScheme(int appBarColorRes, int portStatusBarColorRes) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(
                    new ColorDrawable(getResources().getColor(appBarColorRes)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusBarColorActivity(portStatusBarColorRes);
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColorActivity(int portStatusBarColorRes) {
        getWindow().setStatusBarColor(getResources().getColor(portStatusBarColorRes));
    }

    public void setupToolbar(String title, Boolean hasButtonBack){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(hasButtonBack) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        }
        setTitleActivity(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);

    }

    //====================== channel toolbar ========================================

    public void setupChannelToolbar(String acrivityTitle, String channelName, View.OnClickListener listenerChannelName,
                                    View.OnClickListener listenerSearch) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Button channel_name = (Button) toolbar.findViewById(R.id.channel_title);
        channel_name.setText(channelName);
        channel_name.setOnClickListener(listenerChannelName);

        // TODO критует на некоторых устройства (Самсунг и Huawei)
        ImageView search_message = (ImageView) toolbar.findViewById(R.id.search_message);
        search_message.setOnClickListener(listenerSearch);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_dehaze_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitleActivity(acrivityTitle);
    }

    public static void hideKeyboard(Activity activity) {
        if(activity == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
