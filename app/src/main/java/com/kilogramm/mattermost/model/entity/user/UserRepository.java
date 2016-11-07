package com.kilogramm.mattermost.model.entity.user;


import android.util.Log;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.post.Post;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;

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

    public static void updateUserMessage(String postId, String message) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 ->
                realm.where(Post.class)
                        .equalTo("id", postId)
                        .findFirst()
                        .setMessage(message));
    }

    public static void updateUserAfterSaveSettings(User user){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Log.d(TAG, "change user");
        User me = realm.where(User.class).equalTo("id", user.getId()).findAll().first();
        me.setNickname(user.getNickname());
        me.setUsername(user.getUsername());
        me.setFirstName(user.getFirstName());
        me.setLastName(user.getLastName());
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
}
