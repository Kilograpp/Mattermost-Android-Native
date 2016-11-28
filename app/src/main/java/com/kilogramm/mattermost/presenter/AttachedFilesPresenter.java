package com.kilogramm.mattermost.presenter;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.UploadState;
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
    String channelId;
    @State
    String fileName;
    FileToAttach fileToAttach;

    private RequestBody channel_Id;
    private RequestBody clientId;

    FileUtil fileUtil;

    public void requestUploadFileToServer(String channelId) {
        this.channelId = channelId;
        startRequest();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        fileUtil = FileUtil.getInstance();
        initRequests();
    }

    private void initRequests() {
        restartableFirst(REQUEST_UPLOAD_TO_SERVER, () -> {
//            sendShowToast("Loading start");
            String filePath = fileUtil.getPath(Uri.parse(fileToAttach.getUriAsString()));
            String mimeType = fileUtil.getMimeType(filePath);

            File file;
            if (filePath != null) {
                file = new File(filePath);
            } else {
                file = new File(fileToAttach.getUriAsString());
            }
            this.fileName = file.getName();
            if(file.exists()){
                Log.d(TAG, "initRequests: file exists");
            }
            FileToAttachRepository.getInstance().updateUploadStatus(fileToAttach.getId(), UploadState.UPLOADING);
            ProgressRequestBody fileBody = new ProgressRequestBody(file, mimeType);

            MultipartBody.Part filePart = MultipartBody.Part.createFormData("files", file.getName(), fileBody);

            channel_Id = RequestBody.create(MediaType.parse("multipart/form-data"), channelId);
            clientId = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());

            return service.uploadFile(MattermostPreference.getInstance().getTeamId(), filePart, channel_Id, clientId)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());
        }, (attachedFilesLayout, fileUploadResponse) -> {
            if (fileUploadResponse.getFilenames()!=null
                    && fileUploadResponse.getFilenames().size() != 0) {
//                sendShowToast("Loading complete");
                Log.d(TAG, fileUploadResponse.toString());
                FileToAttachRepository.getInstance().updateName(fileName, fileUploadResponse.getFilenames().get(0));
                FileToAttachRepository.getInstance().updateUploadStatus(fileUploadResponse.getFilenames().get(0), UploadState.UPLOADED);
                FileToAttach fileToAttach = FileToAttachRepository.getInstance().get(fileUploadResponse.getFilenames().get(0));
                if (fileToAttach != null && fileToAttach.isTemporary()) {
                    FileUtil.getInstance().removeFile(fileToAttach.getFilePath());
                }
            }
            startRequest();
        }, (attachedFilesLayout1, throwable) -> {
            throwable.printStackTrace();
            sendShowUploadErrorToast("");
            Log.d(TAG, "Error");
            FileToAttachRepository.getInstance().remove(fileName);
            startRequest();
        });
    }

    public void sendShowToast(String log){
        createTemplateObservable(log)
                .subscribe(split(AttachedFilesLayout::showToast));
    }

    public void sendShowUploadErrorToast(String log){
        createTemplateObservable(log)
                .subscribe(split(AttachedFilesLayout::showUploadErrorToast));
    }

    private void startRequest() {
        fileToAttach = FileToAttachRepository.getInstance().getUnloadedFile();
        if (fileToAttach == null || channelId == null) return;
        start(REQUEST_UPLOAD_TO_SERVER);
    }
}
