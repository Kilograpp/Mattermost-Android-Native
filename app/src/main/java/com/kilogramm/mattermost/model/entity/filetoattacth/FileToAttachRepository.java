package com.kilogramm.mattermost.model.entity.filetoattacth;

import android.util.Log;

import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.tools.FileUtil;

import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by kepar on 7.10.16.
 */
public class FileToAttachRepository {

    private static final String TAG = "FileToAttachRepository";

    private static FileToAttachRepository mInstance;

    public static FileToAttachRepository getInstance() {
        if (mInstance == null) {
            mInstance = new FileToAttachRepository();
        }
        return mInstance;
    }

    //region Add methods

    public void add(FileToAttach item) {
        Log.d(TAG, "add: ");
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            Number id = realm1.where(FileToAttach.class).max("id");
            AtomicLong primaryKeyValue;
            if (id != null) {
                primaryKeyValue = new AtomicLong(id.longValue());
            } else {
                primaryKeyValue = new AtomicLong(0);
            }
            item.setId(primaryKeyValue.incrementAndGet());
            item.setProgress(0);
            realm1.copyToRealm(item);
        });
    }
    //endregion

    // region Update methods

    public void updateName(long id, String fileName) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("id", id)
                    .findFirst();
            if (fileToAttach != null) {
                fileToAttach.setFileName(fileName);
            }
        });
    }

    public void updateIdFromServer(long id, String idFromServer) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("id", id)
                    .findFirst();
            if (fileToAttach != null) {
                fileToAttach.setIdFromServer(idFromServer);
            }
        });
    }

    public void updateUploadStatus(long id, UploadState status) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("id", id)
                    .findFirst();
            if (fileToAttach != null) {
                fileToAttach.setUploadState(status);
            }
        });
    }

    public void updateProgress(String fileName, int progress) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("fileName", fileName)
                    .findFirst();
            if (fileToAttach != null && fileToAttach.isValid()) {
                fileToAttach.setProgress(progress);
            }
        });
    }

    // endregion

    //region Get methods

    public RealmResults<FileToAttach> query() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileToAttach.class).findAll();
    }

    public FileToAttach get(long id) {
        final Realm realm = Realm.getDefaultInstance();
        return realm.where(FileToAttach.class)
                .equalTo("id", id)
                .findFirst();
    }

    public FileToAttach get(String fileName) {
        final Realm realm = Realm.getDefaultInstance();
        return realm.where(FileToAttach.class)
                .equalTo("fileName", fileName)
                .findFirst();
    }

    public RealmResults<FileToAttach> getFilesForAttach() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileToAttach.class)
                .equalTo("uploadState", UploadState.WAITING_FOR_UPLOAD.name())
                .or()
                .equalTo("uploadState", UploadState.UPLOADING.name())
                .or()
                .equalTo("uploadState", UploadState.UPLOADED.name())
                .findAllSorted("id");
    }

    public FileToAttach getUnloadedFile() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileToAttach.class)
                .equalTo("uploadState", UploadState.WAITING_FOR_UPLOAD.name())
//                .or()
//                .equalTo("uploadState", UploadState.UPLOADING.name())
                .findFirst();
    }

    public FileToAttach getUndownloadedFile() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileToAttach.class)
                .equalTo("uploadState", UploadState.WAITING_FOR_DOWNLOAD.name())
                .findFirst();
    }
    // endregion

    // region Check methods

    public boolean haveUploadingFile() {
        Realm realm = Realm.getDefaultInstance();
        FileToAttach fileToAttach = realm.where(FileToAttach.class).equalTo("uploadState", UploadState.UPLOADING.name()).findFirst();
        return fileToAttach != null && fileToAttach.isValid();
    }

    public boolean haveUnloadedFiles() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<FileToAttach> fileToAttachRealmResults = realm.where(FileToAttach.class).findAll();
        for (FileToAttach fileToAttachRealmResult : fileToAttachRealmResults) {
            if (fileToAttachRealmResult.getUploadState() == UploadState.WAITING_FOR_UPLOAD ||
                    fileToAttachRealmResult.getUploadState() == UploadState.UPLOADING) {
                realm.commitTransaction();
                realm.close();
                return true;
            }
        }
        realm.commitTransaction();
        return false;
    }

    public boolean haveFilesToAttach(){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<FileToAttach> fileToAttachRealmResults = realm.where(FileToAttach.class).findAll();
        realm.commitTransaction();
        return fileToAttachRealmResults.size() > 0;
    }

    public boolean haveDownloadingFile() {
        Realm realm = Realm.getDefaultInstance();
        FileToAttach fileToAttach = realm.where(FileToAttach.class)
                .equalTo("uploadState", UploadState.DOWNLOADING.name())
                .findFirst();
        return fileToAttach != null && fileToAttach.isValid();
    }

    // endregion

    // region Delete methods

    public void remove(FileToAttach item) {
        Realm realm = Realm.getDefaultInstance();
        if (item != null && item.isTemporary()) {
            FileUtil.getInstance().removeFile(item.getFilePath());
        }
        realm.executeTransaction(realm1 -> {
            RealmResults<FileToAttach> fileToAttachList = realm1.where(FileToAttach.class)
                    .equalTo("id", item.getId())
                    .findAll();
            fileToAttachList.deleteFirstFromRealm();
        });
        realm.close();
    }

    public void remove(long id) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            RealmResults<FileToAttach> fileToAttachList = realm.where(FileToAttach.class)
                    .equalTo("id", id)
                    .findAll();
            fileToAttachList.deleteFirstFromRealm();
        });
    }

    public void deleteUploadedFiles() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<FileToAttach> fileToAttachList = realm.where(FileToAttach.class)
                    .equalTo("uploadState", UploadState.WAITING_FOR_UPLOAD.toString())
                    .or()
                    .equalTo("uploadState", UploadState.UPLOADING.toString())
                    .or()
                    .equalTo("uploadState", UploadState.UPLOADED.toString())
                    .findAll();
            for (FileToAttach fileToAttach : fileToAttachList) {
                if (fileToAttach != null && fileToAttach.isTemporary()) {
                    FileUtil.getInstance().removeFile(fileToAttach.getFilePath());
                }
            }
            fileToAttachList.deleteAllFromRealm();
        });
    }

    // endregion
}
