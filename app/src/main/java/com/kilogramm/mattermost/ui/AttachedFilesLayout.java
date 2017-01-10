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
import com.kilogramm.mattermost.model.entity.UploadState;
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

    private static final int FILE_TO_ATTACH_MAX = 5;

    private static final String TAG = "AttachedFilesLayout";

    private String mChannelId;
    private List<Uri> mUriList;

    private AllUploadedListener mAllUploadedListener;

    private ProgressDialog mProgressDialog;

    AttachedFilesAdapter mAttachedFilesAdapter;

    BroadcastReceiver mBroadcastReceiver;

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
        getContext().unregisterReceiver(mBroadcastReceiver);
        super.onDetachedFromWindow();
    }

    private void init(Context context) {
        inflate(context, R.layout.attached_files_layout, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAttachedFilesAdapter = new AttachedFilesAdapter(getContext(), FileToAttachRepository.getInstance().getFilesForAttach());
        recyclerView.setAdapter(mAttachedFilesAdapter);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getExtras() != null) {
                    final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

                    if (ni != null && ni.isConnectedOrConnecting()) {
                        getPresenter().requestUploadFileToServer(mChannelId);
                        Log.i(TAG, "Network " + ni.getTypeName() + " connected");
                    } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                        Log.d(TAG, "There's no network connectivity");
                    }
                }
            }
        };
        getContext().registerReceiver(mBroadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public void addItems(List<Uri> uriList, String channelId) {
        Log.d(TAG, "addItems: " + uriList.size());
        this.mChannelId = channelId;
        if (this.mUriList == null) this.mUriList = new ArrayList<>();
        this.mUriList.addAll(uriList);
        uploadNext();
    }

    private void uploadNext() {
        Log.d(TAG, "uploadNext: ");
        if (mUriList.size() > 0) {
            addItem(mUriList.get(0));
            mUriList.remove(mUriList.get(0));
        }
    }

    private void addItem(Uri uri) {
        Log.d(TAG, "addItem: " + uri.toString());
        if(FileToAttachRepository.getInstance().getFilesForAttach().size() == FILE_TO_ATTACH_MAX){
            Toast.makeText(getContext(), String.format("%s %d", getContext().getString(R.string.too_much_files), FILE_TO_ATTACH_MAX + 1), Toast.LENGTH_SHORT).show();
            return;
        }
        String filePath = FileUtil.getInstance().getPath(uri);
        if (filePath == null) {
            new DownloadFile(getContext(), this).execute(uri);
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.show();
            mProgressDialog.setContentView(R.layout.data_processing_progress_layout);
        } else {
            uploadFileToServer(uri, filePath, mChannelId, false);
        }
    }

    private void uploadFileToServer(Uri uri, String filePath, String channelId, boolean isTemporaryFile) {
        Log.d(TAG, "uploadFileToServer: " + uri.toString());
        final File file = new File(filePath);
        if(file.length() > 1024 * 1024 * 50){
            Log.d(TAG, "file too big");
            Toast.makeText(getContext(), getContext().getString(R.string.file_too_big), Toast.LENGTH_SHORT).show();
            return;
        }
        if (file.exists()) {
            FileToAttachRepository.getInstance().add(new FileToAttach(file.getName(),
                    filePath,
                    uri.toString(),
                    UploadState.WAITING_FOR_UPLOAD,
                    isTemporaryFile));
            if (!FileToAttachRepository.getInstance().haveUploadingFile()) {
                getPresenter().requestUploadFileToServer(channelId);
            }
        } else {
            Log.d(TAG, "file doesn't exists");
            Toast.makeText(getContext(), getContext().getString(R.string.cannot_open_file_to_attach), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public List<String> getAttachedFiles() {
        List<String> fileNames = new ArrayList<>();
        for (FileToAttach fileToAttach : mAttachedFilesAdapter.getData()) {
            fileNames.add(fileToAttach.getIdFromServer());
        }
        return fileNames;
    }

    public void setEmptyListListener(AttachedFilesAdapter.EmptyListListener emptyListListener) {
        mAttachedFilesAdapter.setEmptyListListener(emptyListListener);
    }

    @Override
    public void onDownloadedFile(String filePath) {
        if (mProgressDialog != null) mProgressDialog.cancel();
        uploadFileToServer(Uri.parse(filePath), filePath, mChannelId, true);
    }

    @Override
    public void onError() {
        if(mProgressDialog != null) mProgressDialog.cancel();
    }

    public void showUploadErrorToast(String error) {
        Toast.makeText(getContext(), error /*getContext().getString(R.string.error_during_file_upload)*/, Toast.LENGTH_SHORT).show();
    }

    public void setmAllUploadedListener(AllUploadedListener mAllUploadedListener) {
        this.mAllUploadedListener = mAllUploadedListener;
    }

    public void onAllUploaded(){
        if(mAllUploadedListener != null) mAllUploadedListener.onAllUploaded();
    }

    public interface AllUploadedListener{
        void onAllUploaded();
    }
}
