package com.kilogramm.mattermost.model.entity.filetoattacth;

import android.content.Context;
import android.net.Uri;

import com.kilogramm.mattermost.model.Repository;
import com.kilogramm.mattermost.model.Specification;
import com.kilogramm.mattermost.tools.FileUtils;

import java.util.Collection;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by kepar on 7.10.16.
 */

public class FileToAttachRepository implements Repository<FileToAttach>{

    private static FileToAttachRepository instance;
/*
    public static void saveAttachedFile(Context context, Uri uri){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        FileToAttach fileToAttach = realm.createObject(FileToAttach.class);
        fileToAttach.setFilePath(FileUtils.getPath(context, uri));
        realm.commitTransaction();
    }*/

    public static FileToAttachRepository getInstance(){
        if(instance == null){
            instance = new FileToAttachRepository();
        }
        return instance;
    }

    @Override
    public void add(FileToAttach item) {
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransaction(realm1 -> realm.insertOrUpdate(item));
        realm.close();
    }

    @Override
    public void add(Collection<FileToAttach> items) {

    }

    @Override
    public void update(FileToAttach item) {

    }

    @Override
    public void remove(FileToAttach item) {

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
}
