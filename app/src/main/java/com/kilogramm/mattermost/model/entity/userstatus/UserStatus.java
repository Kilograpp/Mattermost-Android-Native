package com.kilogramm.mattermost.model.entity.userstatus;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Evgeny on 29.09.2016.
 */
public class UserStatus extends RealmObject {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    public static final String REFRESH = "refresh";
    public static final String AWAY = "away";

    @PrimaryKey
    private String id;
    private String status = "offline";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserStatus(){

    }

    public UserStatus(String id, String status) {
        this.id = id;
        this.status = status;
    }
}
