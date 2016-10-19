package com.kilogramm.mattermost.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.rxtest.BaseRxPresenter;
import com.kilogramm.mattermost.ui.DownloadFileControls;

/**
 * Created by kepar on 19.10.16.
 */

public class DownLoadFilePresenter extends BaseRxPresenter<DownloadFileControls> {
    private static final String TAG = "DownLoadFilePresenter";

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
    }

    public void downloadFile(){

    }

    public void stopDownload(){

    }

    private void sendHideFileAttachLayout(){
        /*createTemplateObservable(new Object()).subscribe(split((chatRxFragment, o) ->
                chatRxFragment.hideAttachedFilesLayout()));*/
    }
}
