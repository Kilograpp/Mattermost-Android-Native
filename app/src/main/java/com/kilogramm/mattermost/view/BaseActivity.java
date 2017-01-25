package com.kilogramm.mattermost.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kilogramm.mattermost.R;

import icepick.Icepick;
import nucleus.presenter.Presenter;
import nucleus.view.NucleusAppCompatActivity;

/**
 * Created by Evgeny on 26.07.2016.
 */
public abstract class BaseActivity<P extends Presenter> extends NucleusAppCompatActivity<P> {

    private Toolbar toolbar;
    private TextView typing;

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        this.toolbar = toolbar;
    }

    //====================== standard toolbar =======================================

    public void setTitleActivity(String title) {
        try {
            getSupportActionBar().setTitle(title);
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusbarAndToolbarAlpha(int alpha) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(
                    new ColorDrawable(Color.argb(alpha, 0,0,0)));
        }
        getWindow().setStatusBarColor(Color.argb(alpha, 0,0,0));

    }

    public void setupToolbar(String title, Boolean hasButtonBack) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (hasButtonBack) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        }
        setTitleActivity(title);
    }

    public void setupToolbar(Toolbar toolbar, String title, Boolean hasButtonBack) {
        setSupportActionBar(toolbar);
        if (hasButtonBack) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        }
        setTitleActivity(title);
    }

    protected SearchView initSearchView(Menu menu, TextWatcher textWatcher) {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(false);
        EditText searchText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchText.setCursorVisible(true);
        searchText.addTextChangedListener(textWatcher);
        return searchView;
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

    public void setupChannelToolbar(String activityTitle,
                                    String channelName,
                                    View.OnClickListener listenerChannelName,
                                    View.OnClickListener listenerSearch) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView channel_name = (TextView) toolbar.findViewById(R.id.channel_title);
        channel_name.setText(channelName);
        toolbar.setOnClickListener(listenerChannelName);
        channel_name.setTransformationMethod(null);

        ImageView search_message = (ImageView) toolbar.findViewById(R.id.search_message);
        search_message.setOnClickListener(listenerSearch);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_dehaze_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitleActivity(activityTitle);
    }

    public void setupTypingText(String text) {
        typing = (TextView) findViewById(R.id.typing);
        typing.setText(text);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Activity activity) {
        if (activity == null) return;

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }

        imm.showSoftInput(view, 0);
    }

    public void showErrorText(String text, View view) {
        int apiVersion = Build.VERSION.SDK_INT;
        if (apiVersion > Build.VERSION_CODES.LOLLIPOP && view != null) {
            Snackbar error = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
            error.getView().setBackgroundColor(getResources().getColor(R.color.error_color));
            error.setActionTextColor(getResources().getColor(R.color.white));
            error.setDuration(3000);
            error.show();
        } else {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        }
    }

    public void showErrorText(String text) {
        showErrorText(text, getCurrentFocus());
    }

    public void showGoodText(String text) {
        int apiVersion = Build.VERSION.SDK_INT;

        if (apiVersion > Build.VERSION_CODES.LOLLIPOP) {
            Snackbar good = Snackbar.make(getCurrentFocus(), text, Snackbar.LENGTH_LONG);
            good.getView().setBackgroundColor(getResources().getColor(R.color.green_send_massage));
            good.setActionTextColor(getResources().getColor(R.color.white));
            good.show();
        } else {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        }
    }
}
