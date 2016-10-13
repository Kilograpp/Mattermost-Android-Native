package com.kilogramm.mattermost.model.entity.filetoattacth;

import io.realm.RealmObject;

/**
 * Created by kepar on 7.10.16.
 */
public class FileToAttach extends RealmObject {
    String fileName;
    String filePath;
    int progress;
    boolean isUploaded;

    public FileToAttach(String filePath) {
        this.filePath = filePath;
    }

    public FileToAttach(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public FileToAttach() {}

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }
}
