package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by melkshake on 22.09.16.
 */
public class SaveData {

    @SerializedName("category")
    public final String CATEGORY_DIRECT = "direct_channel_show";

    private String name;
    private String user_id;
    private String value;

    public SaveData(String name, String user_id, Boolean value) {
        this.name = name;
        this.user_id = user_id;
        this.value = value.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value.toString();
    }
}
