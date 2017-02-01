package com.kilogramm.mattermost.model.entity.user;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.entity.channel.Channel;


import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Evgeny on 23.09.2016.
 */
public class UserWithDirectSpecification implements RealmSpecification {

    public static final String TAG = "UserWithDirectSp";


    @Override
    public RealmResults toRealmResults(Realm realm) {
        return realm.where(Channel.class)
                .equalTo("type", Channel.DIRECT)
                .findAllSorted("username", Sort.ASCENDING);
    }
}
