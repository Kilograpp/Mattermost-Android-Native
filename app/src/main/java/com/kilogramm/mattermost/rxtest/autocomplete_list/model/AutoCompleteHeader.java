package com.kilogramm.mattermost.rxtest.autocomplete_list.model;

/**
 * Created by Evgeny on 11.01.2017.
 */

public class AutoCompleteHeader extends AutoCompleteListItem {

    private String header;

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
