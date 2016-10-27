package com.kilogramm.mattermost.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

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

    private static final String TAG = "FileDownloadManager";

    private static FileDownloadManager instance;

    private ApiMethod service;

    private Map<String, FileDownloadListener> fileDownloadListeners;

    private boolean isWorkingForCurrentFile = true;

    private String currentFileId;

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
        isWorkingForCurrentFile = true;
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

// region Kepar variant

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

                            File file = new File(dir.getAbsolutePath() + fileName);
                            String mimeType = FileUtil.getInstance().getMimeType(file.getAbsolutePath());

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(file), mimeType == null || mimeType.equals("")
                                    ? "**/*//*" : mimeType);

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
                });

        //endregion

        //region Kraftu variant

//        new DownloadFileWithProgress(fileId, decodedName).execute();

        // endregion
    }

    //region Kraftu variant

//    private void writeResponseBodyToDisk(String fileId, ResponseBody responseBody) {
//        FileToAttachRepository.getInstance().updateUploadStatus(fileId, UploadState.DOWNLOADING);
//        InputStream input = null;
//        OutputStream output = null;
//        NotificationManager notificationManager = (NotificationManager) MattermostApp.
//                getSingleton().getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationCompat.Builder builder = new NotificationCompat.
//                Builder(MattermostApp.getSingleton().getApplicationContext());
//        int id = 1;
//        try {
//            long fileLength = responseBody.contentLength();
//            input = responseBody.byteStream();
//            File dir = new File(FileUtil.getInstance().getDownloadedFilesDir());
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//            String fileName = FileUtil.getInstance().getFileNameFromId(fileId);
//            if (fileName != null) {
//                builder.setContentTitle(fileName).setSmallIcon(R.drawable.attach_img_icon);
//                builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloading));
//
//                output = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);
//                byte data[] = new byte[4096];
//                long total = 0;
//                int count;
//                while ((count = input.read(data)) != -1 && isWorkingForCurrentFile) {
//                    total += count;
//                    if (fileLength > 0) {
//                        if (total % (4096 * 125) == 0) {
//                            builder.setProgress(100, (int) (total * 100 / fileLength), false);
//                            notificationManager.notify(id, builder.build());
//                            if (fileDownloadListeners.get(fileId) != null) {
//                                fileDownloadListeners.get(fileId)
//                                        .onProgress((int) (total * 100 / fileLength));
//                            }
//                        }
//                    }
//                    output.write(data, 0, count);
//                }
//            }
//            builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloaded)).setProgress(0, 0, false);
//
//            File file = new File(dir.getAbsolutePath() + fileName);
//            String mimeType = FileUtil.getInstance().getMimeType(file.getAbsolutePath());
//
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(Uri.fromFile(file), mimeType == null || mimeType.equals("")
//                    ? "*/*" : mimeType);
//
//            if (intent.resolveActivityInfo(MattermostApp.getSingleton()
//                    .getApplicationContext().getPackageManager(), 0) != null) {
//                builder.setAutoCancel(true);
//                builder.setContentIntent(PendingIntent.getActivity(MattermostApp
//                        .getSingleton().getApplicationContext(), 0, intent, 0));
//            }
//            notificationManager.notify(id, builder.build());
//        } catch (IOException e) {
//            e.printStackTrace();
//            FileDownloadListener fileDownloadListener = fileDownloadListeners.get(fileId);
//            if (fileDownloadListener != null) {
//                fileDownloadListener.onError(fileId);
//            }
//            fileDownloadListeners.remove(fileId);
//            startDownload();
//            FileToAttachRepository.getInstance().updateUploadStatus(fileId, UploadState.IN_LIST);
//        } finally {
//            FileToAttachRepository.getInstance().remove(fileId);
//            try {
//                if (output != null) {
//                    output.close();
//                }
//                if (input != null) {
//                    input.close();
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
//
//    class DownloadFileWithProgress extends AsyncTask<Void, Void, Void> {
//
//        String fileId;
//        String decodedName;
//
//        public DownloadFileWithProgress(String fileId, String decodedName) {
//            this.fileId = fileId;
//            this.decodedName = decodedName;
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            Call<ResponseBody> call = service.downloadFile(MattermostPreference.getInstance().getTeamId(), decodedName);
//            Response<ResponseBody> response = null;
//            try {
//                response = call.execute();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            ResponseBody responseBody = response.body();
//            FileToAttachRepository.getInstance().updateUploadStatus(fileId, UploadState.DOWNLOADING);
//            InputStream input = null;
//            OutputStream output = null;
//            NotificationManager notificationManager = (NotificationManager) MattermostApp.
//                    getSingleton().getSystemService(Context.NOTIFICATION_SERVICE);
//            NotificationCompat.Builder builder = new NotificationCompat.
//                    Builder(MattermostApp.getSingleton().getApplicationContext());
//            int id = 1;
//            try {
//                long fileLength = responseBody.contentLength();
//                input = responseBody.byteStream();
//                File dir = new File(FileUtil.getInstance().getDownloadedFilesDir());
//                if (!dir.exists()) {
//                    dir.mkdirs();
//                }
//                String fileName = FileUtil.getInstance().getFileNameFromId(fileId);
//                if (fileName != null) {
//                    builder.setContentTitle(fileName).setSmallIcon(R.drawable.attach_img_icon);
//                    builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloading));
//
//                    output = new FileOutputStream(dir.getAbsolutePath() + File.separator + fileName);
//                    byte data[] = new byte[4096];
//                    long total = 0;
//                    int count;
//                    while ((count = input.read(data)) != -1 && isWorkingForCurrentFile) {
//                        total += count;
//                        if (fileLength > 0) {
//                            if (total % (4096 * 125) == 0) {
//                                builder.setProgress(100, (int) (total * 100 / fileLength), false);
//                                notificationManager.notify(id, builder.build());
//                                if (fileDownloadListeners.get(fileId) != null) {
//                                    fileDownloadListeners.get(fileId)
//                                            .onProgress((int) (total * 100 / fileLength));
//                                }
//                            }
//                        }
//                        output.write(data, 0, count);
//                    }
//                }
//                builder.setContentText(MattermostApp.getSingleton().getString(R.string.downloaded)).setProgress(0, 0, false);
//
//                File file = new File(dir.getAbsolutePath() + fileName);
//                String mimeType = FileUtil.getInstance().getMimeType(file.getAbsolutePath());
//
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(file), mimeType == null || mimeType.equals("")
//                        ? "*/*" : mimeType);
//
//                if (intent.resolveActivityInfo(MattermostApp.getSingleton()
//                        .getApplicationContext().getPackageManager(), 0) != null) {
//                    builder.setAutoCancel(true);
//                    builder.setContentIntent(PendingIntent.getActivity(MattermostApp
//                            .getSingleton().getApplicationContext(), 0, intent, 0));
//                }
//                notificationManager.notify(id, builder.build());
//            } catch (IOException e) {
//                e.printStackTrace();
//                FileDownloadListener fileDownloadListener = fileDownloadListeners.get(fileId);
//                if (fileDownloadListener != null) {
//                    fileDownloadListener.onError(fileId);
//                }
//                fileDownloadListeners.remove(fileId);
//                startDownload();
//                FileToAttachRepository.getInstance().updateUploadStatus(fileId, UploadState.IN_LIST);
//            } finally {
//                FileToAttachRepository.getInstance().remove(fileId);
//                try {
//                    if (output != null) {
//                        output.close();
//                    }
//                    if (input != null) {
//                        input.close();
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            return null;
//
//        }
//    }


    //endregion

    public synchronized void stopDownloadCurrentFile(String fileId) {
        if (currentFileId != null && fileId.equals(currentFileId)) {
            isWorkingForCurrentFile = false;
        }
        FileUtil.getInstance().removeFile(FileUtil.getInstance().getDownloadedFilesDir()
                + File.separator + FileUtil.getInstance().getFileNameFromId(fileId));
    }

    public interface FileDownloadListener {
        void onComplete(String fileId);

        void onProgress(int percantage);

        void onError(String fileId);
    }
}
