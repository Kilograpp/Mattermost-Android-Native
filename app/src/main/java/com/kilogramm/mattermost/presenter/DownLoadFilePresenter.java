package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.ui.DownloadFileControls;

import icepick.State;
import rx.schedulers.Schedulers;

/**
 * Created by kepar on 19.10.16.
 */
public class DownLoadFilePresenter extends BaseRxPresenter<DownloadFileControls> {
    private static final String TAG = "DownLoadFilePresenter";

    private static final int REQUEST_DOWNLOAD_FILE = 1;

    private ApiMethod service;

    @State
    String fileId;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        initRequests();
    }

    private void initRequests() {
        restartableFirst(REQUEST_DOWNLOAD_FILE, () -> service.downloadFile(fileId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()), (downloadFileControls, fileUploadResponse) -> {
            Log.d(TAG, "Success");
        }, (downloadFileControls1, throwable) -> {
            throwable.printStackTrace();
            Log.d(TAG, "Error");
        });
    }

    public void downloadFile(String fileId){
        this.fileId = fileId;
        start(REQUEST_DOWNLOAD_FILE);
    }

    public void stopDownload(){

    }

    private void sendHideFileAttachLayout(){
        /*createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.hideAttachedFilesLayout()));*/
    }
}
