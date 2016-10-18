package com.kilogramm.mattermost.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private ProgressDialog progressDialog;

    AttachedFilesAdapter attachedFilesAdapter;

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

    /*@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AttachedFilesLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }*/

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void init(Context context) {
        inflate(context, R.layout.attached_files_layout, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        attachedFilesAdapter = new AttachedFilesAdapter(getContext(), FileToAttachRepository.getInstance().query());
        recyclerView.setAdapter(attachedFilesAdapter);
    }

    public void addItem(List<Uri> uriList, String teamId, String channelId){
        this.teamId = teamId;
        this.channelId = channelId;
        for (Uri uri : uriList) {
            String filePath = FileUtil.getInstance().getPath(uri);
            if(filePath == null){
                /*Toast.makeText(getContext(), getContext().getString(R.string.cannot_open_file_to_attach), Toast.LENGTH_SHORT).show();
                return;*/
                new DownloadFile(getContext(), this).execute(uri);
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setView(inflate(getContext(), R.layout.data_processing_progress_layout, null));
                progressDialog.show();
            } else {
                uploadFileToServer(uri, filePath, teamId, channelId);
            }
        }
    }

    private void uploadFileToServer(Uri uri, String filePath, String teamId, String channelId){
        final File file = new File(filePath);
        if (file.exists()) {
            getPresenter().requestUploadFileToServer(teamId, channelId, uri.toString());
        } else {
            Log.d(TAG, "file doesn't exists");
            Toast.makeText(getContext(), getContext().getString(R.string.cannot_open_file_to_attach), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public List<String> getAttachedFiles(){
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
        if(progressDialog != null) progressDialog.cancel();
        uploadFileToServer(Uri.parse(filePath), filePath, teamId, channelId);
    }
}
