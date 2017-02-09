package com.kilogramm.mattermost.database.repository;

import android.content.ContentValues;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.database.DBHelper;
import com.kilogramm.mattermost.database.MattermostContentProvider;
import com.kilogramm.mattermost.model.entity.user.User;

/**
 * Created by kepar on 06.02.17.
 */

public class UsersRepository {

    public static final String TABLE_NAME_USERS = "Users";

    public static final String FIELD_USER_NAME = "userName";
    public static final String FIELD_NICK_NAME = "nickName";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FIRST_NAME = "firstName";
    public static final String FIELD_LAST_NAME = "lastName";
    public static final String FIELD_UPDATED_AT = "updatedAt";
    public static final String FIELD_LAST_ACTIVITY_AT = "lastActivityAt";
    public static final String FIELD_LAST_PICTURE_UPDATE = "lastPictureUpdate";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IN_TEAM = "inTeam";
    public static final String FIELD_IS_SHOW = "isShow";

    public static void addUser(User user){
        ContentValues values = new ContentValues();
        values.put(DBHelper.FIELD_COMMON_ID, user.getId());
        values.put(FIELD_USER_NAME, user.getUsername());
        values.put(FIELD_NICK_NAME, user.getNickname());
        values.put(FIELD_EMAIL, user.getEmail());
        values.put(FIELD_FIRST_NAME, user.getFirstName());
        values.put(FIELD_LAST_NAME, user.getLastName());
        values.put(FIELD_UPDATED_AT, user.getUpdateAt());
        values.put(FIELD_LAST_ACTIVITY_AT, user.getLastActivityAt());
        values.put(FIELD_LAST_PICTURE_UPDATE, user.getLastPictureUpdate());
        values.put(FIELD_STATUS, user.getStatus());
       /* values.put(FIELD_IN_TEAM, user.getInTeam());
        values.put(FIELD_IS_SHOW, user.get());*/
        MattermostApp.getSingleton()
                .getApplicationContext()
                .getContentResolver()
                .insert(MattermostContentProvider.CONTENT_URI_USERS, values);
    }

}
