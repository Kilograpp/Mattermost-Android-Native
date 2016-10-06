package com.kilogramm.mattermost.model.entity;

import java.util.ArrayList;
import java.util.Collection;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by melkshake on 06.10.16.
 */

public class FoundMessagesIds extends RealmObject {

    @PrimaryKey
    private String messageId;

    public FoundMessagesIds(){}

    public FoundMessagesIds(String id) {
        this.messageId = id;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
