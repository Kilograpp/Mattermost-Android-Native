package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.ui.DownloadFileControls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        restartableFirst(REQUEST_DOWNLOAD_FILE, () -> service.downloadFile(MattermostPreference.getInstance().getTeamId(), fileId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io()), (downloadFileControls, responseBody) -> {

            InputStream input = null;
            OutputStream output = null;
            try{

//                int fileLength = connection.getContentLength();

                // download the file
                input = responseBody.byteStream();
                output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + File.separator + "Mattermost");
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    /*if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));*/
                    output.write(data, 0, count);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                    if (input != null) {
                        input.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
