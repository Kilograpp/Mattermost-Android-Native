package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.SerializedName;

/**
 * Created by kraftu on 13.09.16.
 */
public class ForgotData {

    @SerializedName("email")
    public String email;

    public ForgotData() {
    }

    public ForgotData(String email) {
        this.email = email;
    }
}
