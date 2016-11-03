package com.kilogramm.mattermost.model.extroInfo;


import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class ExtroInfoRepository {

    public static final String TAG = "ExtroInfoRepository";

    public static void add(ExtraInfo item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(item));
    }

    public static void add(Collection<ExtraInfo> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(items);
        realm.commitTransaction();
    }

    public static void update(ExtraInfo item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 ->
                realm1.insertOrUpdate(item)
        );
    }

    public static void remove(ExtraInfo item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            final ExtraInfo extraInfo = realm1.where(ExtraInfo.class).equalTo("id", item.getId()).findFirst();
            extraInfo.deleteFromRealm();
        });
    }

    public static void remove(Specification specification) {
        RealmSpecification realmSpecification = (RealmSpecification) specification;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ExtraInfo> realmResults = realmSpecification.toRealmResults(realm);
        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());
    }

    public static RealmResults<ExtraInfo> query(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        return ((RealmSpecification) specification).toRealmResults(realm);
    }


    public static class ExtroInfoByIdSpecification implements RealmSpecification {

        private final String id;

        public ExtroInfoByIdSpecification(String id) {
            this.id = id;
        }

        @Override
        public RealmResults<ExtraInfo> toRealmResults(Realm realm) {
            return realm.where(ExtraInfo.class)
                    .equalTo("id", id)
                    .findAll();
        }
    }


}
