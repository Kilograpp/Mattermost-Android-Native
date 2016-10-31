package com.kilogramm.mattermost.model;

import java.util.Collection;

import io.realm.RealmModel;
import io.realm.RealmResults;

/**
 * Created by Evgeny on 19.09.2016.
 */
public interface Repository<T extends RealmModel> {

    void add(T item);
    void add(Collection<T> items);

    void update(T item);

    void remove(T item);
    void remove(Specification specification);

    RealmResults<T> query(Specification specification);

}
