package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.Expose;

import java.util.Map;

import io.realm.annotations.Ignore;

/**
 * Created by Evgeny on 17.08.2016.
 */
// TODO класс не используется (Kepar)
public class UserStatusList {

    @Expose
    @Ignore
    private Map<String, String> statusMap;

    public Map<String, String> getStatusMap() {
        return statusMap;
    }

    public void setStatusMap(Map<String, String> statusMap) {
        this.statusMap = statusMap;
    }
}
