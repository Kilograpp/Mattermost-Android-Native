package com.kilogramm.mattermost.model.entity.user;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import io.realm.RealmObject;

/**
 * Created by melkshake on 27.12.16.
 */

public class Users {
    @SerializedName("users")
    Map<String, User> users;

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }
}
