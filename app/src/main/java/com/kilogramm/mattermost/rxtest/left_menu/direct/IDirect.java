package com.kilogramm.mattermost.rxtest.left_menu.direct;

/**
 * Created by Evgeny on 17.01.2017.
 */

public abstract class IDirect {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    abstract public int getType();

}
