package com.kilogramm.mattermost.model.entity;

import java.util.List;

/**
 * Created by kepar on 28.9.16.
 */

public class FileUploadResponse {
    List<String> filenames;
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
