package com.kilogramm.mattermost.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfoRepository;
import com.kilogramm.mattermost.tools.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.realm.Realm;

/**
 * Created by kepar on 20.10.16.
 */
public class FileDownloadManager {
    private static final String TAG = "FileDownloaderManager";

    private static final int UPDATE_TIME_PROGRESS_MS = 300;

    private static FileDownloadManager instance;

    /**
     * Every listener is for every added to download file. Key - file id,
     * value - an instance of {@link FileDownloadListener}
     */
    private Map<String, FileDownloadListener> fileDownloadListeners;

    /**
     * Every listener is for notification with progress in status bar. key is a file id,
     * value is a notification id.
     */
    private Map<String, Integer> mMapNotifications;

    //    private DownloadManager manager;
    //    private long downloadId;
    private String fileId;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    /**
     * This variable is to notice if current file downloading was canceled
     */
    private boolean isStopCurrent = false;

    public static FileDownloadManager getInstance() {
        if (instance == null) instance = new FileDownloadManager();
        return instance;
    }

    private FileDownloadManager() {
        fileDownloadListeners = new HashMap<>();
        mMapNotifications = new HashMap<>();
        notificationManager = (NotificationManager) MattermostApp.
                getSingleton().getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.
                Builder(MattermostApp.getSingleton().getApplicationContext());
    }

    /**
     * Add item for download. FileDownloadListener adds to {@link #fileDownloadListeners}
     *
     * @param fileInfo             the object contains information for file downloading
     * @param fileDownloadListener the listener to indicate downloading progress
     */
    public void addItem(FileInfo fileInfo, FileDownloadListener fileDownloadListener) {
        Log.d(TAG, "addItem: " + fileInfo.getId());
        fileDownloadListeners.put(fileInfo.getId(), fileDownloadListener);
        FileInfoRepository.getInstance().addForDownload(fileInfo);
        startDownload();
    }

    private void startDownload() {
        Log.d(TAG, "startDownload: ");
        // TODO может быть ситуациятакая ситуация: мы скачиваем файл, в очереди ждет еще один.
        // Отменяем первый. Но реалм еще может не успеть изменить статуст файла
        // с DOWNLOADING на IN_LIST, и тогда на условии ниже нас не пропустят
        if (!FileInfoRepository.getInstance().haveDownloadingFile()) {
            FileInfo fileInfo = FileInfoRepository.getInstance().getUndownloadedFile();
            if (fileInfo != null && fileInfo.isValid()) {
                Log.d(TAG, "startDownload: " + fileInfo.getId());
                FileInfoRepository.getInstance()
                        .updateUploadStatus(fileInfo.getId(), UploadState.DOWNLOADING);
                FileInfo fileInfoNotManagedByRealm = Realm.getDefaultInstance()
                        .copyFromRealm(fileInfo);
                new Thread(() -> {
                    downloadFile(fileInfoNotManagedByRealm);
                }).start();
            }
        }
    }

    // region DownloadManager
// TODO гавно на LG
   /* private void downloadFile(FileInfo fileInfo) {
        this.fileId = fileInfo.getId();

        final ConnectivityManager connectivityManager =
                (ConnectivityManager) MattermostApp.getSingleton()
                        .getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

        if (ni == null || !ni.isConnectedOrConnecting()) {
            onDownloadFail(fileId);
            startDownload();
            return;
        }

        String uri =  "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/files/"
                + fileId
                + "/get";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));

        request.setDescription(MattermostApp.getSingleton().getApplicationContext().getString(R.string.downloading));
        request.setTitle(fileInfo.getmName());
        request.addRequestHeader("Authorization", "Bearer " + MattermostPreference.getInstance().getAuthToken());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        File dir = new File(FileUtil.getInstance().getDownloadedFilesDir());
        if(!dir.exists()){
            dir.mkdirs();
        }
        request.setDestinationUri(Uri.fromFile(new File(FileUtil.getInstance().getDownloadedFilesDir() + File.separator + fileInfo.getmName())));

        downloadId = manager.enqueue(request);

        new Thread(() -> {

            boolean downloading = true;
            while (downloading) {

                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(downloadId);

                Cursor cursor = manager.query(q);
                cursor.moveToFirst();
                int bytes_downloaded = 0;
                long bytes_total = 0;
                try {
                    bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    bytes_total = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if(bytes_total <= 0){
                        bytes_total = fileInfo.getmSize();
                    }
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    cursor.close();
                    break;
                } catch (IllegalArgumentException e){
                    e.printStackTrace();
                    cursor.close();
                    break;
                }
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                    downloading = false;
                    cursor.close();
                    onDownloadFail(fileId);
                    break;
                } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false;
                    if (fileDownloadListeners.containsKey(fileId)) {
                        fileDownloadListeners.get(fileId).onComplete(fileId);
                    }
                    FileInfoRepository.getInstance().updateUploadStatus(fileId, UploadState.DOWNLOADED);
                    cursor.close();
                    startDownload();
                    break;
                }

                if (bytes_total > 0) {
                    final int dl_progress = (int) ((bytes_downloaded * 100L) / bytes_total);
                    if (fileDownloadListeners.get(fileId) != null) {
                        fileDownloadListeners.get(fileId).onProgress(dl_progress);
                    }
                }
                cursor.close();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }*/

