package com.kilogramm.mattermost.model.entity.user;

import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.RealmSpecification;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.fromnet.ExtraInfo;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
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
        RealmList<User> membersTeam = realm.where(ExtraInfo.class).equalTo("id",
                realm.where(Channel.class).equalTo("name", "town-square").findFirst().getId())
                .findFirst().getMembers();
        RealmQuery realmQuery = realm.where(User.class);
        for (User item : membersTeam) {
                realmQuery.equalTo("id", item.getId()).or();
        }
        realmQuery.equalTo("id", "materMostAll").or().equalTo("id","materMostChannel");
        if (name == null)
            return realmQuery.findAll()
                    .where()
                    .isNotNull("id")
                    .notEqualTo("id", currentUser)
                    .equalTo("deleteAt", 0L)
                    .findAllSorted("username", Sort.ASCENDING);
        else {
            StringBuffer nameBuffer = new StringBuffer(name);
            if (cursorPos < nameBuffer.length())
                nameBuffer.delete(cursorPos, nameBuffer.length());
            String[] username = nameBuffer.toString().split("@");
            nameBuffer = new StringBuffer(username[username.length - 1]);

            return realmQuery.findAll().where()
                    .isNotNull("id")
                    .notEqualTo("id", currentUser)
                    .notEqualTo("id", "System")
                    .equalTo("deleteAt", 0L)
                    .contains("username", nameBuffer.toString().toLowerCase())
                    .or()
                    .isNotNull("id")
                    .notEqualTo("id", "System")
                    .notEqualTo("id", currentUser)
                    .equalTo("deleteAt", 0L)
                    .contains("firstName", nameBuffer.toString().substring(0, 1).toUpperCase()
                            + nameBuffer.toString().substring(1))
                    .or()
                    .isNotNull("id")
                    .notEqualTo("id", "System")
                    .notEqualTo("id", currentUser)
                    .equalTo("deleteAt", 0L)
                    .contains("lastName", nameBuffer.toString().substring(0, 1).toUpperCase()
                            + nameBuffer.toString().substring(1))
                    .findAllSorted("username", Sort.ASCENDING);


        }
    }
}
