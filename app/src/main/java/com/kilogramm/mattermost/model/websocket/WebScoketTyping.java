package com.kilogramm.mattermost.model.websocket;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class WebScoketTyping {

    @SerializedName("parent_id")
    @Expose
    private String parentId = "";

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
