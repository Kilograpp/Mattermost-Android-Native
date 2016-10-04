package com.kilogramm.mattermost.model.entity.user;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 20.09.2016.
 */
public class UserByIdSpecification implements RealmSpecification {

    private final String id;

    public UserByIdSpecification(String id) {
        this.id = id;
    }

    @Override
    public RealmResults<User> toRealmResults(Realm realm) {
        return realm.where(User.class)
                .equalTo("id", id)
                .findAll();
    }
}
