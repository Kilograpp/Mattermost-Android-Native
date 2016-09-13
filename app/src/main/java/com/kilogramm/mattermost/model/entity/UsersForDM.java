package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by melkshake on 13.09.16.
 */
public class UsersForDM {
    @SerializedName("dmList")
    @Expose
    private Map<String, User> dmList;

    public Map<String, User> getDmList() {
        return dmList;
    }

    public void setDmList(Map<String, User> dmList) {
        this.dmList = dmList;
    }

    @Override
    public String toString() {
        return "UsersForDM{" +
                "dmList=" + dmList +
                '}';
    }

    public String dmListToString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : dmList.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }

            stringBuilder.append(key);

        }

        return stringBuilder.toString();
    }
}
