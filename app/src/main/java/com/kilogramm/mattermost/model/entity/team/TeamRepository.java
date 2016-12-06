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


    public static void add(Team item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
    }

    public static void add(Collection<Team> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(items));
    }

    public static void update(Team item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
    }

    public static void remove(Team item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            if(realm.where(Team.class).equalTo("id", item.getId()).findAll().size()!=0) {
                Team team = realm.where(Team.class).equalTo("id", item.getId()).findFirst();
                team.deleteFromRealm();
            }
        });
    }

    public static void remove(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults realmResults = ((RealmSpecification) specification).toRealmResults(realm);
        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());

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
