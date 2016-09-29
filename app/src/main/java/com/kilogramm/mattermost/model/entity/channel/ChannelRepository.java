package com.kilogramm.mattermost.model.entity.channel;

import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.Repository;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserByIdSpecification;
import com.kilogramm.mattermost.model.entity.user.UserRepository;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 22.09.2016.
 */
public class ChannelRepository implements Repository<Channel> {
    private static final int LOW_DASH_COUNT = 2;

    @Override
    public void add(Channel item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    @Override
    public void add(Collection<Channel> items) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(items));
        realm.close();
    }

    public void prepareChannelAndAdd(Collection<Channel> items,
                                     String myId,
                                     UserRepository userRepository){
        String userId;
        for (Channel channel : items) {
            if(channel.getType().equals(Channel.DIRECT)){
                if(channel.getName().startsWith(myId)){
                    userId = channel.getName().substring(myId.length()+ LOW_DASH_COUNT);
                } else {
                    userId = channel.getName().substring(0, channel.getName().length()-myId.length()-LOW_DASH_COUNT);
                }
                channel.setUser(userRepository.query(new UserByIdSpecification(userId)).first());
                channel.setUsername(channel.getUser().getUsername());
            }
        }
        add(items);
    }
    @Override
    public void update(Channel item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    @Override
    public void remove(Channel item) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            final Channel post = realm.where(Channel.class).equalTo("id",item.getId()).findFirst();
            post.deleteFromRealm();
        });
        realm.close();
    }

    @Override
    public void remove(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final  Realm realm = Realm.getDefaultInstance();
        final  RealmResults<Channel> realmResults = realmSpecification.toRealmResults(realm);

        realm.executeTransaction(realm1 -> realmResults.deleteAllFromRealm());


        realm.close();
    }

    @Override
    public RealmResults<Channel> query(Specification specification) {
        final RealmSpecification realmSpecification = (RealmSpecification) specification;
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<Channel> realmResults = realmSpecification.toRealmResults(realm);

        realm.close();

        return realmResults;
    }
}
