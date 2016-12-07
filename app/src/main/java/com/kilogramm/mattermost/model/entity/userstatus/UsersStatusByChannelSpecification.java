package com.kilogramm.mattermost.model.entity.userstatus;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 20.09.2016.
 */
public class UsersStatusByChannelSpecification implements RealmSpecification {

    private final String channelId;

    public UsersStatusByChannelSpecification(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public RealmResults<UserStatus> toRealmResults(Realm realm) {
        RealmList<User> users = realm
                .where(ExtraInfo.class).equalTo("id", channelId).findFirst().getMembers();
        RealmQuery realmQuery = realm.where(UserStatus.class);
        for (int i = 0; i < users.size(); i++) {
            if (i != (users.size() - 1))
                realmQuery.equalTo("id", users.get(i).getId()).equalTo("status", UserStatus.ONLINE).or();
            else
                realmQuery.equalTo("id", users.get(i).getId()).equalTo("status", UserStatus.ONLINE);
        }
        return realmQuery.findAll();
    }
}
