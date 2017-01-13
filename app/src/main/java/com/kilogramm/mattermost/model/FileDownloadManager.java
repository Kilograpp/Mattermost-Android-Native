package com.kilogramm.mattermost.model;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfoRepository;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.tools.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kepar on 20.10.16.
 */

public class FileDownloadManager {
    private static final String TAG = "FileDownloaderManager";

    private static FileDownloadManager instance;

    private Map<String, FileDownloadListener> fileDownloadListeners;

    private DownloadManager manager;
    private long downloadId;
    private String fileId;

    public static FileDownloadManager getInstance() {
        if (instance == null) instance = new FileDownloadManager();
        return instance;
    }

    private FileDownloadManager() {
        fileDownloadListeners = new HashMap<>();
        manager = (DownloadManager) MattermostApp.getSingleton().
                getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void addItem(FileInfo fileInfo, FileDownloadListener fileDownloadListener) {
        fileDownloadListeners.put(fileInfo.getId(), fileDownloadListener);
        FileInfoRepository.getInstance().addForDownload(fileInfo);
        startDownload();
    }

    public void addItem(String fileId) {
        FileToAttachRepository.getInstance().addForDownload(fileId);
        startDownload();
    }

    private void startDownload() {
        if (!FileInfoRepository.getInstance().haveDownloadingFile()) {
            FileInfo fileInfo = FileInfoRepository.getInstance().getUndownloadedFile();
            if (fileInfo != null && fileInfo.isValid()) {
                FileInfoRepository.getInstance().updateUploadStatus(fileInfo.getId(), UploadState.DOWNLOADING);
                downloadFile(fileInfo);
            }
        }
    }

    private void downloadFile(FileInfo fileInfo) {
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

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/api/v3/files/"
                + fileId
                + "/get"));

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
                int bytes_downloaded;
                int bytes_total;
                try {
                    bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                } catch (CursorIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    cursor.close();
                    break;
                }

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                    downloading = false;
                    int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
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
                }

                if (bytes_total > 0) {
                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
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
    }

    public synchronized void stopDownloadCurrentFile(FileInfo fileInfo) {
        if (this.fileId != null && fileInfo != null && this.fileId.equals(fileInfo.getId())) {
            manager.remove(downloadId);
            FileUtil.getInstance().removeFile(FileUtil.getInstance().getDownloadedFilesDir()
                    + File.separator + fileInfo.getmName());
        }
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
