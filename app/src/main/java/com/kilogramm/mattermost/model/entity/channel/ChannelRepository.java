package com.kilogramm.mattermost.model.entity.channel;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Repository;
import com.kilogramm.mattermost.model.Specification;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 22.09.2016.
 */
public class ChannelRepository implements Repository<Channel> {
    @Override
    public void add(Channel item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    @Override
    public void add(Collection<Channel> items) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(items));
        realm.close();
    }

    @Override
    public void update(Channel item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    @Override
    public void remove(Channel item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            final Channel post = realm.where(Channel.class).equalTo("id",item.getId()).findFirst();
            post.deleteFromRealm();
        });
        realm.close();
    }

    @Override
    public void remove(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final  Realm realm = Realm.getDefaultInstance();
        final  RealmResults<Channel> realmResults = realmSpecification.toRealmResults(realm);

        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());


        realm.close();
    }

    @Override
    public RealmResults<Channel> query(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<Channel> realmResults = realmSpecification.toRealmResults(realm);

        realm.close();

        return realmResults;
    }
}
