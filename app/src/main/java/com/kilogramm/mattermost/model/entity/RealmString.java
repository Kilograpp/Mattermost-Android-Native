package com.kilogramm.mattermost.model.entity;

import io.realm.RealmObject;

/**
 * Created by Evgeny on 02.09.2016.
 */
public class RealmString extends RealmObject {

    private String string;

    public RealmString(String s) {
        super();
        this.string = s;
    }

    public RealmString(){
        super();
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }



}
