package com.kilogramm.mattermost.model.entity.channel;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.presenter.ChatPresenter;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 26.09.2016.
 */
public class ChannelByNameSpecification implements RealmSpecification {

    private String myId;
    private String userId;

    public ChannelByNameSpecification(String myId, String userId) {
        this.myId = myId;
        this.userId = userId;
    }

    @Override
    public RealmResults toRealmResults(Realm realm) {
        return realm.where(Channel.class)
                .equalTo("name", myId + "__" + userId)
                .or()
                .equalTo("name", userId + "__" + myId)
                .findAll();
    }
}
