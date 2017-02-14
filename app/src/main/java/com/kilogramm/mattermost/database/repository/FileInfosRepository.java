package com.kilogramm.mattermost.database.repository;

import android.content.ContentValues;
import android.net.Uri;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.database.DBHelper;
import com.kilogramm.mattermost.database.MattermostContentProvider;
import com.kilogramm.mattermost.model.entity.UploadState;
import com.kilogramm.mattermost.model.entity.filetoattacth.FileInfo;

import static com.kilogramm.mattermost.database.DBHelper.FIELD_COMMON_ID;

/**
 * Created by kepar on 06.02.17.
 */

public class FileInfosRepository {

    public static final String TABLE_NAME_FILE_INFOS = "FileInfos";

    public static final String FIELD_NAME = "name";
    public static final String FIELD_MIME_TYPE = "mimeType";
    public static final String FIELD_SIZE = "size";
    public static final String FIELD_HAS_PREVIEW_IMAGE = "hasPreviewImage";
    public static final String FIELD_WIDTH = "width";
    public static final String FIELD_HEIGHT = "height";
    public static final String FIELD_POST_ID = "postId";
    public static final String FIELD_STATE = "state";

    public static void add(FileInfo item) {
        ContentValues values = new ContentValues();
        values.put(FIELD_COMMON_ID, item.getId());
        values.put(FileInfosRepository.FIELD_POST_ID, item.getmPostId());
        values.put(FileInfosRepository.FIELD_MIME_TYPE, item.getmMimeType());
        values.put(FileInfosRepository.FIELD_NAME, item.getmName());
        values.put(FileInfosRepository.FIELD_SIZE, item.getmSize());
        MattermostApp.getSingleton()
                .getApplicationContext()
                .getContentResolver()
                .insert(MattermostContentProvider.CONTENT_URI_FILE_INFOS,
                values);
    }

    public static int updateUploadState(String id, UploadState state){

/*UPDATE Customers
SET City='Hamburg'
WHERE CustomerID=1;*/

        String selection = "SET " + FileInfosRepository.FIELD_STATE + "" +
                "=\'" + state.toString() + "\' WHERE " + FIELD_COMMON_ID +
                "=" + id + ";";
        return MattermostApp.getSingleton()
                .getApplicationContext()
                .getContentResolver()
                .update(MattermostContentProvider.CONTENT_URI_FILE_INFOS,
                        null,
                        selection,
                        null);
    }
}
