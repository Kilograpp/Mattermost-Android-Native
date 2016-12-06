package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by kepar on 28.9.16.
 */

public class FileUploadResponse {
    @SerializedName("filenames")
    @Expose
    List<String> filenames;
    @SerializedName("client_ids")
    @Expose
    List<String> client_ids;

    public List<String> getFilenames() {
        return filenames;
    }

    public List<String> getClient_ids() {
        return client_ids;
    }

    @Override
    public String toString() {
        return "FileUploadResponse{" +
                "filenames=" + filenames +
                ", client_ids=" + client_ids +
                '}';
    }
}
