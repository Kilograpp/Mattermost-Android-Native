package com.kilogramm.mattermost.model.entity.filetoattacth;

import com.kilogramm.mattermost.model.Repository;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.model.entity.UploadState;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by kepar on 7.10.16.
 */

//TODO for kerap refactor class (remove implements Repository)
public class FileToAttachRepository implements Repository<FileToAttach> {

    private static FileToAttachRepository instance;

    public static FileToAttachRepository getInstance() {
        if (instance == null) {
            instance = new FileToAttachRepository();
        }
        return instance;
    }

    //region Add methods

    @Override
    public void add(FileToAttach item) {
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
            realm1.copyToRealm(item);
        });
    }

    public void addForUpload(String fileName, String filePath) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            Number id = realm1.where(FileToAttach.class).max("id");
            AtomicLong primaryKeyValue;
            if (id != null) {
                primaryKeyValue = new AtomicLong(id.longValue());
            } else {
                primaryKeyValue = new AtomicLong(0);
            }
            FileToAttach fileToAttach = new FileToAttach(primaryKeyValue.incrementAndGet(), fileName, filePath, UploadState.WAITING_FOR_UPLOAD);
            realm1.copyToRealm(fileToAttach);
        });
    }


    public void addForDownload(String fileName) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            Number id = realm1.where(FileToAttach.class).max("id");
            AtomicLong primaryKeyValue;
            if (id != null) {
                primaryKeyValue = new AtomicLong(id.longValue());
            } else {
                primaryKeyValue = new AtomicLong(0);
            }
            FileToAttach fileToAttach = new FileToAttach(primaryKeyValue.incrementAndGet(), fileName, UploadState.WAITING_FOR_DOWNLOAD);
            realm1.copyToRealm(fileToAttach);
        });
    }

    @Override
    public void add(Collection<FileToAttach> items) {

    }

    //endregion

    // region Update methods

    @Override
    public void update(FileToAttach item) {

    }

    public void updateName(String oldName, String fileName) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("fileName", oldName)
                    .findFirst();
            if (fileToAttach != null) {
                fileToAttach.setFileName(fileName);
            }
        });
    }

    public void updateUploadStatus(String fileName, UploadState status) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("fileName", fileName)
                    .findFirst();
            if (fileToAttach != null) {
                fileToAttach.setUploadState(status);
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

    public void updateProgress(long id, int progress) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("id", id)
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

    @Override
    public RealmResults<FileToAttach> query(Specification specification) {
        return null;
    }

    public FileToAttach get(long id){
        final Realm realm = Realm.getDefaultInstance();
        return realm.where(FileToAttach.class)
                .equalTo("id", id)
                .findFirst();
    }

    public FileToAttach get(String fileName){
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
                .or()
                .equalTo("uploadState", UploadState.UPLOADING.name())
                .findFirst();
    }

    public FileToAttach getUndownloadedFile() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileToAttach.class)
                .equalTo("uploadState", UploadState.WAITING_FOR_DOWNLOAD.name())
                .or()
                .equalTo("uploadState", UploadState.DOWNLOADING.name())
                .findFirst();
    }
    // endregion

    // region Check methods

    public boolean haveUploadingFile() {
        Realm realm = Realm.getDefaultInstance();
        FileToAttach fileToAttach = realm.where(FileToAttach.class).equalTo("uploadState", UploadState.UPLOADING.name()).findFirst();
        if (fileToAttach != null && fileToAttach.isValid()) return true;
        return false;
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

    // endregion

    // region Delete methods

    @Override
    public void remove(FileToAttach item) {
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            RealmResults<FileToAttach> fileToAttachList = realm.where(FileToAttach.class)
                    .equalTo("id", item.getId())
                    .findAll();
            fileToAttachList.deleteFirstFromRealm();
        });
        realm.close();
    }

    public void remove(String fileName) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            RealmResults<FileToAttach> fileToAttachList = realm.where(FileToAttach.class)
                    .equalTo("fileName", fileName)
                    .findAll();
            fileToAttachList.deleteFirstFromRealm();
        });
    }

    @Override
    public void remove(Specification specification) {

    }

    public void clearData() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(FileToAttach.class);
        realm.commitTransaction();
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
            fileToAttachList.deleteAllFromRealm();
        });
    }

    // endregion
}
