package com.kilogramm.mattermost.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import static com.kilogramm.mattermost.database.DBHelper.FIELD_COMMON_ID;
import static com.kilogramm.mattermost.database.repository.ChannelsRepository.TABLE_NAME_CHANNELS;
import static com.kilogramm.mattermost.database.repository.PostsRepository.TABLE_NAME_POSTS;
import static com.kilogramm.mattermost.database.repository.PreferencesRepository.TABLE_NAME_PREFERENCES;
import static com.kilogramm.mattermost.database.repository.TeamsRepository.TABLE_NAME_TEAMS;
import static com.kilogramm.mattermost.database.repository.UsersRepository.TABLE_NAME_USERS;

/**
 * Created by kepar on 06.02.17.
 */

public class MattermostContentProvider extends ContentProvider {

    // authorities
    public static final String AUTHORITY = "com.kilogramm.mattermost.provider";

    // content uries
    public static final Uri CONTENT_URI_USERS = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_NAME_USERS);
    public static final Uri CONTENT_URI_CHANNELS = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_NAME_CHANNELS);
    public static final Uri CONTENT_URI_POSTS = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_NAME_POSTS);
    public static final Uri CONTENT_URI_TEAMS = Uri.parse("content://" + AUTHORITY + "/"
            + TABLE_NAME_TEAMS);
    public static final Uri CONTENT_URI_PREFERENCES = Uri.parse("content://" + AUTHORITY +"/"
            + TABLE_NAME_PREFERENCES);

    private static final int CODE_CHANNELS = 1;
    private static final int CODE_CHANNELS_WITH_ID = 2;
    private static final int CODE_POSTS = 3;
    private static final int CODE_POSTS_WITH_ID = 4;
    private static final int CODE_TEAMS = 5;
    private static final int CODE_TEAMS_WITH_ID = 6;
    private static final int CODE_USERS = 7;
    private static final int CODE_USERS_WITH_ID = 8;
    private static final int CODE_PREFERENCES = 9;

    private DBHelper mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_USERS, CODE_USERS);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_USERS + "/*", CODE_USERS_WITH_ID);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_POSTS, CODE_POSTS);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_POSTS + "/*", CODE_POSTS_WITH_ID);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_CHANNELS, CODE_CHANNELS);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_CHANNELS + "/*", CODE_CHANNELS_WITH_ID);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_TEAMS, CODE_TEAMS);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_TEAMS + "/*", CODE_TEAMS_WITH_ID);
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME_PREFERENCES, CODE_PREFERENCES);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase mDatabase = mDbHelper.getWritableDatabase();
        String tableName;
        Uri notificationUri;
        boolean withId = false;
        switch (sUriMatcher.match(uri)) {
            case CODE_CHANNELS:
                tableName = TABLE_NAME_CHANNELS;
                notificationUri = CONTENT_URI_CHANNELS;
                break;
            case CODE_CHANNELS_WITH_ID:
                tableName = TABLE_NAME_CHANNELS;
                notificationUri = CONTENT_URI_CHANNELS;
                withId = true;
                break;
            case CODE_POSTS:
                tableName = TABLE_NAME_POSTS;
                notificationUri = CONTENT_URI_POSTS;
                break;
            case CODE_POSTS_WITH_ID:
                tableName = TABLE_NAME_POSTS;
                notificationUri = CONTENT_URI_POSTS;
                withId = true;
                break;
            case CODE_TEAMS:
                tableName = TABLE_NAME_TEAMS;
                notificationUri = CONTENT_URI_TEAMS;
                break;
            case CODE_TEAMS_WITH_ID:
                tableName = TABLE_NAME_TEAMS;
                notificationUri = CONTENT_URI_TEAMS;
                withId = true;
                break;
            case CODE_USERS:
                tableName = TABLE_NAME_USERS;
                notificationUri = CONTENT_URI_USERS;
                break;
            case CODE_USERS_WITH_ID:
                tableName = TABLE_NAME_USERS;
                notificationUri = CONTENT_URI_USERS;
                withId = true;
                break;
            case CODE_PREFERENCES:
                tableName = TABLE_NAME_PREFERENCES;
                notificationUri = CONTENT_URI_PREFERENCES;
                break;
            default:
                throw new IllegalArgumentException("Wrong or unhandled URI: " + uri);
        }

        if (withId) {
            selection = addIdToSpecification(uri, selection);
        }
        Cursor cursor = mDatabase.query(tableName, projection, selection, selectionArgs,
                null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        Uri resultUri;
        Uri contentUri;
        String tableName;
        switch (sUriMatcher.match(uri)) {
            case CODE_CHANNELS:
                tableName = TABLE_NAME_CHANNELS;
                contentUri = CONTENT_URI_CHANNELS;
                break;
            case CODE_POSTS:
                tableName = TABLE_NAME_POSTS;
                contentUri = CONTENT_URI_POSTS;
                break;
            case CODE_TEAMS:
                tableName = TABLE_NAME_TEAMS;
                contentUri = CONTENT_URI_TEAMS;
                break;
            case CODE_USERS:
                tableName = TABLE_NAME_USERS;
                contentUri = CONTENT_URI_USERS;
                break;
            case CODE_PREFERENCES:
                tableName = TABLE_NAME_PREFERENCES;
                contentUri= CONTENT_URI_PREFERENCES;
                break;
            default:
                throw new IllegalArgumentException("Wrong or unhandled URI: " + uri);
        }
        long rowId = database.insert(tableName, null, values);
        resultUri = ContentUris.withAppendedId(contentUri, rowId);
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String tableName = "" ;

        switch (sUriMatcher.match(uri)){
            case CODE_CHANNELS:
                tableName = TABLE_NAME_CHANNELS;
                break;
            case CODE_POSTS:
                tableName = TABLE_NAME_POSTS;
                break;
            case CODE_TEAMS:
                tableName = TABLE_NAME_TEAMS;
                break;
            case CODE_USERS:
                tableName = TABLE_NAME_USERS;
                break;
            case CODE_PREFERENCES:
                tableName = TABLE_NAME_PREFERENCES;
                break;
            default:
                throw new IllegalArgumentException("Wrong or unhandled URI: " + uri);
        }

        long count = 0;
        db.beginTransaction();
        for (ContentValues value : values) {
            count += db.insert(tableName, null, value);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        getContext().getContentResolver().notifyChange(uri, null);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String tableName;
        boolean withId = false;
        switch (sUriMatcher.match(uri)) {
            case CODE_CHANNELS:
                tableName = TABLE_NAME_CHANNELS;
                break;
            case CODE_CHANNELS_WITH_ID:
                tableName = TABLE_NAME_CHANNELS;
                withId = true;
                break;
            case CODE_POSTS:
                tableName = TABLE_NAME_POSTS;
                break;
            case CODE_POSTS_WITH_ID:
                tableName = TABLE_NAME_POSTS;
                withId = true;
                break;
            case CODE_TEAMS:
                tableName = TABLE_NAME_TEAMS;
                break;
            case CODE_TEAMS_WITH_ID:
                tableName = TABLE_NAME_TEAMS;
                withId = true;
                break;
            case CODE_USERS:
                tableName = TABLE_NAME_USERS;
                break;
            case CODE_USERS_WITH_ID:
                tableName = TABLE_NAME_USERS;
                withId = true;
                break;
            case CODE_PREFERENCES:
                tableName = TABLE_NAME_PREFERENCES;
                break;
            default:
                throw new IllegalArgumentException("Wrong or unhandled URI: " + uri);
        }

        if (withId) {
            selection = addIdToSpecification(uri, selection);
        }
        int count = db.delete(tableName, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String tableName;
        boolean withId = false;
        switch (sUriMatcher.match(uri)) {
            case CODE_CHANNELS:
                tableName = TABLE_NAME_CHANNELS;
                break;
            case CODE_CHANNELS_WITH_ID:
                tableName = TABLE_NAME_CHANNELS;
                withId = true;
                break;
            case CODE_POSTS:
                tableName = TABLE_NAME_POSTS;
                break;
            case CODE_POSTS_WITH_ID:
                tableName = TABLE_NAME_POSTS;
                withId = true;
                break;
            case CODE_TEAMS:
                tableName = TABLE_NAME_TEAMS;
                break;
            case CODE_TEAMS_WITH_ID:
                tableName = TABLE_NAME_TEAMS;
                withId = true;
                break;
            case CODE_USERS:
                tableName = TABLE_NAME_USERS;
                break;
            case CODE_USERS_WITH_ID:
                tableName = TABLE_NAME_USERS;
                withId = true;
                break;
            case CODE_PREFERENCES:
                tableName = TABLE_NAME_PREFERENCES;
                break;
            default:
                throw new IllegalArgumentException("Wrong or unhandled URI: " + uri);
        }

        if (withId) {
            selection = addIdToSpecification(uri, selection);
        }
        int count = db.update(tableName, values, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    /**
     * If user specifies uri with item id, call this method to add this id to specification
     *
     * @param uri       passed to ContentProvider uri
     * @param selection specification
     * @return new specification with item id
     */
    private String addIdToSpecification(Uri uri, String selection) {
        String id = uri.getLastPathSegment();
        if (TextUtils.isEmpty(selection)) {
            selection = FIELD_COMMON_ID + " = " + id;
        } else {
            selection = selection + "AND" + FIELD_COMMON_ID + " = " + id;
        }
        return selection;
    }
}
