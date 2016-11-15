package com.kilogramm.mattermost.rxtest.left_menu;

/**
 * Created by Evgeny on 15.11.2016.
 */

public interface OnLeftMenuClickListener {
    void onChannelClick(String itemId, String name, String type);
    void onCreateChannelClick();
}
