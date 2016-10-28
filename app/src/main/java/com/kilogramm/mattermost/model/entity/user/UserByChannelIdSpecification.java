package com.kilogramm.mattermost.model.entity.user;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.entity.channel.Channel;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 20.09.2016.
 */
public class UserByChannelIdSpecification implements RealmSpecification {

    private final String channelId;

    public UserByChannelIdSpecification(String id) {
        this.channelId = id;
    }

    @Override
    public RealmResults<User> toRealmResults(Realm realm) {
        Channel channel = realm.where(Channel.class).equalTo("id", channelId).findFirst();
        if (channel.getUsername() != null)
            return realm.where(User.class)
                    .equalTo("username", channel.getUsername())
                    .findAll();
        return null;
    }
}
