package com.kilogramm.mattermost.model.entity.member;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;

import java.util.Collection;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 08.11.2016.
 */

public class MembersRepository {

    public static final String TAG = "MembersRepository";


    public static void add(Member item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(item));
    }


    public static void add(Collection<Member> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(items));
    }


    public static void update(Member item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(item));
    }

    public static void update(Map<String, Member> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            for (Map.Entry<String, Member> item : items.entrySet())
                realm1.insertOrUpdate(item.getValue());
        });
    }


    public static void remove(Member item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            UserStatus userStatus = realm1.where(UserStatus.class).equalTo("channelId", item.getChannelId()).findFirst();
            userStatus.deleteFromRealm();
        });
    }


    public static void remove(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults realmResults = ((RealmSpecification) specification).toRealmResults(realm);
        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());
    }


    public static RealmResults<Member> query(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        return ((RealmSpecification) specification).toRealmResults(realm);
    }

}
