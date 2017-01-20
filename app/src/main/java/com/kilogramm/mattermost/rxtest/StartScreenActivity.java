package com.kilogramm.mattermost.rxtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.view.BaseActivity;

import java.util.List;

import io.realm.Realm;

/**
 * Created by kepar on 18.01.17.
 */

public class StartScreenActivity extends BaseActivity<StartScreenPresenter> {

    BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        findViewById(R.id.imageView).postDelayed(this::tryToStart, 2000);
    }

    private void tryToStart() {
        if (getPresenter().isNetworkAvailable()) {
            startActivity(new Intent(this, MainRxActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            showErrorTextForever(getString(R.string.network_error),
                    findViewById(R.id.imageView));

            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getExtras() != null) {
                        final ConnectivityManager connectivityManager =
                                (ConnectivityManager) context
                                .getSystemService(Context.CONNECTIVITY_SERVICE);
                        final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

                        if (ni != null && ni.isConnectedOrConnecting()) {
                            tryToStart();
                        }
                    }
                }
            };

            registerReceiver(mBroadcastReceiver,
                    new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    public void showErrorTextForever(String text, View view) {
        int apiVersion = Build.VERSION.SDK_INT;
        if (apiVersion > Build.VERSION_CODES.LOLLIPOP && view != null) {
            Snackbar error = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE);
            error.getView().setBackgroundColor(getResources().getColor(R.color.error_color));
            error.setActionTextColor(getResources().getColor(R.color.white));
            error.show();
        } else {
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        super.onDestroy();
    }
}
