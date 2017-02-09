package com.kilogramm.mattermost.model.entity.user_v2;

import android.content.ContentValues;
import android.database.Cursor;

import com.kilogramm.mattermost.database.DBHelper;
import com.kilogramm.mattermost.database.repository.UsersRepository;

/**
 * Created by ivan on 09.02.17.
 */

public class UserV2 {

    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_OFFLINE = "offline";
    public static final String STATUS_AWAY = "away";
    public static final String REFRESH = "refresh";

    private String id;
    private String username;
    private String nickname;
    private String email;
    private String firstName;
    private String lastName;
    private Long updateAt;
    private Long lastActivityAt;
    private Long lastPictureUpdate;
    private String status;
    private String inTeam;
    private String isShow;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Long updateAt) {
        this.updateAt = updateAt;
    }

    public Long getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Long lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public Long getLastPictureUpdate() {
        return lastPictureUpdate;
    }

    public void setLastPictureUpdate(Long lastPictureUpdate) {
        this.lastPictureUpdate = lastPictureUpdate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInTeam() {
        return inTeam;
    }

    public void setInTeam(String inTeam) {
        this.inTeam = inTeam;
    }

    public String getIsShow() {
        return isShow;
    }

    public void setIsShow(String isShow) {
        this.isShow = isShow;
    }

    public UserV2() {
    }

    public UserV2(Cursor cursor) {
        this.id = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_COMMON_ID));
        this.username = cursor.getString(cursor.getColumnIndex(UsersRepository.FIELD_USER_NAME));
        this.nickname = cursor.getString(cursor.getColumnIndex(UsersRepository.FIELD_NICK_NAME));
        this.email = cursor.getString(cursor.getColumnIndex(UsersRepository.FIELD_EMAIL));
        this.firstName = cursor.getString(cursor.getColumnIndex(UsersRepository.FIELD_FIRST_NAME));
        this.lastName = cursor.getString(cursor.getColumnIndex(UsersRepository.FIELD_LAST_NAME));
        this.updateAt = cursor.getLong(cursor.getColumnIndex(UsersRepository.FIELD_UPDATED_AT));
        this.lastActivityAt = cursor.getLong(cursor.getColumnIndex(UsersRepository.FIELD_LAST_ACTIVITY_AT));
        this.lastPictureUpdate = cursor.getLong(cursor.getColumnIndex(UsersRepository.FIELD_LAST_PICTURE_UPDATE));
        this.status = cursor.getString(cursor.getColumnIndex(UsersRepository.FIELD_STATUS));
        this.inTeam = cursor.getString(cursor.getColumnIndex(UsersRepository.FIELD_IN_TEAM));
        this.isShow = cursor.getString(cursor.getColumnIndex(UsersRepository.FIELD_IS_SHOW));
    }

    public static ContentValues getMockableUserData(String name, String status, String inTeam){
        ContentValues cv = new ContentValues();
        if(name!=null) cv.put(DBHelper.FIELD_COMMON_ID, name);
        if(name!=null) cv.put(UsersRepository.FIELD_USER_NAME, "user."+name);
        cv.put(UsersRepository.FIELD_FIRST_NAME, name);
        cv.put(UsersRepository.FIELD_LAST_NAME, name + "ович");
        cv.put(UsersRepository.FIELD_NICK_NAME, name+"."+name);
        cv.put(UsersRepository.FIELD_STATUS, status);
        cv.put(UsersRepository.FIELD_IN_TEAM, inTeam);

        return cv;
    }
}
