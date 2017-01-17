package com.kilogramm.mattermost.model;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

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

import io.realm.Realm;

/**
 * Created by kepar on 20.10.16.
 */
public class FileDownloadManager {
    private static final String TAG = "FileDownloaderManager";

    private static FileDownloadManager instance;

    /**
     * Every listener is for every added to download file. Key - file id,
     * value - an instance of {@link FileDownloadListener}
     */
    private Map<String, FileDownloadListener> fileDownloadListeners;

    private DownloadManager manager;
    private long downloadId;
    private String fileId;

    BroadcastReceiver receiver;

    public static FileDownloadManager getInstance() {
        if (instance == null) instance = new FileDownloadManager();
        return instance;
    }

    private FileDownloadManager() {
        fileDownloadListeners = new HashMap<>();
        manager = (DownloadManager) MattermostApp.getSingleton().
                getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    return;
                }
                context.getApplicationContext().unregisterReceiver(receiver);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor c = manager.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        Log.i(TAG, "downloaded file " + uriString);
                    } else {
                        Log.i(TAG, "download failed " + c.getInt(columnIndex));
                    }
                }
            }
        };
        MattermostApp.getSingleton().getApplicationContext()
                .registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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
                FileInfoRepository.getInstance().updateUploadStatus(fileInfo.getId(), UploadState.DOWNLOADING);
                downloadFile(Realm.getDefaultInstance().copyFromRealm(fileInfo));
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

        // TODO гавно на LG
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
//        request.setDestinationUri(Uri.fromFile(new File(FileUtil.getInstance().getDownloadedFilesDir() + File.separator + fileInfo.getmName())));

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
    }

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

    public synchronized void stopDownloadCurrentFile(FileInfo fileInfo) {
        if (this.fileId != null && fileInfo != null && this.fileId.equals(fileInfo.getId())) {
            manager.remove(downloadId);
            FileUtil.getInstance().removeFile(FileUtil.getInstance().getDownloadedFilesDir()
                    + File.separator + fileInfo.getmName());
        }
        startDownload();
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
