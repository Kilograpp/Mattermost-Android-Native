package com.kilogramm.mattermost.model.fromnet;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttachRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by kepar on 27.9.16.
 */

public class ProgressRequestBody extends RequestBody {
    private File mFile;
    private String mMediaType = "*"; // also can use: "application/octet-stream"
    private UploadCallbacks mListener;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 32;

    FileToAttachRepository fileToAttachRepository;

    public ProgressRequestBody(final File file, String mediaType, final  UploadCallbacks listener) {
        this(file, mediaType);
        mListener = listener;
    }

    public ProgressRequestBody(final File file, String mediaType) {
        mFile = file;
        if(mediaType != null && mediaType.length() > 0) {
            mMediaType = mediaType;
        }
        fileToAttachRepository = new FileToAttachRepository();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(mMediaType);
    }

    @Override
    public long contentLength() throws IOException {
        return mFile.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long uploaded = 0;

        try (FileInputStream in = new FileInputStream(mFile)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                // update progress on UI thread
//                handler.post(new ProgressUpdater(uploaded, fileLength));
                uploaded += read;
                fileToAttachRepository.updateProgress(mFile.getName(), (int) (100 * uploaded / fileLength));
                sink.write(buffer, 0, read);
            }
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;

        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            mListener.onProgressUpdate((int)(100 * mUploaded / mTotal));
        }
    }

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
        void onError();
        void onFinish();
    }
}
