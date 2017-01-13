package com.kilogramm.mattermost.rxtest.autocomplete_list.model;

/**
 * Created by Evgeny on 11.01.2017.
 */

public abstract class AutoCompleteListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    abstract public int getType();
}
