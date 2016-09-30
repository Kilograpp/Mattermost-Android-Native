package com.kilogramm.mattermost.presenter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.model.entity.FileUploadResponse;
import com.kilogramm.mattermost.model.fromnet.ProgressRequestBody;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.tools.FileUtils;
import com.kilogramm.mattermost.ui.AttachedFilesLayout;

import java.io.File;
import java.util.ArrayList;

import nucleus.presenter.Presenter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by kepar on 30.9.16.
 */

public class AttachedFilesPresenter extends Presenter<AttachedFilesLayout> {

    private static final String TAG = "AttachedFilesPresenter";

    private MattermostApp mMattermostApp;
    private Subscription mSubscription;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        mMattermostApp = MattermostApp.getSingleton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
    }

    public void uploadFileToServer(Context context, String teamId, String channel_id, Uri uri){
        String filePath = FileUtils.getPath(context, uri);
        String mimeType = FileUtils.getMimeType(filePath);
        File file = new File(filePath);
        if(file.exists()) {
            ProgressRequestBody fileBody = new ProgressRequestBody(file, mimeType, new ProgressRequestBody.UploadCallbacks() {
                @Override
                public void onProgressUpdate(int percentage) {
                    Log.d(TAG, String.format("Progress: %d", percentage));
                }

                @Override
                public void onError() {

                }

                @Override
                public void onFinish() {

                }
            });
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("files", file.getName(), fileBody);
            RequestBody channelId = RequestBody.create(MediaType.parse("multipart/form-data"), channel_id);
            RequestBody clientId = RequestBody.create(MediaType.parse("multipart/form-data"), file.getName());

            if (mSubscription != null && !mSubscription.isUnsubscribed())
                mSubscription.unsubscribe();
            ApiMethod service = mMattermostApp.getMattermostRetrofitService();

            mSubscription = service.uploadFile(teamId, filePart, channelId, clientId)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.io())
                    .subscribe(new Subscriber<FileUploadResponse>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "Complete update last viewed at");
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error");
                        }

                        @Override
                        public void onNext(FileUploadResponse fileUploadResponse) {
                            Log.d(TAG, fileUploadResponse.toString());
                           /* if(fileNames == null) fileNames = new ArrayList<>();
                            fileNames.addAll(fileUploadResponse.getFilenames());*/
                        }
                    });
        } else {
            Log.e(TAG, "file not found");
        }
    }

}
