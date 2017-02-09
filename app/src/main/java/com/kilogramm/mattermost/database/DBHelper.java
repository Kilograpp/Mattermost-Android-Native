package com.kilogramm.mattermost.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kilogramm.mattermost.database.repository.AttachmentsRepository;
import com.kilogramm.mattermost.database.repository.ChannelsRepository;
import com.kilogramm.mattermost.database.repository.FileInfosRepository;
import com.kilogramm.mattermost.database.repository.PostsRepository;
import com.kilogramm.mattermost.database.repository.PropsRepository;
import com.kilogramm.mattermost.database.repository.TeamsRepository;
import com.kilogramm.mattermost.database.repository.UsersRepository;

import static com.kilogramm.mattermost.database.repository.AttachmentsRepository.TABLE_NAME_ATTACHMENTS;
import static com.kilogramm.mattermost.database.repository.ChannelsRepository.TABLE_NAME_CHANNELS;
import static com.kilogramm.mattermost.database.repository.PostsRepository.TABLE_NAME_POSTS;
import static com.kilogramm.mattermost.database.repository.PropsRepository.TABLE_NAME_PROPS;
import static com.kilogramm.mattermost.database.repository.TeamsRepository.TABLE_NAME_TEAMS;
import static com.kilogramm.mattermost.database.repository.UsersRepository.TABLE_NAME_USERS;

