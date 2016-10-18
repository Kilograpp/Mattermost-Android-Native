package com.kilogramm.mattermost.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.adapters.AttachedFilesAdapter;
import com.kilogramm.mattermost.model.entity.filetoattacth.DownloadFile;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.presenter.AttachedFilesPresenter;
import com.kilogramm.mattermost.tools.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nucleus.factory.RequiresPresenter;
import nucleus.view.NucleusLayout;

/**
 * Created by kepar on 29.9.16.
 */
@RequiresPresenter(AttachedFilesPresenter.class)
public class AttachedFilesLayout extends NucleusLayout<AttachedFilesPresenter> implements DownloadFile.DownloadFileListener {

    public static final String TAG = "AttachedFilesLayout";

    private String teamId;
    private String channelId;
    private List<Uri> uriList;

    private ProgressDialog progressDialog;

    AttachedFilesAdapter attachedFilesAdapter;

    BroadcastReceiver broadcastReceiver;

    public AttachedFilesLayout(Context context) {
        super(context);
        init(context);
    }

    public AttachedFilesLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AttachedFilesLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
        inflate(context, R.layout.attached_files_layout, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        attachedFilesAdapter = new AttachedFilesAdapter(getContext(), FileToAttachRepository.getInstance().query());
        recyclerView.setAdapter(attachedFilesAdapter);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getExtras() != null) {
                    final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

                    if (ni != null && ni.isConnectedOrConnecting()) {
                        getPresenter().requestUploadFileToServer(teamId, channelId);
                        Log.i(TAG, "Network " + ni.getTypeName() + " connected");
                    } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                        Log.d(TAG, "There's no network connectivity");
                    }
                }
            }
        };
        getContext().registerReceiver(broadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public void addItems(List<Uri> uriList, String teamId, String channelId) {
        this.teamId = teamId;
        this.channelId = channelId;
        if (this.uriList == null) this.uriList = new ArrayList<>();
        this.uriList.addAll(uriList);
        uploadNext();
    }

    private void uploadNext() {
        if (uriList.size() > 0) {
            addItem(uriList.get(0));
            uriList.remove(uriList.get(0));
        }
    }

    private void addItem(Uri uri) {
        String filePath = FileUtil.getInstance().getPath(uri);
        if (filePath == null) {
            new DownloadFile(getContext(), this).execute(uri);
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setView(inflate(getContext(), R.layout.data_processing_progress_layout, null));
            progressDialog.show();
        } else {
            uploadFileToServer(uri, filePath, teamId, channelId);
        }
    }

    private void uploadFileToServer(Uri uri, String filePath, String teamId, String channelId) {
        final File file = new File(filePath);
        if (file.exists()) {
            FileToAttachRepository.getInstance().add(new FileToAttach(file.getName(), filePath, uri.toString()));
            if (!FileToAttachRepository.getInstance().haveUploadingFile()) {
                getPresenter().requestUploadFileToServer(teamId, channelId);
            }
        } else {
            Log.d(TAG, "file doesn't exists");
            Toast.makeText(getContext(), getContext().getString(R.string.cannot_open_file_to_attach), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public List<String> getAttachedFiles() {
        // lambda requires min API level 24, so use old method
        List<String> fileNames = new ArrayList<>();
        for (FileToAttach fileToAttach : attachedFilesAdapter.getData()) {
            fileNames.add(fileToAttach.getFileName());
        }
        return fileNames;
    }

    public void setEmptyListListener(AttachedFilesAdapter.EmptyListListener emptyListListener) {
        attachedFilesAdapter.setEmptyListListener(emptyListListener);
    }

    @Override
    public void onDownloadedFile(String filePath) {
        if (progressDialog != null) progressDialog.cancel();
        uploadFileToServer(Uri.parse(filePath), filePath, teamId, channelId);
    }
}
