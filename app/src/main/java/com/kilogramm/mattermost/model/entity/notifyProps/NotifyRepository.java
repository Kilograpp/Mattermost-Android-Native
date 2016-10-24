package com.kilogramm.mattermost.model.entity.notifyProps;


import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Repository;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.user.User;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class NotifyRepository implements Repository<NotifyProps> {

    public static final String TAG = "UserRepository";

    @Override
    public void add(NotifyProps item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
    }

    @Override
    public void add(Collection<NotifyProps> items) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(items));
    }

    @Override
    public void update(NotifyProps item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm.insertOrUpdate(item);
        });
    }

    @Override
    public void remove(NotifyProps item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            final NotifyProps notifyProps = realm.where(NotifyProps.class).equalTo("id", item.getId()).findFirst();
            notifyProps.deleteFromRealm();
        });

    }

    @Override
    public void remove(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<User> realmResults = realmSpecification.toRealmResults(realm);
        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());

    }

    @Override
    public RealmResults<NotifyProps> query(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<NotifyProps> realmResults = realmSpecification.toRealmResults(realm);


        return realmResults;
    }

    public RealmResults<NotifyProps> query() {
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<NotifyProps> realmResults = realm.where(NotifyProps.class)
                .findAll();

        return realmResults;
    }


}
