package com.kilogramm.mattermost.model.entity.member;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.userstatus.UserStatus;

import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

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

    public static void update(List<Member> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(items));
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

    public static class MemberByNotIdsSpecification implements RealmSpecification {

        private final List<User> users;
        private final String searchName;

        public MemberByNotIdsSpecification(List<User> users, String searchName) {
            this.users = users;
            this.searchName = searchName;
        }

        @Override
        public RealmResults<User> toRealmResults(Realm realm) {
            RealmQuery realmQuery = realm.where(User.class);
            realmQuery.isNotNull("createAt");
            realmQuery.equalTo("deleteAt", 0);
            if (searchName != null)
                realmQuery.contains("username", searchName);
            for (User u : users) {
                realmQuery.notEqualTo("id", u.getId());
            }
            return realmQuery.findAllSorted("username", Sort.ASCENDING);
        }
    }
}
