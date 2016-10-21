package com.kilogramm.mattermost.model.entity.user;


import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Repository;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.post.Post;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class UserRepository {

    public static final String TAG = "UserRepository";

    public static void add(User item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    public static void add(Collection<User> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(items));
        realm.close();
    }

    public static void update(User item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            realm.insertOrUpdate(item);
            realm.insertOrUpdate(item.getNotifyProps());
        });
        realm.close();
    }

    public static void remove(User item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            final User user = realm.where(User.class).equalTo("id", item.getId()).findFirst();
            user.deleteFromRealm();
        });
        realm.close();
    }

    public static void remove(Specification specification) {
        RealmSpecification realmSpecification = (RealmSpecification) specification;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<User> realmResults = realmSpecification.toRealmResults(realm);

        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());
        realm.close();
    }

    public static RealmResults<User> query(Specification specification) {
        RealmSpecification realmSpecification = (RealmSpecification) specification;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<User> realmResults = realmSpecification.toRealmResults(realm);

        realm.close();

        return realmResults;
    }

    public static void updateUserMessage(String postId, String message) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 ->
                realm.where(Post.class)
                        .equalTo("id", postId)
                        .findFirst()
                        .setMessage(message));
        realm.close();
    }

    public static List<User> queryList(Specification specification){
        RealmSpecification realmSpecification = (RealmSpecification) specification;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<User> realmResults = realmSpecification.toRealmResults(realm);

        List<User> users = new ArrayList<>();

        for (User user : realmResults) {
            users.add(user);
        }

        realm.close();

        return users;
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
