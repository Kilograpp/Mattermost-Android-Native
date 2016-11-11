package com.kilogramm.mattermost.model.entity.member;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 08.11.2016.
 */
public class MemberById implements RealmSpecification {
    private String id;

    public MemberById(String id) {
        this.id = id;
    }

    @Override
    public RealmResults toRealmResults(Realm realm) {
        return realm.where(Member.class).equalTo("channelId", id).findAll();
    }
}
