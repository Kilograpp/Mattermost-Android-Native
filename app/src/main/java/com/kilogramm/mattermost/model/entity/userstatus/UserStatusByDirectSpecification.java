package com.kilogramm.mattermost.model.entity.userstatus;

import com.kilogramm.mattermost.MattermostPreference;
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
public class UserStatusByDirectSpecification implements RealmSpecification {

    private final String channelId;

    public UserStatusByDirectSpecification(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public RealmResults<UserStatus> toRealmResults(Realm realm) {
        RealmList<User> users = realm
                .where(ExtraInfo.class).equalTo("id", channelId).findFirst().getMembers();
        String myUserId = MattermostPreference.getInstance().getMyUserId();
        RealmQuery realmQuery = realm.where(UserStatus.class);
        for (User user : users) {
            if (!user.getId().equals(myUserId))
                realmQuery
                        .equalTo("id", user.getId());
        }
        return realmQuery.findAll();
    }
}
