package com.kilogramm.mattermost.model;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.network.ApiMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
//        FileToAttachRepository.getInstance().updateUploadStatus(fileId, UploadState.WAITING_FOR_DOWNLOAD);
        FileToAttachRepository.getInstance().addForDownload(fileId);
        startDownload();
    }

    public void startDownload() {
        if (!FileToAttachRepository.getInstance().haveDownloadingFile()) {
            FileToAttach fileToAttach = FileToAttachRepository.getInstance().getUndownloadedFile();
            if (fileToAttach != null && fileToAttach.isValid()) {
                String decodedName = null;
                try {
                    decodedName = URLDecoder.decode(fileToAttach.getFileName(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                dowloadFile(fileToAttach.getFileName(), decodedName);
            }
        }
    }

    private void dowloadFile(String fileId, String decodedName) {

// region commented
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
//endregion

        service.downloadFile(MattermostPreference.getInstance().getTeamId(), decodedName)
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
                        FileToAttachRepository.getInstance().updateUploadStatus(fileId, UploadState.DOWNLOADING);
                        Log.d(TAG, "onNext()");
                        InputStream input = null;
                        OutputStream output = null;
                        NotificationManager notificationManager = (NotificationManager) MattermostApp.getSingleton().getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(MattermostApp.getSingleton().getApplicationContext());
                        int id = 1;
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
                            Pattern pattern = Pattern.compile("\\/.*\\/(.*)");
                            Matcher matcher = pattern.matcher(fileId);
                            if (matcher.matches()) {
                                String fileName = matcher.group(1);
                                builder.setContentTitle(fileName).setSmallIcon(R.drawable.attach_img_icon);
                                builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloading));

                                output = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);
                                Log.d(TAG, "create file");
                                byte data[] = new byte[4096];
                                long total = 0;
                                int count;
                                while ((count = input.read(data)) != -1) {
                                    total += count;
                                    // publishing the progress....
                                    if (fileLength > 0) { // only if total length is known
                                        if(total % (4096 * 125) == 0) {
                                            builder.setProgress(100, (int) (total * 100 / fileLength), false);
                                            notificationManager.notify(id, builder.build());

                                            if(fileDownloadListeners.get(fileId) != null) {
                                                fileDownloadListeners.get(fileId).onProgress((int) (total * 100 / fileLength));
                                            }

                                            Log.d(TAG, String.valueOf(total * 100 / fileLength));
                                        }



/*                                        FileToAttachRepository.getInstance().
                                                updateProgress(fileId,
                                                        (int) (total * 100 / fileLength));*/

                                    }
                                    output.write(data, 0, count);
                                }
                            }
                            builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloaded)).setProgress(0, 0, false);
                            notificationManager.notify(id, builder.build());

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
