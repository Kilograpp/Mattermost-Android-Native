package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LicenseCfg extends RealmObject {

    @PrimaryKey
    private long id;
    @SerializedName("IsLicensed")
    @Expose
    private String isLicensed;

    /**
     *
     * @return
     * The isLicensed
     */
    public String getIsLicensed() {
        return isLicensed;
    }

    /**
     *
     * @param isLicensed
     * The IsLicensed
     */
    public void setIsLicensed(String isLicensed) {
        this.isLicensed = isLicensed;
    }

}
