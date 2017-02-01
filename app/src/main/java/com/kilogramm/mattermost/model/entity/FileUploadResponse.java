package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;

import java.util.List;

/**
 * Created by kepar on 28.9.16.
 */

public class FileUploadResponse {
    @SerializedName("file_infos")
    @Expose
    List<FileInfo> file_infos;
    @SerializedName("client_ids")
    @Expose
    List<String> client_ids;

    public List<FileInfo> getFile_infos() {
        return file_infos;
    }

    public List<String> getClient_ids() {
        return client_ids;
    }

    @Override
    public String toString() {
        return "FileUploadResponse{" +
                "file_infos=" + file_infos +
                ", client_ids=" + client_ids +
                '}';
    }
}
