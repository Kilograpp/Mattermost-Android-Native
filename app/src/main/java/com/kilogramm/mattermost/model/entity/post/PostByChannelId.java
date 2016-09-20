package com.kilogramm.mattermost.model.entity.post;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class PostByChannelId implements RealmSpecification {

    private final String channelId;

    public PostByChannelId(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public RealmResults<Post> toRealmResults(Realm realm) {
        return realm.where(Post.class)
                .equalTo("channelId", channelId)
                .findAllSorted("createAt", Sort.ASCENDING);
    }
}
