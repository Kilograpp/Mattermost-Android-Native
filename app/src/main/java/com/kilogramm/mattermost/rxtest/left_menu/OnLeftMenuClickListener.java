package com.kilogramm.mattermost.rxtest.left_menu;

import android.view.View;

/**
 * Created by Evgeny on 15.11.2016.
 */

public interface OnLeftMenuClickListener {
    void onChannelClick(String itemId, String name, String type);
    void onCreateChannelClick(View view);
}
