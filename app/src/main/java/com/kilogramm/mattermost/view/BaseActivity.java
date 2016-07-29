package com.kilogramm.mattermost.view;

import android.annotation.TargetApi;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.kilogramm.mattermost.R;

/**
 * Created by Evgeny on 26.07.2016.
 */
public class BaseActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        this.toolbar = toolbar;
    }

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
}
