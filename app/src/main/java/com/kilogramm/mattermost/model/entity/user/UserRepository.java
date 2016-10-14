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
public class UserRepository implements Repository<User> {

    public static final String TAG = "UserRepository";

    @Override
    public void add(User item) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    @Override
    public void add(Collection<User> items) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(items));
        realm.close();
    }

    @Override
    public void update(User item) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    public void updateUserStatus(String id, String new_status){
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            User user = realm.where(User.class)
                    .equalTo("id", id)
                    .findFirst();
            user.setStatus(new_status);
        });

        realm.close();
    }

    @Override
    public void remove(User item) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            final User user = realm.where(User.class).equalTo("id",item.getId()).findFirst();
            user.deleteFromRealm();
        });
        realm.close();
    }

    @Override
    public void remove(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final  Realm realm = Realm.getDefaultInstance();
        final RealmResults<User> realmResults = realmSpecification.toRealmResults(realm);

        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());
        realm.close();
    }

    @Override
    public RealmResults<User> query(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<User> realmResults = realmSpecification.toRealmResults(realm);

        realm.close();

        return realmResults;
    }

    public void updateUserMessage(String postId, String message) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 ->
                realm.where(Post.class)
                .equalTo("id", postId)
                .findFirst()
                .setMessage(message));
        realm.close();
    }

    public List<User> queryList(Specification specification){
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<User> realmResults = realmSpecification.toRealmResults(realm);

        final List<User> users = new ArrayList<>();

        for (User user : realmResults) {
            users.add(user);
        }

        realm.close();

        return users;
    }
}
