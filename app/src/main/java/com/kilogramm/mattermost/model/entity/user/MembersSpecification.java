package com.kilogramm.mattermost.model.entity.user;

import com.kilogramm.mattermost.model.RealmSpecification;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 20.09.2016.
 */
public class MembersSpecification implements RealmSpecification {

    private List<User> members;

    public MembersSpecification(List<User> members) {
        this.members = members;
    }

    @Override
    public RealmResults<User> toRealmResults(Realm realm) {
        
        return null;
    }
}
