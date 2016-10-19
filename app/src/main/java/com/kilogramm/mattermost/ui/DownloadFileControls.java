package com.kilogramm.mattermost.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.presenter.DownLoadFilePresenter;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusLayout;

/**
 * Created by kepar on 29.9.16.
 */
@RequiresPresenter(DownLoadFilePresenter.class)
public class DownloadFileControls extends NucleusLayout<DownLoadFilePresenter> {

    public static final String TAG = "AttachedFilesLayout";

    private BroadcastReceiver broadcastReceiver;

    private String fileId;

    private View iconActionDownload;
    private ProgressBar progressBar;
    private View viewClose;

    public DownloadFileControls(Context context) {
        super(context);
        init(context);
    }

    public DownloadFileControls(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DownloadFileControls(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(broadcastReceiver);
        super.onDetachedFromWindow();
    }

    private void init(Context context) {
        inflate(context, R.layout.download_controls, this);

        iconActionDownload = findViewById(R.id.iconActionDownload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        viewClose = findViewById(R.id.close);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getExtras() != null) {
                    final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

                    if (ni != null && ni.isConnectedOrConnecting()) {
                        Log.i(TAG, "Network " + ni.getTypeName() + " connected");
                    } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                        Log.d(TAG, "There's no network connectivity");
                    }
                }
            }
        };
        getContext().registerReceiver(broadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        iconActionDownload.setOnClickListener(v -> {
            showProgressControls();
            if(fileId != null) {
                getPresenter().downloadFile(fileId);
            }
        });

        viewClose.setOnClickListener(v -> {
            hideProgressControls();
            getPresenter().stopDownload();
        });
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    private void showProgressControls() {
        iconActionDownload.setVisibility(INVISIBLE);
        progressBar.setVisibility(VISIBLE);
        viewClose.setVisibility(VISIBLE);
    }

    private void hideProgressControls() {
        iconActionDownload.setVisibility(VISIBLE);
        progressBar.setVisibility(INVISIBLE);
        viewClose.setVisibility(INVISIBLE);
    }
}