    // endregion

    // region Kepar variant

        /*service.downloadFile(MattermostPreference.getInstance().getTeamId(), decodedName)
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
                        FileDownloadListener fileDownloadListener = fileDownloadListeners.get(fileId);
                        if (fileDownloadListener != null) {
                            fileDownloadListener.onError(fileId);
                        }
                        fileDownloadListeners.remove(fileId);
                        startDownload();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        FileToAttachRepository.getInstance().updateUploadStatus(fileId, UploadState.DOWNLOADING);
                        InputStream input = null;
                        OutputStream output = null;
                        NotificationManager notificationManager = (NotificationManager) MattermostApp.
                                getSingleton().getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder builder = new NotificationCompat.
                                Builder(MattermostApp.getSingleton().getApplicationContext());
                        int id = 1;
                        try {
                            long fileLength = responseBody.contentLength();
                            input = responseBody.byteStream();
                            File dir = new File(FileUtil.getInstance().getDownloadedFilesDir());
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            String fileName = FileUtil.getInstance().getFileNameFromId(fileId);
                            if (fileName != null) {
                                builder.setContentTitle(fileName).setSmallIcon(R.drawable.attach_img_icon);
                                builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloading));

                                output = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);
                                byte data[] = new byte[4096];
                                long total = 0;
                                int count;
                                while ((count = input.read(data)) != -1 && isWorkingForCurrentFile) {
                                    total += count;
                                    if (fileLength > 0) {
                                        if (total % (4096 * 125) == 0) {
                                            builder.setProgress(100, (int) (total * 100 / fileLength), false);
                                            notificationManager.notify(id, builder.build());
                                            if (fileDownloadListeners.get(fileId) != null) {
                                                fileDownloadListeners.get(fileId)
                                                        .onProgress((int) (total * 100 / fileLength));
                                            }
                                        }
                                    }
                                    output.write(data, 0, count);
                                }
                            }
                            builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloaded)).setProgress(0, 0, false);

                            Intent intent = FileUtil.getInstance().createOpenFileIntent(dir.getAbsolutePath() + File.separator + fileName);

                            if (intent.resolveActivityInfo(MattermostApp.getSingleton()
                                    .getApplicationContext().getPackageManager(), 0) != null) {
                                builder.setAutoCancel(true);
                                builder.setContentIntent(PendingIntent.getActivity(MattermostApp
                                        .getSingleton().getApplicationContext(), 0, intent, 0));
                            }
                            notificationManager.notify(id, builder.build());

                        } catch (IOException e) {
                            e.printStackTrace();
                            FileDownloadListener fileDownloadListener = fileDownloadListeners.get(fileId);
                            if (fileDownloadListener != null) {
                                fileDownloadListener.onError(fileId);
                            }
                            fileDownloadListeners.remove(fileId);
                            startDownload();
                            FileToAttachRepository.getInstance().updateUploadStatus(fileId, UploadState.IN_LIST);
                        } finally {
                            FileToAttachRepository.getInstance().remove(fileId);
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
                });*/
    // endregion

    // region new manual variant

