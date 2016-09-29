package com.kilogramm.mattermost.model.entity.channel;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 22.09.2016.
 */
public class ChannelByIdSpecification implements RealmSpecification {
    private final String id;

    public ChannelByIdSpecification(String id) {
        this.id = id;
    }

    @Override
    public RealmResults<Channel> toRealmResults(Realm realm) {
        return realm.where(Channel.class)
                .equalTo("id", id)
                .findAll();
    }
}
