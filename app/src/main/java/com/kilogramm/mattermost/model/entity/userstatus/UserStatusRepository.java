package com.kilogramm.mattermost.model.entity.userstatus;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Repository;
import com.kilogramm.mattermost.model.Specification;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 29.09.2016.
 */
public class UserStatusRepository {
    public static final String TAG = "UserRepository";


    public static void add(UserStatus item) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }


    public static void add(Collection<UserStatus> items) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(items));
        realm.close();
    }


    public static void update(UserStatus item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }


    public static void remove(UserStatus item) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            final UserStatus userStatus = realm.where(UserStatus.class).equalTo("id",item.getId()).findFirst();
            userStatus.deleteFromRealm();
        });
        realm.close();
    }


    public static void remove(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final  Realm realm = Realm.getDefaultInstance();
        final RealmResults<UserStatus> realmResults = realmSpecification.toRealmResults(realm);

        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());
        realm.close();
    }


    public static RealmResults<UserStatus> query(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<UserStatus> realmResults = realmSpecification.toRealmResults(realm);

        realm.close();

        return realmResults;
    }

    public static class UserStatusAllSpecification implements RealmSpecification {
        @Override
        public RealmResults<UserStatus> toRealmResults(Realm realm) {
            return realm.where(UserStatus.class).findAll();
        }
    }

}
