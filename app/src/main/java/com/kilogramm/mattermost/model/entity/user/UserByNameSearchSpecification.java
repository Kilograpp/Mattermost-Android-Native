package com.kilogramm.mattermost.model.entity.user;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.RealmSpecification;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Evgeny on 20.09.2016.
 */
public class UserByNameSearchSpecification implements RealmSpecification {

    private final String name;

    public UserByNameSearchSpecification(String name) {
        this.name = name;
    }

    @Override
    public RealmResults<User> toRealmResults(Realm realm) {
        String currentUser = MattermostPreference.getInstance().getMyUserId();
        if (name == null)
            return realm.where(User.class)
                    .isNotNull("id")
                    .notEqualTo("id", currentUser)
                    .equalTo("deleteAt", 0L)
                    .findAllSorted("username", Sort.ASCENDING);
        else {
            String[] username = name.split("@");
            return realm.where(User.class)
                    .isNotNull("id")
                    .notEqualTo("id", currentUser)
                    .equalTo("deleteAt", 0L)
                    .contains("username", username[username.length - 1])
                    .findAllSorted("username", Sort.ASCENDING);
        }
    }
}
