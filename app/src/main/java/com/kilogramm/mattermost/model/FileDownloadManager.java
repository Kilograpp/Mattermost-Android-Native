package com.kilogramm.mattermost.model;

import android.os.Environment;
import android.util.Log;

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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by kepar on 20.10.16.
 */

public class FileDownloadManager {

    private static final String TAG = "FileDownloadManager";

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


        /*Observable.create(new Observable.OnSubscribe<retrofit2.Call>() {
            @Override
            public void call(Subscriber<? super retrofit2.Call> subscriber) {
                subscriber.onNext(service.downloadFile(MattermostPreference.getInstance().getTeamId(), fileId));
            }
        })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<retrofit2.Call>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(retrofit2.Call call) {
                        Log.d(TAG, "onNext()");
                        call.enqueue(new retrofit2.Callback() {
                            @Override
                            public void onResponse(retrofit2.Call call, retrofit2.Response response) {
                                Log.d(TAG, "onResponse()");
                                InputStream input = null;
                                OutputStream output = null;
                                try {
//                                                 BufferedSource bufferedSource = responseBody.source();
                                    ResponseBody responseBody = (ResponseBody) response.body();

                                    long fileLength = responseBody.contentLength();

                                    // download the file
                                    input = responseBody.byteStream();
                                    File dir = new File(Environment.getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_DOWNLOADS) + File.separator + "Mattermost");
                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }
                                    Log.d(TAG, "create dir");
                                    Pattern pattern = Pattern.compile("\\/\\w*\\.\\w*");
                                    Matcher matcher = pattern.matcher(fileId);
                                    if (matcher.find()) {
                                        output = new FileOutputStream(dir.getAbsolutePath() + matcher.group());
                                        Log.d(TAG, "create file");
                                        byte data[] = new byte[4096];
                                        long total = 0;
                                        int count;
                                        while ((count = input.read(data)) != -1) {
                                            total += count;
                                            // publishing the progress....
                                            if (fileLength > 0) // only if total length is known
                                                Log.d(TAG, String.valueOf(total * 100 / fileLength));
                                                FileToAttachRepository.getInstance().
                                                        updateProgress(fileId,
                                                                (int) (total * 100 / fileLength));
                                            if (fileDownloadListeners.get(fileId) != null) {
                                                fileDownloadListeners.get(fileId).onProgress((int) (total * 100 / fileLength));
                                            }
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

                            @Override
                            public void onFailure(retrofit2.Call call, Throwable t) {

                            }
                        });
                    }
                });*/



        service.downloadFile(MattermostPreference.getInstance().getTeamId(), fileId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                        FileDownloadListener fileDownloadListener = fileDownloadListeners.get(fileId);
                        if (fileDownloadListener != null) {
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
                        Log.d(TAG, "onNext()");
                        InputStream input = null;
                        OutputStream output = null;
                        try {
//                            BufferedSource bufferedSource = responseBody.source();

                            long fileLength = responseBody.contentLength();

                            // download the file
                            input = responseBody.byteStream();
                            File dir = new File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DOWNLOADS) + File.separator + "Mattermost");
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            Log.d(TAG, "create dir");
                            Pattern pattern = Pattern.compile("\\/\\w*\\.\\w*");
                            Matcher matcher = pattern.matcher(fileId);
                            if (matcher.find()) {
                                output = new FileOutputStream(dir.getAbsolutePath() + matcher.group());
                                Log.d(TAG, "create file");
                                byte data[] = new byte[4096];
                                long total = 0;
                                int count;
                                while ((count = input.read(data)) != -1) {
                                    total += count;
                                    // publishing the progress....
                                    if (fileLength > 0) // only if total length is known
                                        Log.d(TAG, String.valueOf(total * 100 / fileLength));
                                            FileToAttachRepository.getInstance().
                                                    updateProgress(fileId,
                                                            (int) (total * 100 / fileLength));
                                        if(fileDownloadListeners.get(fileId) != null) {
                                            fileDownloadListeners.get(fileId).onProgress((int) (total * 100 / fileLength));
                                        }
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

    public interface FileDownloadListener {
        void onComplete(String fileId);

        void onProgress(int percantage);

        void onError(String fileId);
    }
}
