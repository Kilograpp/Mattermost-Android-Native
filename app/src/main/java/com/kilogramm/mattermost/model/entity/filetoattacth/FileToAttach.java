package com.kilogramm.mattermost.model.entity.filetoattacth;

import com.kilogramm.mattermost.model.entity.UploadState;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by kepar on 7.10.16.
 */
public class FileToAttach extends RealmObject {
    @PrimaryKey
    private long id;
    private String fileName;
    private String filePath;
    private String uriAsString;
    private int progress;
    private String uploadState;
    private boolean isTemporary;

    public FileToAttach(String filePath) {
        this(UploadState.WAITING_FOR_UPLOAD);
        this.filePath = filePath;
    }

    public FileToAttach(UploadState uploadState) {
        this.uploadState = uploadState.toString();
    }

    public FileToAttach(String fileName, String filePath, String uriAsString, UploadState uploadState) {
        this.uploadState = uploadState.toString();
        this.fileName = fileName;
        this.filePath = filePath;
        this.uriAsString = uriAsString;
    }

    public FileToAttach(String fileName, String filePath, String uriAsString, UploadState uploadState, boolean isTemporary) {
        this.uploadState = uploadState.toString();
        this.fileName = fileName;
        this.filePath = filePath;
        this.uriAsString = uriAsString;
        this.isTemporary = isTemporary;
    }

    public FileToAttach(long id, String fileName, String filePath, UploadState uploadState) {
        this.uploadState = uploadState.toString();
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public FileToAttach(long id, String fileName, UploadState uploadState) {
        this.uploadState = uploadState.toString();
        this.id = id;
        this.fileName = fileName;
    }

    public FileToAttach() {
    }

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

    public UploadState getUploadState() {
        return (uploadState != null) ? UploadState.valueOf(uploadState) : null;
    }

    public void setUploadState(UploadState uploadState) {
        this.uploadState = uploadState.toString();
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUriAsString() {
        return uriAsString;
    }

    public void setUriAsString(String uriAsString) {
        this.uriAsString = uriAsString;
    }

    public long getId() {
        return id;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean temporary) {
        isTemporary = temporary;
    }
}
