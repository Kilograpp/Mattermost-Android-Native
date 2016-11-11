package com.kilogramm.mattermost.model.entity.member;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 09.11.2016.
 */

public class MemberAll implements RealmSpecification {
    @Override
    public RealmResults toRealmResults(Realm realm) {
        return realm.where(Member.class).findAll();
    }
}
