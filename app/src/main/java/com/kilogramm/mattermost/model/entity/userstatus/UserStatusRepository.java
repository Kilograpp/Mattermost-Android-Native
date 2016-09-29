package com.kilogramm.mattermost.model.entity.userstatus;

import com.kilogramm.mattermost.model.Repository;
import com.kilogramm.mattermost.model.Specification;

import java.util.Collection;

import io.realm.RealmResults;

/**
 * Created by Evgeny on 29.09.2016.
 */
public class UserStatusRepository implements Repository<UserStatus> {
    @Override
    public void add(UserStatus item) {

    }

    @Override
    public void add(Collection<UserStatus> items) {

    }

    @Override
    public void update(UserStatus item) {

    }

    @Override
    public void remove(UserStatus item) {

    }

    @Override
    public void remove(Specification specification) {

    }

    @Override
    public RealmResults<UserStatus> query(Specification specification) {
        return null;
    }
}
