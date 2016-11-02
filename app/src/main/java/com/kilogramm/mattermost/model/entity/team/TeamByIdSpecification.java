package com.kilogramm.mattermost.model.entity.team;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class TeamByIdSpecification implements RealmSpecification {

    private final String id;

    public TeamByIdSpecification(String id) {
        this.id = id;
    }

    @Override
    public RealmResults<Team> toRealmResults(Realm realm) {
        return realm.where(Team.class)
                .equalTo("id", id)
                .findAll();
    }
}
