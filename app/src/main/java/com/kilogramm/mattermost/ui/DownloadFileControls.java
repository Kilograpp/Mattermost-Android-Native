package com.kilogramm.mattermost.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.AttachedFilesAdapter;
import com.kilogramm.mattermost.databinding.DownloadControlsBinding;
import com.kilogramm.mattermost.model.entity.filetoattacth.DownloadFile;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.presenter.AttachedFilesPresenter;
import com.kilogramm.mattermost.presenter.DownLoadFilePresenter;
import com.kilogramm.mattermost.tools.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusLayout;

/**
 * Created by kepar on 29.9.16.
 */
@RequiresPresenter(DownLoadFilePresenter.class)
public class DownloadFileControls extends NucleusLayout<DownLoadFilePresenter> {

    public static final String TAG = "AttachedFilesLayout";

    BroadcastReceiver broadcastReceiver;

    DownloadControlsBinding binding;

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
        binding = DownloadControlsBinding.inflate(LayoutInflater.from(context), null, false);

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

        binding.iconActionDownload.setOnClickListener(v -> {
            showProgressControls();
            getPresenter().downloadFile();
        });

        binding.close.setOnClickListener(v -> {
            hideProgressControls();
            getPresenter().stopDownload();
        });
    }

    private void showProgressControls() {
        binding.iconActionDownload.setVisibility(INVISIBLE);
        binding.progressBar.setVisibility(VISIBLE);
        binding.close.setVisibility(VISIBLE);
    }

    private void hideProgressControls() {
        binding.iconActionDownload.setVisibility(VISIBLE);
        binding.progressBar.setVisibility(INVISIBLE);
        binding.close.setVisibility(INVISIBLE);
    }
}
