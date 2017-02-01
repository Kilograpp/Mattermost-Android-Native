package com.kilogramm.mattermost.model.entity.notifyProps;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class NotifySpecification implements RealmSpecification {

    public NotifySpecification() {
    }

    @Override
    public RealmResults<NotifyProps> toRealmResults(Realm realm) {
        return realm.where(NotifyProps.class)
                .findAll();
    }
}
