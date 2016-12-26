package com.kilogramm.mattermost.model.fromnet;

import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;
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
    private long fileId;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 32;
    private static final int UPDATE_TIME_PROGRESS_MS = 300;

    public ProgressRequestBody(final File file, String mediaType, long id) {
        mFile = file;
        if (mediaType != null && mediaType.length() > 0) {
            mMediaType = mediaType;
        }
        fileId = id;
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
        long lastTimeUpdate = 0;
        FileToAttach fileToAttach = FileToAttachRepository.getInstance().get(fileId);
        try (FileInputStream in = new FileInputStream(mFile)) {
            int read;
            if (fileLength == 0) {
                FileToAttachRepository.getInstance().updateProgress(mFile.getName(), 100);
            }
            while ((read = in.read(buffer)) != -1 && fileToAttach != null && fileToAttach.isValid()) {
                uploaded += read;
                if(System.currentTimeMillis() - lastTimeUpdate > UPDATE_TIME_PROGRESS_MS){
                    lastTimeUpdate = System.currentTimeMillis();
                    FileToAttachRepository.getInstance().updateProgress(mFile.getName(), (int) (100 * uploaded / fileLength));
                }
                sink.write(buffer, 0, read);
            }
            if(fileToAttach != null && fileToAttach.isValid()) {
                FileToAttachRepository.getInstance().updateProgress(mFile.getName(), 100);
            }
        }
    }
}