/**
 * Created by kepar on 06.02.17.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "mattermostDb.db";
    private static final int DB_VERSION = 1;

    public static final String FIELD_COMMON_ID = "_id";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        addUsersTable(db);
        addPostsTable(db);
        addChannelsTable(db);
        addTeamsTable(db);
        addFileInfosTable(db);
        addPropsTable(db);
        addAttachmentsTable(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void addUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +  TABLE_NAME_USERS + "("
                + FIELD_COMMON_ID + " TEXT PRIMARY KEY,"
                + UsersRepository.FIELD_USER_NAME + " TEXT,"
                + UsersRepository.FIELD_NICK_NAME + " TEXT,"
                + UsersRepository.FIELD_EMAIL + " TEXT,"
                + UsersRepository.FIELD_FIRST_NAME + " TEXT,"
                + UsersRepository.FIELD_LAST_NAME + " BIGINT,"
                + UsersRepository.FIELD_UPDATED_AT + " BIGINT,"
                + UsersRepository.FIELD_LAST_ACTIVITY_AT + " BIGINT,"
                + UsersRepository.FIELD_LAST_PICTURE_UPDATE + " BIGINT,"
                + UsersRepository.FIELD_STATUS + " INTEGER,"
                + UsersRepository.FIELD_IN_TEAM + " BOOLEAN,"
                + UsersRepository.FIELD_IS_SHOW + " BOOLEAN"
                + ");");
    }

    private void addPostsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +  TABLE_NAME_POSTS + "("
                + FIELD_COMMON_ID + " TEXT PRIMARY KEY,"
                + PostsRepository.FIELD_CREATED_AT + " BIGINT,"
                + PostsRepository.FIELD_UPDATED_AT + " BIGINT,"
                + PostsRepository.FIELD_DELETED_AT + " BIGINT,"
                + PostsRepository.FIELD_USER_ID + " TEXT,"
                + PostsRepository.FIELD_ROOT_ID + " TEXT,"
                + PostsRepository.FIELD_MESSAGE + " TEXT,"
                + PostsRepository.FIELD_CHANNEL_ID + " TEXT,"
                + PostsRepository.FIELD_PENDING_POST_ID + " TEXT,"
                + PostsRepository.FIELD_PROPS_ID + " INTEGER,"
                + PostsRepository.FIELD_TYPE + " TEXT,"
                + "FOREIGN KEY(" + PostsRepository.FIELD_USER_ID + ") REFERENCES " + TABLE_NAME_USERS + "(_id),"
                + "FOREIGN KEY(" + PostsRepository.FIELD_CHANNEL_ID + ") REFERENCES " + TABLE_NAME_CHANNELS + "(_id),"
                + "FOREIGN KEY(" + PostsRepository.FIELD_PROPS_ID + ") REFERENCES " +  TABLE_NAME_PROPS + "(_id)"
                + ");");
    }

    private void addChannelsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +  TABLE_NAME_CHANNELS + "("
                + FIELD_COMMON_ID + " TEXT PRIMARY KEY,"
                + ChannelsRepository.FIELD_TYPE + " INTEGER,"
                + ChannelsRepository.FIELD_DISPLAY_NAME + " TEXT,"
                + ChannelsRepository.FIELD_NAME + " TEXT,"
                + ChannelsRepository.FIELD_HEADER + " TEXT,"
                + ChannelsRepository.FIELD_PURPOSE + " TEXT,"
                + ChannelsRepository.FIELD_MESSAGE_COUNT + " BIGINT,"
                + ChannelsRepository.FIELD_TOTAL_MESSAGE_COUNT + " BIGINT,"
                + ChannelsRepository.FIELD_MENTIONS_COUNT + " INTEGER"
                + ChannelsRepository.FIELD_MEMBER_COUNT + " INTEGER,"
                + ChannelsRepository.FIELD_CREATOR_ID + " TEXT,"
                + ChannelsRepository.FIELD_TEAM_ID + " TEXT,"
                + "FOREIGN KEY(" + ChannelsRepository.FIELD_TEAM_ID + ") REFERENCES " + TABLE_NAME_TEAMS + "(_id)"
                + ");");
    }

    private void addTeamsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME_TEAMS + "("
                + FIELD_COMMON_ID + " TEXT PRIMARY KEY,"
                + TeamsRepository.FIELD_NAME + " TEXT"
                + ");");
    }

    private void addFileInfosTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + FileInfosRepository.TABLE_NAME_FILE_INFOS + "("
                + FIELD_COMMON_ID + " TEXT PRIMARY KEY,"
                + FileInfosRepository.FIELD_NAME + " TEXT,"
                + FileInfosRepository.FIELD_MIME_TYPE + " TEXT,"
                + FileInfosRepository.FIELD_SIZE + " INTEGER,"
                + FileInfosRepository.FIELD_HAS_PREVIEW_IMAGE + " BOOLEAN,"
                + FileInfosRepository.FIELD_WIDTH + " INTEGER,"
                + FileInfosRepository.FIELD_HEIGHT + " INTEGER,"
                + FileInfosRepository.FIELD_POST_ID + " TEXT,"
                + FileInfosRepository.FIELD_STATE + " TEXT,"
                + "FOREIGN KEY(" + FileInfosRepository.FIELD_POST_ID + ") REFERENCES " + TABLE_NAME_POSTS + "(_id)"
                + ");");
    }

    private void addPropsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME_PROPS + "("
                + FIELD_COMMON_ID + " TEXT PRIMARY KEY,"
                + PropsRepository.FIELD_FROM_WEBHOOK + " TEXT,"
                + PropsRepository.FIELD_OVERRIDE_USER_NAME + " TEXT"
                + ");");
    }

    private void addAttachmentsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME_ATTACHMENTS + "("
                + FIELD_COMMON_ID + " TEXT PRIMARY KEY,"
                + AttachmentsRepository.FIELD_COLOR + " TEXT,"
                + AttachmentsRepository.FIELD_FALLBACK + " TEXT,"
                + AttachmentsRepository.FIELD_TEXT + " TEXT,"
                + AttachmentsRepository.FIELD_PROPS_ID + " INTEGER,"
                + "FOREIGN KEY(" + AttachmentsRepository.FIELD_PROPS_ID + ") REFERENCES " + TABLE_NAME_PROPS + "(_id)"
                + ");");
    }
}

