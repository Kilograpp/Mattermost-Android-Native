package com.kilogramm.mattermost.database.repository;

import android.content.ContentValues;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.database.MattermostContentProvider;
import com.kilogramm.mattermost.model.entity.Preference.Preferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evgeny on 10.02.2017.
 */

public class PreferencesRepository {

    public static final String TABLE_NAME_PREFERENCES = "Preferences";

    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_USER_ID= "userId";
    public static final String FIELD_VALUE= "value";

    public static void addPreferences(List<Preferences> preferencesList) {
        List<ContentValues> list = new ArrayList<>();
        for (Preferences preferences: preferencesList) {
            ContentValues values = new ContentValues();
            values.put(FIELD_CATEGORY, preferences.getCategory());
            values.put(FIELD_NAME, preferences.getName());
            values.put(FIELD_USER_ID, preferences.getUser_id());
            values.put(FIELD_VALUE, preferences.getValue());
            list.add(values);
        }
        ContentValues[] contentValues = new ContentValues[preferencesList.size()];
        list.toArray(contentValues);
        MattermostApp.getSingleton()
                .getApplicationContext()
                .getContentResolver()
                .bulkInsert(MattermostContentProvider.CONTENT_URI_PREFERENCES, contentValues);
    }

}
