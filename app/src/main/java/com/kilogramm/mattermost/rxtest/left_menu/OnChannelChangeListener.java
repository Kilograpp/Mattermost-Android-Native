package com.kilogramm.mattermost.rxtest.left_menu;

/**
 * Created by Evgeny on 16.11.2016.
 */

public interface OnChannelChangeListener {
    void onChange(String channelId, String name,String type);
}
