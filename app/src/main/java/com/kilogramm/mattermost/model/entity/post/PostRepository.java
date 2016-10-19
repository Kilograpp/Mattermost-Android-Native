package com.kilogramm.mattermost.model.entity.post;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Repository;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class PostRepository {


    public static void add(Post item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    public static void add(Collection<Post> items) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(items));
        realm.close();
    }

    public static void update(Post item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    public static void remove(Post item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            final Post post = realm.where(Post.class).equalTo("id",item.getId()).findFirst();
            post.deleteFromRealm();
        });
        realm.close();
    }

    public static void remove(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final  Realm realm = Realm.getDefaultInstance();
        final  RealmResults<Post> realmResults = realmSpecification.toRealmResults(realm);

        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());


        realm.close();
    }

    public static RealmResults<Post> query(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<Post> realmResults = realmSpecification.toRealmResults(realm);

        realm.close();

        return realmResults;
    }

    public static void removeTempPost(String sendedPostId){
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            final Post post = realm.where(Post.class).equalTo("pendingPostId",sendedPostId).findFirst();
            post.deleteFromRealm();
        });
        realm.close();
    }

    public static void prepareAndAdd(Posts posts) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            for (Post post : posts.getPosts().values()) {
                post.setUser(realm1.where(User.class).equalTo("id", post.getUserId()).findFirst());
                post.setViewed(true);
                post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
            }
            realm1.insertOrUpdate(posts.getPosts().values());
        });
        realm.close();

    }
}
