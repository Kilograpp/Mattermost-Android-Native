package com.kilogramm.mattermost.model.entity.userstatus;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 30.09.2016.
 */
public class AllRemove implements RealmSpecification {
    @Override
    public RealmResults toRealmResults(Realm realm) {
        return  realm.where(UserStatus.class).findAll();

    }
}
