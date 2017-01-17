package com.kilogramm.mattermost.model.entity.usermember;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.UserMember;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 16.01.2017.
 */
public class UserMemberRepository {
    public static final String TAG = "UserMemberRepository";

    public static void add(UserMember item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(item));
    }


    public static void add(Collection<UserMember> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(items));
    }


    public static void update(UserMember item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(item));
    }


    public static void remove(UserMember item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            UserStatus userStatus = realm1.where(UserStatus.class).equalTo("id",item.getUserId()).findFirst();
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


    public static RealmResults<UserMember> query(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        return ((RealmSpecification) specification).toRealmResults(realm);
    }


    public static class UserMemberAllSpecification implements RealmSpecification {
        @Override
        public RealmResults<UserMember> toRealmResults(Realm realm) {
            return realm.where(UserMember.class).findAll();
        }
    }
}
