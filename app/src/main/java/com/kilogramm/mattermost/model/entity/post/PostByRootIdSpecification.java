package com.kilogramm.mattermost.model.entity.post;

import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class PostByRootIdSpecification implements RealmSpecification {

    private final String id;

    public PostByRootIdSpecification(String id) {
        this.id = id;
    }

    @Override
    public RealmResults<Post> toRealmResults(Realm realm) {
        return realm.where(Post.class)
                .equalTo("parentId", id)
                .findAll();
    }
}
