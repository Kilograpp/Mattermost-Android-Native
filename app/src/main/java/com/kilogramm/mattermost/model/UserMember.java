package com.kilogramm.mattermost.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Evgeny on 16.01.2017.
 */
public class UserMember extends RealmObject {

    @SerializedName("deleted_at")
    private long deletedAt;
    @SerializedName("roles")
    private String roles;
    @SerializedName("team_id")
    private String teamId;
    @PrimaryKey
    @SerializedName("user_id")
    private String userId;


    public UserMember() {
    }

    public UserMember(long deletedAt, String roles, String teamId, String userId) {
        this.deletedAt = deletedAt;
        this.roles = roles;
        this.teamId = teamId;
        this.userId = userId;
    }

    public long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
