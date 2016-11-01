package com.kilogramm.mattermost.model.entity.user;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 20.09.2016.
 */
public class UserByChannelSpecification implements RealmSpecification {

    private final String channelId;

    public UserByChannelSpecification(String id) {
        this.channelId = id;
    }

    @Override
    public RealmResults<User> toRealmResults(Realm realm) {
            return realm.where(User.class)
                    .findAll();
    }
}
