package com.kilogramm.mattermost.presenter;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.model.fromnet.ProgressRequestBody;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.tools.FileUtil;
import com.kilogramm.mattermost.ui.AttachedFilesLayout;

import java.io.File;

import icepick.State;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.schedulers.Schedulers;

/**
 * Created by kepar on 30.9.16.
 */

public class AttachedFilesPresenter extends BaseRxPresenter<AttachedFilesLayout> {
    private static final String TAG = "AttachedFilesPresenter";

    private static final int REQUEST_UPLOAD_TO_SERVER = 1;

    private ApiMethod service;

    @State
    String teamId;
    @State
    String channelId;
    @State
    String uri;
    @State
    String fileName;

    private RequestBody channel_Id;
    private RequestBody clientId;

    FileUtil fileUtil;



    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        fileUtil = FileUtil.getInstance();
        initRequests();
    }

    private void initRequests(){
        restartableFirst(REQUEST_UPLOAD_TO_SERVER,() -> {
            String filePath = fileUtil.getPath(Uri.parse(uri));
            String mimeType = fileUtil.getMimeType(filePath);

            File file = new File(filePath);
            this.fileName = file.getName();
            FileToAttachRepository.getInstance().add(new FileToAttach(filePath, file.getName()));
            ProgressRequestBody fileBody = new ProgressRequestBody(file, mimeType);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("files", file.getName(), fileBody);
            channel_Id = RequestBody.create(MediaType.parse("multipart/form-data"), channelId);
            clientId = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());

            return service.uploadFile(teamId, filePart, channel_Id, clientId)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.io());
        }, (attachedFilesLayout, fileUploadResponse) -> {
            Log.d(TAG, fileUploadResponse.toString());
            FileToAttachRepository.getInstance().updateName(fileName, fileUploadResponse.getFilenames().get(0));
            FileToAttachRepository.getInstance().updateUploadStatus(fileUploadResponse.getFilenames().get(0), true);
        }, (attachedFilesLayout1, throwable) -> {
            throwable.printStackTrace();
            Log.d(TAG, "Error");
        });
    }

    public void requestUploadFileToServer(String teamId, String channelId, String uri){
        this.teamId = teamId;
        this.channelId = channelId;
        this.uri = uri;
        start(REQUEST_UPLOAD_TO_SERVER);
    }

}
