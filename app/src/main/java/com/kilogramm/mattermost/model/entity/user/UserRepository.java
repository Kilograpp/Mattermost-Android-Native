package com.kilogramm.mattermost.model.entity.user;


import android.util.Log;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;

import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class UserRepository {

    public static final String TAG = "UserRepository";

    public static void add(User item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(realm1 -> realm1.insertOrUpdate(item));
    }

    public static void add(Collection<User> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(items);
        realm.commitTransaction();
    }

    public static void update(User item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm1.insertOrUpdate(item);
            realm1.insertOrUpdate(item.getNotifyProps());
        });
    }

    public static void remove(User item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            final User user = realm1.where(User.class).equalTo("id", item.getId()).findFirst();
            user.deleteFromRealm();
        });
    }

    public static void remove(Specification specification) {
        RealmSpecification realmSpecification = (RealmSpecification) specification;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<User> realmResults = realmSpecification.toRealmResults(realm);
        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());
    }

    public static RealmResults<User> query(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        return ((RealmSpecification) specification).toRealmResults(realm);
    }

    public static RealmResults<User> query() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(User.class).isNotNull("id").findAll();
    }

    public static void updateUserMessage(String postId, String message) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 ->
                realm.where(Post.class)
                        .equalTo("id", postId)
                        .findFirst()
                        .setMessage(message)
        );
    }

    public static void updateUserAfterSaveSettings(User user) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Log.d(TAG, "change user");
        User me = realm.where(User.class).equalTo("id", user.getId()).findAll().first();
        me.setNickname(user.getNickname());
        me.setUsername(user.getUsername());
        me.setFirstName(user.getFirstName());
        me.setLastName(user.getLastName());
        me.setEmail(user.getEmail());
        realm.copyToRealmOrUpdate(me);
        realm.commitTransaction();
    }


    public static void updateUserAvataTime(String userId, long time) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        User me = realm.where(User.class).equalTo("id", userId).findAll().first();
        me.setLastPictureUpdate(time);
        realm.copyToRealmOrUpdate(me);
        realm.commitTransaction();
    }

    public static class UserByIdSpecification implements RealmSpecification {

        private final String id;

        public UserByIdSpecification(String id) {
            this.id = id;
        }

        @Override
        public RealmResults<User> toRealmResults(Realm realm) {
            return realm.where(User.class)
                    .equalTo("id", id)
                    .findAll();
        }
    }

    public static class UserByIdsSpecification implements RealmSpecification {

        private final List<User> users;

        public UserByIdsSpecification(List<User> users) {
            this.users = users;
        }

        @Override
        public RealmResults<User> toRealmResults(Realm realm) {
            RealmQuery realmQuery = realm.where(User.class);
            User lastUser = users.get(users.size() - 1);
            for (User u : users) {
                if (lastUser != u)
                    realmQuery.equalTo("id", u.getId()).or();
                else
                    realmQuery.equalTo("id", u.getId());
            }
            return realmQuery.findAll();
        }
    }

    public static class UserByNotIdsSpecification implements RealmSpecification {

        private final List<User> users;
        private final String searchName;

        public UserByNotIdsSpecification(List<User> users, String searchName) {
            this.users = users;
            this.searchName = searchName;
        }

        @Override
        public RealmResults<User> toRealmResults(Realm realm) {
            RealmList<User> membersTeam = realm.where(ExtraInfo.class).equalTo(
                    "id", realm.where(Channel.class).equalTo("name", "town-square").findFirst().getId())
                    .findFirst().getMembers();

            RealmQuery realmQuery = membersTeam.where();
            if (searchName != null)
                realmQuery.contains("username", searchName);
            for (User u : users) {
                realmQuery.notEqualTo("id", u.getId());
            }

            return realmQuery.findAllSorted("username", Sort.ASCENDING);
        }
    }

    public static class UserByNotMiSpecification implements RealmSpecification {

        private final String id;

        public UserByNotMiSpecification(String id) {
            this.id = id;
        }

        @Override
        public RealmResults<User> toRealmResults(Realm realm) {
            return realm.where(User.class).notEqualTo("id",id).findAll();
        }
    }
}
