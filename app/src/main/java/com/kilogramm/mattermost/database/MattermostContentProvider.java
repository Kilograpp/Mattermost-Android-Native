package com.kilogramm.mattermost.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kilogramm.mattermost.database.repository.ChannelsRepository;
import com.kilogramm.mattermost.database.repository.PostsRepository;
import com.kilogramm.mattermost.database.repository.TeamsRepository;
import com.kilogramm.mattermost.database.repository.UsersRepository;

/**
 * Created by kepar on 06.02.17.
 */

public class MattermostContentProvider extends ContentProvider {

    // authorities
    public static final String AUTHORITY = "com.kilogramm.mattermost.provider";

    // content uries
    public static final Uri CONTENT_URI_USERS = Uri.parse("content://" + AUTHORITY + "/"
            + UsersRepository.TABLE_NAME_USERS);
    public static final Uri CONTENT_URI_CHANNELS = Uri.parse("content://" + AUTHORITY + "/"
            + ChannelsRepository.TABLE_NAME_CHANNELS);
    public static final Uri CONTENT_URI_POSTS = Uri.parse("content://" + AUTHORITY + "/"
            + PostsRepository.TABLE_NAME_POSTS);
    public static final Uri CONTENT_URI_TEAMS = Uri.parse("content://" + AUTHORITY + "/"
            + PostsRepository.TABLE_NAME_POSTS);

    private static final int CODE_CHANNELS = 1;
    private static final int CODE_CHANNELS_WITH_ID = 2;
    private static final int CODE_POSTS = 3;
    private static final int CODE_POSTS_WITH_ID = 4;
    private static final int CODE_TEAMS = 5;
    private static final int CODE_TEAMS_WITH_ID = 6;
    private static final int CODE_USERS = 7;
    private static final int CODE_USERS_WITH_ID = 8;

    private DBHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, UsersRepository.TABLE_NAME_USERS, CODE_USERS);
        sUriMatcher.addURI(AUTHORITY, UsersRepository.TABLE_NAME_USERS + "/#", CODE_USERS_WITH_ID);
        sUriMatcher.addURI(AUTHORITY, PostsRepository.TABLE_NAME_POSTS, CODE_POSTS);
        sUriMatcher.addURI(AUTHORITY, PostsRepository.TABLE_NAME_POSTS + "/#", CODE_POSTS_WITH_ID);
        sUriMatcher.addURI(AUTHORITY, ChannelsRepository.TABLE_NAME_CHANNELS, CODE_CHANNELS);
        sUriMatcher.addURI(AUTHORITY, ChannelsRepository.TABLE_NAME_CHANNELS + "/#", CODE_CHANNELS_WITH_ID);
        sUriMatcher.addURI(AUTHORITY, TeamsRepository.TABLE_NAME_TEAMS, CODE_TEAMS);
        sUriMatcher.addURI(AUTHORITY, TeamsRepository.TABLE_NAME_TEAMS + "/#", CODE_TEAMS_WITH_ID);
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
        mDatabase = mDbHelper.getWritableDatabase();
        String tableName;
        Uri notificationUri = null;
        switch (sUriMatcher.match(uri)){
            case CODE_CHANNELS:
                tableName = ChannelsRepository.TABLE_NAME_CHANNELS;
                notificationUri = CONTENT_URI_CHANNELS;
                break;
            case CODE_CHANNELS_WITH_ID:
                tableName = ChannelsRepository.TABLE_NAME_CHANNELS;
                notificationUri = CONTENT_URI_CHANNELS;
                break;
            case CODE_POSTS:
                tableName = PostsRepository.TABLE_NAME_POSTS;
                notificationUri = CONTENT_URI_POSTS;
                break;
            case CODE_POSTS_WITH_ID:
                tableName = PostsRepository.TABLE_NAME_POSTS;
                notificationUri = CONTENT_URI_POSTS;
                break;
            case CODE_TEAMS:
                tableName = TeamsRepository.TABLE_NAME_TEAMS;
                notificationUri = CONTENT_URI_TEAMS;
                break;
            case CODE_TEAMS_WITH_ID:
                tableName = TeamsRepository.TABLE_NAME_TEAMS;
                notificationUri = CONTENT_URI_TEAMS;
                break;
            case CODE_USERS:
                tableName = UsersRepository.TABLE_NAME_USERS;
                notificationUri = CONTENT_URI_USERS;
                break;
            case CODE_USERS_WITH_ID:
                tableName = UsersRepository.TABLE_NAME_USERS;
                notificationUri = CONTENT_URI_USERS;
                break;
            default:
                throw new IllegalArgumentException("Wrong or unhandled URI: " + uri);
        }
        Cursor cursor = mDatabase.query(tableName, projection, selection, selectionArgs,
                null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
