package com.kilogramm.mattermost.model.entity.notifyProps;


import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class NotifyRepository {

    public static final String TAG = "UserRepository";


    public static void add(NotifyProps item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(item));
    }

    public static void add(Collection<NotifyProps> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(items));
    }

    public static void update(NotifyProps item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm1.insertOrUpdate(item);
        });
    }


    public static void remove(NotifyProps item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            NotifyProps notifyProps = realm1.where(NotifyProps.class).equalTo("id", item.getId()).findFirst();
            notifyProps.deleteFromRealm();
        });

    }

    public static void remove(Specification specification) {
        RealmSpecification realmSpecification = (RealmSpecification) specification;
        Realm realm = Realm.getDefaultInstance();
        RealmResults realmResults = realmSpecification.toRealmResults(realm);
        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());

    }

    public static RealmResults<NotifyProps> query(Specification specification) {
        RealmSpecification realmSpecification = (RealmSpecification) specification;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<NotifyProps> realmResults = realmSpecification.toRealmResults(realm);
        return realmResults;
    }

    public static RealmResults<NotifyProps> query() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<NotifyProps> realmResults = realm.where(NotifyProps.class)
                .findAll();
        return realmResults;
    }


}