    private void downloadFile(FileInfo fileInfo) {
        Log.d(TAG, "downloadFile: " + fileInfo.getId());
        isStopCurrent = false;
        this.fileId = fileInfo.getId();

        final ConnectivityManager connectivityManager =
                (ConnectivityManager) MattermostApp.getSingleton()
                        .getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

        if (ni == null || !ni.isConnectedOrConnecting()) {
            onDownloadFail(fileInfo.getId());
            startDownload();
            return;
        }

        createIdNotificationForFile(fileId);

        String uri = "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/files/"
                + fileInfo.getId()
                + "/get";
        InputStream input = null;
        OutputStream output = null;
        try {
            URL url = new URL(uri);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("Authorization", "Bearer " +
                    MattermostPreference.getInstance().getAuthToken());
            String contentLength = urlConnection.getHeaderField("Content-Length");
            long fileLength = Long.parseLong(contentLength);

            input = new BufferedInputStream(urlConnection.getInputStream());

            File dir = new File(FileUtil.getInstance().getDownloadedFilesDir());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = fileInfo.getmName();
            if (fileName != null) {
                Log.d(TAG, "downloadFile: fileName != null");
                builder.setContentTitle(fileName).setSmallIcon(R.drawable.ic_downloaded_file);
                builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloading));

                output = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);
                byte data[] = new byte[4096];
                long total = 0;
                long lastTimeUpdate = 0;
                int count;
                while ((count = input.read(data)) != -1 && !isStopCurrent) {
                    total += count;
                    if (fileLength > 0) {
                        if (System.currentTimeMillis() - lastTimeUpdate > UPDATE_TIME_PROGRESS_MS) {
                            Log.d(TAG, String.format("%s: %d/%d", "downloadFile", total, fileLength));
                            lastTimeUpdate = System.currentTimeMillis();
                            builder.setProgress(100, (int) (total * 100 / fileLength), false);
                            notificationManager.notify(mMapNotifications.get(fileInfo.getId()),
                                    builder.build());

                            if (fileDownloadListeners.get(fileInfo.getId()) != null) {
                                fileDownloadListeners.get(fileInfo.getId())
                                        .onProgress((int) (total * 100 / fileLength));
                            }
                        }
                    }
                    output.write(data, 0, count);
                }
            }
            urlConnection.disconnect();
            if (!isStopCurrent) {
                Log.d(TAG, "downloadFile: downloading completed successfully");
                fileDownloadListeners.get(fileInfo.getId()).onComplete(fileInfo.getId());
                FileInfoRepository.getInstance().updateUploadStatus(fileInfo.getId(), UploadState.DOWNLOADED);

                builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloaded)).setProgress(0, 0, false);

                Intent intent = FileUtil.getInstance().createOpenFileIntent(dir.getAbsolutePath() + File.separator + fileName);

                if (intent.resolveActivityInfo(MattermostApp.getSingleton()
                        .getApplicationContext().getPackageManager(), 0) != null) {
                    builder.setAutoCancel(true);
                    builder.setContentIntent(PendingIntent.getActivity(MattermostApp
                            .getSingleton().getApplicationContext(), 0, intent, 0));
                }
                notificationManager.notify(mMapNotifications.get(fileInfo.getId()), builder.build());
            } else {
                Log.d(TAG, "downloadFile: downloading was canceled");
                FileInfoRepository.getInstance().updateUploadStatus(fileInfo.getId(),
                        UploadState.IN_LIST);
                FileUtil.getInstance().removeFile(FileUtil.getInstance().getDownloadedFilesDir()
                        + File.separator + fileInfo.getmName());
                notificationManager.cancel(mMapNotifications.get(fileInfo.getId()));
                isStopCurrent = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            FileDownloadListener fileDownloadListener = fileDownloadListeners.get(fileInfo.getId());
            if (fileDownloadListener != null) {
                fileDownloadListener.onError(fileInfo.getId());
            }
            FileInfoRepository.getInstance().updateUploadStatus(fileInfo.getId(), UploadState.IN_LIST);
        } finally {
            Log.d(TAG, "downloadFile: finally");
            fileDownloadListeners.remove(fileInfo.getId());
            mMapNotifications.remove(fileInfo.getId());
            startDownload();
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

    private int createIdNotificationForFile(String fileId) {
        boolean flag = false;
        int id;
        do {
            Random r = new Random();
            id = r.nextInt();
            for (Map.Entry<String, Integer> entry : mMapNotifications.entrySet()) {
                flag = true;
                if (!entry.getValue().equals(id)) {
                    flag = false;
                    break;
                }
            }
        } while (flag);
        mMapNotifications.put(fileId, id);
        Log.d(TAG, "createIdNotificationForFile: " + id);
        return id;
    }

    // endregion

    public synchronized void stopDownloadCurrentFile(FileInfo fileInfo) {
        Log.d(TAG, "stopDownloadCurrentFile: ");
        isStopCurrent = true;
/*
        if (this.fileId != null && fileInfo != null && this.fileId.equals(fileInfo.getId())) {
            Log.d(TAG, "stopDownloadCurrentFile: " + fileInfo.getId());
//            manager.remove(downloadId);
        }
*/
    }

    public void addListener(FileInfo fileInfo, FileDownloadListener fileDownloadListener) {
        fileDownloadListeners.put(fileInfo.getId(), fileDownloadListener);
    }

    private void onDownloadFail(String fileId) {
        FileInfoRepository.getInstance().updateUploadStatus(fileId, UploadState.FAILED);
        if (fileDownloadListeners.get(fileId) != null) {
            fileDownloadListeners.get(fileId).onError(fileId);
        }
    }

    public interface FileDownloadListener {
        void onComplete(String fileId);

        void onProgress(int percantage);

        void onError(String fileId);
    }
}
