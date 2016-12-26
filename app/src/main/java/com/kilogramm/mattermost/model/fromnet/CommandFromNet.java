package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Evgeny on 12.12.2016.
 */

public class CommandFromNet {
    @SerializedName("text")
    private String text;
    @SerializedName("goto_location")
    private String goToLocation;

    public String getGoToLocation() {
        return goToLocation;
    }
}
