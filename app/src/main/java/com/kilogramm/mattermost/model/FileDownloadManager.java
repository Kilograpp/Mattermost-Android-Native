package com.kilogramm.mattermost.model;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.tools.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by kepar on 20.10.16.
 */

public class FileDownloadManager {
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

    public void addItem(String fileId, FileDownloadListener fileDownloadListener) {
        fileDownloadListeners.put(fileId, fileDownloadListener);
        FileToAttachRepository.getInstance().addForDownload(fileId);
        startDownload();
    }

    public void addItem(String fileId) {
        FileToAttachRepository.getInstance().addForDownload(fileId);
        startDownload();
    }

    public void startDownload() {
        if (!FileToAttachRepository.getInstance().haveDownloadingFile()) {
            FileToAttach fileToAttach = FileToAttachRepository.getInstance().getUndownloadedFile();
            if (fileToAttach != null && fileToAttach.isValid()) {
                FileToAttachRepository.getInstance().updateUploadStatus(fileToAttach.getFileName(), UploadState.DOWNLOADING);
                dowloadFile(fileToAttach.getFileName());
            }
        }
    }

    private void dowloadFile(String fileId) {
        this.fileId = fileId;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://"
                + MattermostPreference.getInstance().getBaseUrl()
                + "/"
                + "api/v3/teams/"
                + MattermostPreference.getInstance().getTeamId()
                + "/files/get"
                + fileId));

        request.setDescription(MattermostApp.getSingleton().getApplicationContext().getString(R.string.downloading));
        request.setTitle(FileUtil.getInstance().getFileNameFromIdDecoded(fileId));
        request.addRequestHeader("Authorization", "Bearer " + MattermostPreference.getInstance().getAuthToken());
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(new File(FileUtil.getInstance().getDownloadedFilesDir() + File.separator + FileUtil.getInstance().getFileNameFromIdDecoded(fileId))));

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
                } catch (CursorIndexOutOfBoundsException e){
                    e.printStackTrace();
                    cursor.close();
                    break;
                }

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                    downloading = false;
                    if (fileDownloadListeners.get(fileId) != null) {
                        fileDownloadListeners.get(fileId).onError(fileId);
                        FileToAttachRepository.getInstance().remove(fileId);
                    }
                    break;
                }

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false;
                    if (fileDownloadListeners.get(fileId) != null) {
                        fileDownloadListeners.get(fileId).onComplete(fileId);
                    }
                    FileToAttachRepository.getInstance().remove(fileId);
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

    public synchronized void stopDownloadCurrentFile(String fileId) {
        if(this.fileId.equals(fileId)) {
            manager.remove(downloadId);
            FileUtil.getInstance().removeFile(FileUtil.getInstance().getDownloadedFilesDir()
                    + File.separator + FileUtil.getInstance().getFileNameFromId(fileId));
        }
    }

    public void addListener(String fileId, FileDownloadListener fileDownloadListener){
        fileDownloadListeners.put(fileId, fileDownloadListener);
    }

    public interface FileDownloadListener {
        void onComplete(String fileId);

        void onProgress(int percantage);

        void onError(String fileId);
    }
}
