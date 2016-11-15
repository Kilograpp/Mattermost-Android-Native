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
    private final int cursorPos;

    public UserByNameSearchSpecification(String name, int cursorPos) {
        this.name = name;
        this.cursorPos = cursorPos;
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
            int position = 0;
            int count = 0;
            String[] username = name.split("@");
            for (String s : username) {
                count += s.length() + 1;
                if (cursorPos < count)
                    break;
                position++;
            }
            return realm.where(User.class)
                    .isNotNull("id")
                    .notEqualTo("id", currentUser)
                    .equalTo("deleteAt", 0L)
                    .contains("username", username[position].toLowerCase())
                    .or()
                    .contains("firstName", username[position].substring(0, 1).toUpperCase()
                            + username[position].substring(1))
                    .or()
                    .contains("lastName", username[position].substring(0, 1).toUpperCase()
                            + username[position].substring(1))
                    .findAllSorted("username", Sort.ASCENDING);


        }
    }
}
