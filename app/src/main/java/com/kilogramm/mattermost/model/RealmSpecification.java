package com.kilogramm.mattermost.model;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public interface RealmSpecification extends Specification {
    RealmResults toRealmResults(Realm realm);
}
