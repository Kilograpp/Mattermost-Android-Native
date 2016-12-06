package com.kilogramm.mattermost.model.entity.team;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Evgeny on 28.07.2016.
 */
public class Team extends RealmObject{
    @PrimaryKey
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("display_name")
    private String displayName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
