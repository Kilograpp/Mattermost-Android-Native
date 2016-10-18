package com.kilogramm.mattermost.model.entity.filetoattacth;

import com.kilogramm.mattermost.model.Repository;
import com.kilogramm.mattermost.model.Specification;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kepar on 7.10.16.
 */

public class FileToAttachRepository implements Repository<FileToAttach>{

    private static FileToAttachRepository instance;

    public static FileToAttachRepository getInstance(){
        if(instance == null){
            instance = new FileToAttachRepository();
        }
        return instance;
    }

    @Override
    public void add(FileToAttach item) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            AtomicLong primaryKeyValue = new AtomicLong(realm.where(FileToAttach.class).max("id").longValue());
            item.setId(primaryKeyValue.incrementAndGet());
            realm.copyToRealm(item);
        });
        realm.close();
    }

    public void add(String fileName, String filePath) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            AtomicLong primaryKeyValue = new AtomicLong(realm.where(FileToAttach.class).max("id").longValue());
            FileToAttach fileToAttach = new FileToAttach(primaryKeyValue.incrementAndGet(), fileName, filePath);
            realm.copyToRealm(fileToAttach);
/*
            FileToAttach fileToAttach = realm1.createObject(FileToAttach.class);
            fileToAttach.setFileName(fileName);
            fileToAttach.setFilePath(filePath);*/
        });
        realm.close();
    }

    @Override
    public void add(Collection<FileToAttach> items) {

    }

    @Override
    public void update(FileToAttach item) {

    }

    public void updateName(String oldName, String fileName){
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("fileName", oldName)
                    .findFirst();
            if(fileToAttach != null) {
                fileToAttach.setFileName(fileName);
            }
        });

        realm.close();
    }

    public void updateUploadStatus(String fileName, boolean status){
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("fileName", fileName)
                    .findFirst();
            if(fileToAttach != null) {
                fileToAttach.setUploaded(status);
            }
        });

        realm.close();
    }

    public void updateProgress(String fileName, int progress){
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("fileName", fileName)
                    .findFirst();
            if(fileToAttach != null && fileToAttach.isValid()) {
                fileToAttach.setProgress(progress);
            }
        });

        realm.close();
    }

    public void updateProgress(Realm realm, String fileName, int progress){
        realm.executeTransaction(realm1 -> {
            FileToAttach fileToAttach = realm1.where(FileToAttach.class)
                    .equalTo("fileName", fileName)
                    .findFirst();
            if(fileToAttach != null && fileToAttach.isValid()) {
                fileToAttach.setProgress(progress);
            }
        });
    }

    @Override
    public void remove(FileToAttach item) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> {
            RealmResults<FileToAttach> fileToAttachList = realm.where(FileToAttach.class)
                    .equalTo("fileName", item.getFileName())
                    .findAll();
            fileToAttachList.deleteFirstFromRealm();
        });
        realm.close();
    }

    @Override
    public void remove(Specification specification) {

    }

    @Override
    public RealmResults<FileToAttach> query(Specification specification) {
        return null;
    }

    public void clearData(){
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(FileToAttach.class);
        realm.commitTransaction();
        realm.close();
    }

    public RealmResults<FileToAttach> query(){
        Realm realm = Realm.getDefaultInstance();
        return realm.where(FileToAttach.class).findAll();
    }

    public boolean haveUnloadedFiles(){
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        RealmResults<FileToAttach> fileToAttachRealmResults =  realm.where(FileToAttach.class).findAll();
        for (FileToAttach fileToAttachRealmResult : fileToAttachRealmResults) {
            if(!fileToAttachRealmResult.isUploaded()){
                realm.commitTransaction();
                realm.close();
                return true;
            }
        }
        realm.commitTransaction();
        realm.close();
        return false;
    }
}
