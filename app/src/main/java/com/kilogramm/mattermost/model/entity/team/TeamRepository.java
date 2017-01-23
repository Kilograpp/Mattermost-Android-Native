package com.kilogramm.mattermost.model.entity.team;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class TeamRepository {

    public static Team get(String teamId){
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Team.class).equalTo("id", teamId).findFirst();
    }

    public static RealmResults<Team> query(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        return ((RealmSpecification) specification).toRealmResults(realm);
    }

    public static RealmResults<Team> query() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Team.class).findAll();
    }


}
