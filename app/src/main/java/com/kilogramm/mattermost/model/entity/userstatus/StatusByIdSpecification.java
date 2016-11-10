package com.kilogramm.mattermost.model.entity.userstatus;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 20.09.2016.
 */
public class StatusByIdSpecification implements RealmSpecification {

    private final String userId;

    public StatusByIdSpecification(String id) {
        this.userId = id;
    }

    @Override
    public RealmResults<UserStatus> toRealmResults(Realm realm) {
            return realm.where(UserStatus.class).equalTo("id",userId).findAll();
    }
}
