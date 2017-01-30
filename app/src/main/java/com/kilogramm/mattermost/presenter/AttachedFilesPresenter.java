package com.kilogramm.mattermost.presenter;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfoRepository;
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

    private ApiMethod mService;

    @State
    String channelId;
    @State
    String fileName;
    private FileToAttach mFileToAttach;
    private long mCurrentFileId;

    private RequestBody mChannelIdBody;
    private RequestBody mClientIdBody;

    private FileUtil mFileUtil;

    public void requestUploadFileToServer(String channelId) {
        Log.d(TAG, "requestUploadFileToServer: ");
        this.channelId = channelId;
        startRequest();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mService = MattermostApp.getSingleton().getMattermostRetrofitService();
        mFileUtil = FileUtil.getInstance();
        initRequests();
    }

    private void initRequests() {
        restartableFirst(REQUEST_UPLOAD_TO_SERVER, () -> {
            Log.d(TAG, "initRequests: ");
            String filePath = mFileUtil.getPath(Uri.parse(mFileToAttach.getUriAsString()));
            String mimeType = mFileUtil.getMimeType(filePath);

            File file;
            if (filePath != null) {
                file = new File(filePath);
            } else {
                file = new File(mFileToAttach.getUriAsString());
            }
            this.fileName = file.getName();
            if (file.exists()) {
                Log.d(TAG, "initRequests: file exists");
            }
            FileToAttachRepository.getInstance().updateUploadStatus(mFileToAttach.getId(), UploadState.UPLOADING);
            ProgressRequestBody fileBody = new ProgressRequestBody(file, mimeType, mFileToAttach.getId());

            MultipartBody.Part filePart = MultipartBody.Part.createFormData("files", file.getName(), fileBody);

            mChannelIdBody = RequestBody.create(MediaType.parse("multipart/form-data"), channelId);
            mClientIdBody = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());

            return mService.uploadFile(MattermostPreference.getInstance().getTeamId(), filePart, mChannelIdBody, mClientIdBody)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation());
        }, (attachedFilesLayout, fileUploadResponse) -> {
            Log.d(TAG, "initRequests: success");
            if (fileUploadResponse.getFile_infos() != null
                    && fileUploadResponse.getFile_infos().size() != 0) {
                Log.d(TAG, fileUploadResponse.toString());
                FileInfo fileInfo = fileUploadResponse.getFile_infos().get(0);
                FileToAttachRepository.getInstance().updateName(mCurrentFileId, fileInfo.getmName());
                FileToAttachRepository.getInstance().updateUploadStatus(mCurrentFileId,
                                UploadState.UPLOADED);
                FileToAttachRepository.getInstance().updateIdFromServer(mCurrentFileId,
                        fileInfo.getId());
                FileInfoRepository.getInstance().add(fileInfo);
            }
            startRequest();
        }, (attachedFilesLayout1, throwable) -> {
            throwable.printStackTrace();
            // TODO обработать различные ошибки (чтобы при отмене ничего не выводилось)
            if (!throwable.getMessage().trim().equals("unexpected end of stream")) {
                String error = parceError(throwable, UPLOAD_A_FILE);
                if (error != null) {
                    sendShowError(error);
                }
            } else {
                sendShowError("Cannot upload a file");
            }
            FileToAttachRepository.getInstance().remove(mCurrentFileId);
            startRequest();
        });
    }

    private void sendShowError(String error) {
        createTemplateObservable(error).subscribe(split((attachedFilesLayout, o)
                -> attachedFilesLayout.showUploadErrorToast(error)));
    }

    private void sendAllUploaded() {
        createTemplateObservable(new Object())
                .subscribe(split((attachedFilesLayout, o)
                        -> attachedFilesLayout.onAllUploaded()));
    }

    private void startRequest() {
        mFileToAttach = FileToAttachRepository.getInstance().getUnloadedFile();
        if (mFileToAttach == null || channelId == null) {
            // TODO тормозит, сделать асинхронно
            if (FileToAttachRepository.getInstance().haveFilesToAttach()) {
                sendAllUploaded();
            }
            return;
        } else {
            mCurrentFileId = mFileToAttach.getId();
        }
        start(REQUEST_UPLOAD_TO_SERVER);
    }
}
