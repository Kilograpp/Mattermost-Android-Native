package com.kilogramm.mattermost.model;

import android.os.Environment;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.network.ApiMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by kepar on 20.10.16.
 */

public class FileDownloadManager {

    private static FileDownloadManager instance;

    private ApiMethod service;

    private Map<String, FileDownloadListener> fileDownloadListeners;

    public static FileDownloadManager getInstance() {
        if (instance == null) instance = new FileDownloadManager();
        return instance;
    }

    private FileDownloadManager() {
        service = MattermostApp.getSingleton().getMattermostRetrofitService();
        fileDownloadListeners = new HashMap<>();
    }

    public void addItem(String fileId, FileDownloadListener fileDownloadListener) {
        fileDownloadListeners.put(fileId, fileDownloadListener);
        FileToAttachRepository.getInstance().addForDownload(fileId);
        startDownload();
    }

    public void startDownload() {
        FileToAttach fileToAttach = FileToAttachRepository.getInstance().getUndownloadedFile();
        if (fileToAttach != null && fileToAttach.isValid()) {
            dowloadFile(fileToAttach.getFileName());
        }
    }

    private void dowloadFile(String fileId) {

        service.downloadFile(MattermostPreference.getInstance().getTeamId(), fileId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                        FileDownloadListener fileDownloadListener = fileDownloadListeners.get(fileId);
                        if(fileDownloadListener != null) {
                            fileDownloadListener.onComplete(fileId);
                        }
                        fileDownloadListeners.remove(fileId);
                        startDownload();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        InputStream input = null;
                        OutputStream output = null;
                        try {

//                int fileLength = connection.getContentLength();

                            // download the file
                            input = responseBody.byteStream();
                            File dir = new File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS) + File.separator + "Mattermost");
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            Pattern pattern = Pattern.compile("\\/\\w*\\.\\w*");
                            Matcher matcher = pattern.matcher(fileId);
                            if (matcher.find()) {


                                output = new FileOutputStream(dir.getAbsolutePath() + matcher.group());
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
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            FileToAttachRepository.getInstance().updateUploadStatus(fileId, UploadState.DOWNLOADED);
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
                    }
                });
    }

    public interface FileDownloadListener{
        void onComplete(String fileId);

        void onError(String fileId);
    }
}
