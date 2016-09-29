package com.kilogramm.mattermost.model.entity.userstatus;

import io.realm.RealmObject;

/**
 * Created by Evgeny on 29.09.2016.
 */
public class UserStatus extends RealmObject {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    public static final String REFRESH = "refresh";
    public static final String AWAY = "away";

    private String userId;
    private String status = "offline";

}
