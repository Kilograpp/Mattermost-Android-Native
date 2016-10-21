package com.kilogramm.mattermost.model.entity.channel;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.user.User;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Evgeny on 22.09.2016.
 */
public class ChannelRepository {

    private static final int LOW_DASH_COUNT = 2;

    public static void add(Channel channel){
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm1.insertOrUpdate(channel);
        });
        realm.close();
    }

    public static void add(Collection<Channel> items) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(items));
        realm.close();
    }

    public static void update(Channel item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    public static void remove(Channel item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            final Channel post = realm1.where(Channel.class).equalTo("id",item.getId()).findFirst();
            post.deleteFromRealm();
        });
        realm.close();
    }


    public static void prepareChannelAndAdd(Collection<Channel> items,
                                     String myId){
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            for (Channel channel : items) {
                String userId;
                if(channel.getType().equals(Channel.DIRECT)){
                    if(channel.getName().startsWith(myId)){
                        userId = channel.getName().substring(myId.length()+ LOW_DASH_COUNT);
                    } else {
                        userId = channel.getName().substring(0, channel.getName().length()-myId.length()-LOW_DASH_COUNT);
                    }
                    channel.setUser(realm1.where(User.class).equalTo("id", userId).findFirst());
                    channel.setUsername(channel.getUser().getUsername());
                }
            }
            realm1.copyToRealmOrUpdate(items);
        });
        realm.close();
    }

    public static void prepareDirectChannelAndAdd(Channel channel, String userId) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            if(channel.getType().equals(Channel.DIRECT)){
                channel.setUser(realm1.where(User.class).equalTo("id", userId).findFirst());
                channel.setUsername(channel.getUser().getUsername());
            }
        });
    }


    public static void remove(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final  Realm realm = Realm.getDefaultInstance();
        final  RealmResults<Channel> realmResults = realmSpecification.toRealmResults(realm);

        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());


        realm.close();
    }

    public static RealmResults<Channel> query(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<Channel> realmResults = realmSpecification.toRealmResults(realm);

        realm.close();

        return realmResults;
    }



    // region Specification

    public static class ChannelByIdSpecification implements RealmSpecification {
        private final String id;

        public ChannelByIdSpecification(String id) {
            this.id = id;
        }

        @Override
        public RealmResults<Channel> toRealmResults(Realm realm) {
            return realm.where(Channel.class)
                    .equalTo("id", id)
                    .findAll();
        }
    }

    public static class ChannelByTypeSpecification implements RealmSpecification {

        private final String type;

        public ChannelByTypeSpecification(String type) {
            this.type = type;
        }

        @Override
        public RealmResults<Channel> toRealmResults(Realm realm) {
            RealmResults<Channel> channels = realm.where(Channel.class)
                    .equalTo("type", type)
                    .findAllSorted("username", Sort.ASCENDING);
            return channels;
        }
    }

    public static class ChannelAllSpecification implements RealmSpecification{

        @Override
        public RealmResults toRealmResults(Realm realm) {
            return realm.where(Channel.class).findAllSorted("username",Sort.ASCENDING);
        }
    }

    //endregion
}

