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

    /**
     * Time period for updating progress of downloading
     */
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
        fileDownloadListeners.put(fileInfo.getId(), fileDownloadListener);
        FileInfoRepository.getInstance().addForDownload(fileInfo);
        startDownload();
    }

    private void startDownload() {
        if (!FileInfoRepository.getInstance().haveDownloadingFile()) {
            FileInfo fileInfo = FileInfoRepository.getInstance().getUndownloadedFile();
            if (fileInfo != null && fileInfo.isValid()) {
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

    private void downloadFile(FileInfo fileInfo) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) MattermostApp.getSingleton()
                        .getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

        if (ni == null || !ni.isConnectedOrConnecting()) {
            onDownloadFail(fileInfo.getId());
            startDownload();
            return;
        }

        createIdNotificationForFile(fileInfo.getId());

        String uri = "https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/files/"
                + fileInfo.getId()
                + "/get";
        InputStream input = null;
        OutputStream output = null;
        File dir = new File(FileUtil.getInstance().getDownloadedFilesDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            String fileName = fileInfo.getmName();
            if (fileName != null) {
                showIndeterminateProgressNotification(fileInfo);

                URL url = new URL(uri);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.addRequestProperty("Authorization", "Bearer " +
                        MattermostPreference.getInstance().getAuthToken());

                input = new BufferedInputStream(urlConnection.getInputStream());
                output = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);

                String contentLength = urlConnection.getHeaderField("Content-Length");
                writeData(input, output, fileInfo.getId(), Long.parseLong(contentLength));

                urlConnection.disconnect();
            }
            if (!isStopCurrent) {
                fileDownloadListeners.get(fileInfo.getId()).onComplete(fileInfo.getId());
                FileInfoRepository.getInstance()
                        .updateUploadStatus(fileInfo.getId(), UploadState.DOWNLOADED);
                showDownloadedNotification(dir, fileInfo);
            } else {
                FileInfoRepository.getInstance().updateUploadStatus(fileInfo.getId(),
                        UploadState.IN_LIST);
                FileUtil.getInstance().removeFile(FileUtil.getInstance().getDownloadedFilesDir()
                        + File.separator + fileInfo.getmName());
                notificationManager.cancel(mMapNotifications.get(fileInfo.getId()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            isStopCurrent = false;
            showErrorNotification(fileInfo);
            FileDownloadListener fileDownloadListener = fileDownloadListeners.get(fileInfo.getId());
            if (fileDownloadListener != null) {
                fileDownloadListener.onError(fileInfo.getId());
            }
            FileInfoRepository.getInstance().updateUploadStatus(fileInfo.getId(), UploadState.IN_LIST);
        } finally {
            isStopCurrent = false;
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
        boolean flag = true;
        int id;
        do {
            Random r = new Random();
            id = r.nextInt();
            for (Map.Entry<String, Integer> entry : mMapNotifications.entrySet()) {
                if (entry.getValue().equals(id)) {
                    break;
                }
                flag = false;
            }
        } while (flag);
        mMapNotifications.put(fileId, id);
        return id;
    }

    private void showIndeterminateProgressNotification(FileInfo fileInfo) {
        builder.setContentTitle(fileInfo.getmName()).setSmallIcon(R.drawable.ic_downloaded_file);
        builder.setContentText(MattermostApp.getSingleton()
                .getString(R.string.prepare_for_download));
        builder.setProgress(0, 0, true);
        notificationManager.notify(mMapNotifications.get(fileInfo.getId()),
                builder.build());
    }

    private void showDownloadedNotification(File dir, FileInfo fileInfo){
        builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloaded))
                .setProgress(0, 0, false);
        // Intent to open file on tap on the notification
        Intent intent = FileUtil.getInstance()
                .createOpenFileIntent(dir.getAbsolutePath() + File.separator + fileInfo.getmName());

        if (intent.resolveActivityInfo(MattermostApp.getSingleton()
                .getApplicationContext().getPackageManager(), 0) != null) {
            builder.setAutoCancel(true);
            builder.setContentIntent(PendingIntent.getActivity(MattermostApp
                    .getSingleton().getApplicationContext(), 0, intent, 0));
        }
        notificationManager.notify(mMapNotifications.get(fileInfo.getId()), builder.build());
    }

    private void showErrorNotification(FileInfo fileInfo) {
        builder.setContentText(MattermostApp.getSingleton()
                .getString(R.string.error_during_file_download));
        builder.setProgress(0, 0, false);
        notificationManager.notify(mMapNotifications.get(fileInfo.getId()),
                builder.build());
    }

    /**
     * Write data to the specified file
     *
     * @param input      InputStream object with data from urlConnection
     * @param output     OutputStream object, that will be write data to the specified file
     * @param fileId     id of downloading file
     * @param fileLength file size in bytes
     * @throws IOException
     */
    private void writeData(InputStream input,
                           OutputStream output,
                           String fileId,
                           long fileLength) throws IOException {
        byte data[] = new byte[4096];
        long total = 0;
        long lastTimeUpdate = 0;
        int count;
        while ((count = input.read(data)) != -1 && !isStopCurrent) {
            total += count;
            if (fileLength > 0) {
                if (System.currentTimeMillis() - lastTimeUpdate > UPDATE_TIME_PROGRESS_MS) {
                    builder.setProgress(100, (int) (total * 100 / fileLength), false);
                    notificationManager.notify(mMapNotifications.get(fileId),
                            builder.build());

                    if (fileDownloadListeners.get(fileId) != null) {
                        fileDownloadListeners.get(fileId)
                                .onProgress((int) (total * 100 / fileLength));
                    }
                    lastTimeUpdate = System.currentTimeMillis();
                }
            }
            output.write(data, 0, count);
        }
    }

    public synchronized void stopDownloadCurrentFile() {
        Log.d(TAG, "stopDownloadCurrentFile: ");
        isStopCurrent = true;
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
