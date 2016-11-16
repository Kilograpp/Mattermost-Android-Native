package com.kilogramm.mattermost.model.entity.channel;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 26.09.2016.
 */
public class ChannelByHadleSpecification implements RealmSpecification {

    private String id;

    public ChannelByHadleSpecification(String id) {
        this.id = id;
    }

    @Override
    public RealmResults toRealmResults(Realm realm) {
        return realm.where(Channel.class)
                .equalTo("name",id)
                .findAll();
    }
}
