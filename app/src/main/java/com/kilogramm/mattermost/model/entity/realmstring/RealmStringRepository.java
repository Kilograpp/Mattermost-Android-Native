package com.kilogramm.mattermost.model.entity.realmstring;

import com.kilogramm.mattermost.model.entity.RealmString;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileToAttach;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by kepar on 7.10.16.
 */
public class RealmStringRepository {

    private static RealmStringRepository instance;

    public static RealmStringRepository getInstance() {
        if (instance == null) {
            instance = new RealmStringRepository();
        }
        return instance;
    }

    //region Add methods

    public void add(RealmString item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> realm1.copyToRealm(item));
    }

    public void add(String fileId) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> realm1.copyToRealm(new RealmString(fileId)));
    }
    //endregion

    // region Update methods

    public void updateFileSize(String fileName, long fileSize) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmString realmString = realm1.where(RealmString.class)
                    .equalTo("string", fileName)
                    .findFirst();
            if (realmString != null && realmString.isValid()) {
                realmString.setFileSize(fileSize);
            }
        });
    }

    // endregion

    //region Get methods

    public RealmResults<RealmString> query() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(RealmString.class).findAll();
    }

    public RealmString get(String fileName) {
        final Realm realm = Realm.getDefaultInstance();
        return realm.where(RealmString.class)
                .equalTo("string", fileName)
                .findFirst();
    }
    // endregion

    // region Delete methods

    public void remove(String fileName) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            RealmResults<FileToAttach> fileToAttachList = realm.where(FileToAttach.class)
                    .equalTo("fileName", fileName)
                    .findAll();
            fileToAttachList.deleteFirstFromRealm();
        });
    }
    // endregion
}
