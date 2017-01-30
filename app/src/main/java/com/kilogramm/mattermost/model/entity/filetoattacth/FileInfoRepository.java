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
        realm.executeTransaction(realm1 ->{
            FileInfo fileInfo = realm1.where(FileInfo.class).equalTo("id", item.getId()).findFirst();
            if(fileInfo != null && fileInfo.isValid()){
                item.setUploadState(fileInfo.getUploadState());
            }
            realm1.copyToRealmOrUpdate(item);
        });
    }

    //endregion

    // region Update methods

    public void addForDownload(FileInfo fileInfo) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
                fileInfo.setUploadState(UploadState.WAITING_FOR_DOWNLOAD);
                realm1.copyToRealmOrUpdate(fileInfo);
        });
    }

    public void updateUploadStatus(String fileId, UploadState status) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            FileInfo fileInfo = realm1.where(FileInfo.class)
                    .equalTo("id", fileId)
                    .findFirst();
            if (fileInfo != null) {
                fileInfo.setUploadState(status);
            }
        });
    }

    public void updatePostId(String fileId, String postId) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            FileInfo fileInfo = realm1.where(FileInfo.class)
                    .equalTo("id", fileId)
                    .findFirst();
            if (fileInfo != null) {
                fileInfo.setmPostId(postId);
            }
        });
    }

    // endregion

    //region Get methods

    public RealmResults<FileInfo> query() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileInfo.class).findAll();
    }

    public RealmResults<FileInfo> queryForPostId(String postId) {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileInfo.class)
                .equalTo("mPostId", postId)
                .findAll();
    }

    public FileInfo get(String id) {
        final Realm realm = Realm.getDefaultInstance();
        return realm.where(FileInfo.class)
                .equalTo("id", id)
                .findFirst();
    }

    public FileInfo getUndownloadedFile() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileInfo.class)
                .equalTo("uploadState", UploadState.WAITING_FOR_DOWNLOAD.name())
                .findFirst();
    }

    public RealmResults<FileInfo> getDownloadedFiles() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileInfo.class)
                .equalTo("uploadState", UploadState.DOWNLOADED.name())
                .findAll();
    }

    // endregion

    // region Check methods

    public boolean haveDownloadingFile() {
        Realm realm = Realm.getDefaultInstance();
        FileInfo fileInfo = realm.where(FileInfo.class)
                .equalTo("uploadState", UploadState.DOWNLOADING.name())
                .findFirst();
        return fileInfo != null && fileInfo.isValid();
    }

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
        realm.close();
    }

    public void remove(String fileId) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<FileInfo> fileToAttachList = realm1.where(FileInfo.class)
                    .equalTo("id", fileId)
                    .findAll();
            fileToAttachList.deleteFirstFromRealm();
        });
        realm.close();
    }

    // endregion
}
