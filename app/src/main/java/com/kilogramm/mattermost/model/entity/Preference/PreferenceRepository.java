package com.kilogramm.mattermost.model.entity.Preference;


import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;

import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class PreferenceRepository {

    public static final String TAG = "UserRepository";

    public static void add(Preferences item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(realm1 -> realm1.insertOrUpdate(item));
    }

    public static void add(Collection<Preferences> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(items);
        realm.commitTransaction();
    }

    public static void update(Specification specification, String isTrue) {
        RealmSpecification realmSpecification = (RealmSpecification) specification;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Preferences> realmResults = realmSpecification.toRealmResults(realm);
        realm.executeTransaction(realm1 -> {
            for (Preferences item : realmResults) {
                item.setValue(isTrue);
            }
        });
    }

    public static void update(Preferences item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(item));
    }

    public static void update(String name, String value) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm1.where(Preferences.class).equalTo("name",name).findFirst().setValue(value);
        });
    }


    public static void update(List<Preferences> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            for (Preferences preferences : items) {
                realm1.insertOrUpdate(preferences);
            }
        });
    }

    public static void remove(Preferences item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            final Preferences preferences = realm1.where(Preferences.class).equalTo("name", item.getName()).findFirst();
            preferences.deleteFromRealm();
        });
    }

    public static void remove(Specification specification) {
        RealmSpecification realmSpecification = (RealmSpecification) specification;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Preferences> realmResults = realmSpecification.toRealmResults(realm);
        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());
    }

    public static RealmResults<Preferences> query(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        return ((RealmSpecification) specification).toRealmResults(realm);
    }

    public static RealmResults<Preferences> query() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Preferences.class).equalTo("category", "direct_channel_show").findAll();
    }


    public static class PreferenceByNameSpecification implements RealmSpecification {

        private final String name;

        public PreferenceByNameSpecification(String name) {
            this.name = name;
        }

        @Override
        public RealmResults<Preferences> toRealmResults(Realm realm) {
            return realm.where(Preferences.class)
                    .equalTo("name", name)
                    .findAll();
        }
    }

    public static class PreferenceByCategorySpecification implements RealmSpecification {

        private final String category;

        public PreferenceByCategorySpecification(String category) {
            this.category = category;
        }

        @Override
        public RealmResults<Preferences> toRealmResults(Realm realm) {
            return realm.where(Preferences.class)
                    .equalTo("category", category)
                    .findAll();
        }
    }


}
