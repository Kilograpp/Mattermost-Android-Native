package com.kilogramm.mattermost.model.entity.post;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.Posts;
import com.kilogramm.mattermost.model.entity.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public class PostRepository {

    public static void add(Post item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
    }

    public static void add(Collection<Post> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(items));
    }

    public static void update(Post item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
    }

    public static void remove(Post item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            if (realm.where(Post.class).equalTo("id", item.getId()).findAll().size() != 0) {
                Post post = realm.where(Post.class).equalTo("id", item.getId()).findFirst();
                post.deleteFromRealm();
            }
        });
    }

    public static void remove(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults realmResults = ((RealmSpecification) specification).toRealmResults(realm);
        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());
    }

    public static RealmResults<Post> query(Specification specification) {
        Realm realm = Realm.getDefaultInstance();
        return ((RealmSpecification) specification).toRealmResults(realm);
    }

    public static Post query(String id) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(Post.class).equalTo("id", id).findFirst();
    }

    public static void removeTempPost(String sendedPostId) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            final Post post = realm.where(Post.class).equalTo("pendingPostId", sendedPostId).findFirst();
            post.deleteFromRealm();
        });
    }

    public static void merge(Collection<Post> posts, Specification specification) {
        RealmResults realmResults = query(specification);
        List<Post> deleteFromRealm = new ArrayList<>();
        List<Post> postsFromServer = new ArrayList<>();
        postsFromServer.addAll(posts);
        for (Post post : posts) {
            if (query(post.getId()) != null) {
                postsFromServer.remove(post);
            }
        }
        for (Post post : postsFromServer) {
            prepareAndAddPost(post);
        }
    }

    public static void prepareAndAddPost(Post post) {
        Realm realm = Realm.getDefaultInstance();
        if (!post.isSystemMessage())
            post.setUser(realm.where(User.class).equalTo("id", post.getUserId()).findFirst());
        else
            post.setUser(new User("System", "System", "System"));
        post.setViewed(true);
        if (post.getProps().getFrom_webhook() == null) {
            post.setProps(null);
        } else {
            post.getProps().getAttachments().get(0).setText(
                    Processor.process(post.getProps().getAttachments().get(0).getText(),
                            Configuration.builder().forceExtentedProfile().build()));
        }
        post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
        add(post);
    }

    public static void prepareAndAdd(Posts posts) {
        Realm realm = Realm.getDefaultInstance();
        for (Post post : posts.getPosts().values()) {
            if (post.isSystemMessage())
                post.setUser(new User("System", "System", "System"));
            else
                post.setUser(realm.where(User.class).equalTo("id", post.getUserId()).findFirst());
            post.setViewed(true);
            post.setMessage(Processor.process(post.getMessage(), Configuration.builder().forceExtentedProfile().build()));
            if (post.getProps().getFrom_webhook() == null) {
                post.setProps(null);
            } else
                post.getProps().getAttachments().get(0).setText(
                        Processor.process(post.getProps().getAttachments().get(0).getText(),
                                Configuration.builder().forceExtentedProfile().build()));

        }
        add(posts.getPosts().values());
    }

    public static void updateUpdateAt(String postId, long update) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<Post> posts = realm1.where(Post.class).equalTo("id", postId).findAll();
            if (posts.size() != 0) {
                Post post = posts.first();
                post.setUpdateAt(update);
            }
        });
    }
}
