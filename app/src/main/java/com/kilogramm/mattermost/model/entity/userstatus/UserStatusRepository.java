package com.kilogramm.mattermost.model.entity.userstatus;

import com.kilogramm.mattermost.model.RealmSpecification;
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
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(item));
    }


    public static void add(Collection<UserStatus> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(items));
    }


    public static void update(UserStatus item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(item));
    }


    public static void remove(UserStatus item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            UserStatus userStatus = realm1.where(UserStatus.class).equalTo("id",item.getId()).findFirst();
            userStatus.deleteFromRealm();
        });
    }


    public static void remove(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults realmResults = ((RealmSpecification) specification).toRealmResults(realm);
            realmResults.deleteAllFromRealm();
        });
    }


    public static RealmResults<UserStatus> query(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        return ((RealmSpecification) specification).toRealmResults(realm);
    }


    public static class UserStatusAllSpecification implements RealmSpecification {
        @Override
        public RealmResults<UserStatus> toRealmResults(Realm realm) {
            return realm.where(UserStatus.class).findAll();
        }
    }

}
