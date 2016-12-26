package com.kilogramm.mattermost.model.entity.filetoattacth;

import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.tools.FileUtil;

import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by kepar on 7.10.16.
 */
public class FileInfoRepository {

    private static FileInfoRepository mInstance;

    public static FileInfoRepository getInstance() {
        if (mInstance == null) {
            mInstance = new FileInfoRepository();
        }
        return mInstance;
    }

    //region Add methods

    public void add(FileInfo item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            realm1.copyToRealm(item);
        });
    }

    //endregion

    // region Update methods


    // endregion

    //region Get methods

    public RealmResults<FileInfo> query() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileInfo.class).findAll();
    }

    public FileInfo get(String id) {
        final Realm realm = Realm.getDefaultInstance();
        return realm.where(FileInfo.class)
                .equalTo("id", id)
                .findFirst();
    }

    // endregion

    // region Check methods

    // endregion

    // region Delete methods

    public void remove(FileInfo item) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<FileInfo> fileToAttachList = realm1.where(FileInfo.class)
                    .equalTo("id", item.getId())
                    .findAll();
            fileToAttachList.deleteFirstFromRealm();
        });
//        realm.close();
    }

    // endregion
}
