package com.kilogramm.mattermost.model.entity.user;

import android.util.Log;

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
            StringBuffer nameBuffer = new StringBuffer(name);
            if(cursorPos < nameBuffer.length())
                nameBuffer.delete(cursorPos, nameBuffer.length());
            String[] username = nameBuffer.toString().split("@");
            nameBuffer = new StringBuffer(username[username.length - 1]);
            Log.d("TEST", "toRealmResults: s = " + nameBuffer.toString());


            return realm.where(User.class)
                    .isNotNull("id")
                    .isNotNull("createAt")
                    .notEqualTo("id", currentUser)
                    .equalTo("deleteAt", 0L)
                    .contains("username", nameBuffer.toString().toLowerCase())
                    .or()
                    .isNotNull("id")
                    .isNotNull("createAt")
                    .notEqualTo("id", currentUser)
                    .equalTo("deleteAt", 0L)
                    .contains("firstName",nameBuffer.toString().substring(0, 1).toUpperCase()
                            + nameBuffer.toString().substring(1))
                    .or()
                    .isNotNull("id")
                    .isNotNull("createAt")
                    .notEqualTo("id", currentUser)
                    .equalTo("deleteAt", 0L)
                    .contains("lastName", nameBuffer.toString().substring(0, 1).toUpperCase()
                            + nameBuffer.toString().substring(1))
                    .findAllSorted("username", Sort.ASCENDING);


        }
    }
}
