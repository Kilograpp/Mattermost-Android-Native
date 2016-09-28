package com.kilogramm.mattermost.model.entity.channel;

import com.kilogramm.mattermost.model.RealmSpecification;

import java.util.Collections;
import java.util.Comparator;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Evgeny on 22.09.2016.
 */
public class ChannelByTypeSpecification implements RealmSpecification {

    private final String type;

    public ChannelByTypeSpecification(String type) {
        this.type = type;
    }

    @Override
    public RealmResults toRealmResults(Realm realm) {
        RealmResults<Channel> channels = realm.where(Channel.class)
                .equalTo("type", type)
                .findAllSorted("username", Sort.ASCENDING);
        return channels;
    }
}
