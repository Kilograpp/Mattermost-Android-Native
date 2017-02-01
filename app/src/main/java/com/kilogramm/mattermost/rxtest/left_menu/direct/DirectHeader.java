package com.kilogramm.mattermost.rxtest.left_menu.direct;

/**
 * Created by Evgeny on 17.01.2017.
 */

public class DirectHeader extends IDirect {

    private String headerTitle;

    public DirectHeader(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    @Override
    public String toString() {
        return "DirectHeader{" +
                "headerTitle='" + headerTitle + '\'' +
                '}';
    }
}
