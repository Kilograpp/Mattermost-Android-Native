package com.kilogramm.mattermost.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kilogramm.mattermost.database.repository.ChannelsRepository;
import com.kilogramm.mattermost.database.repository.PostsRepository;
import com.kilogramm.mattermost.database.repository.TeamsRepository;
import com.kilogramm.mattermost.database.repository.UsersRepository;

import static com.kilogramm.mattermost.database.repository.ChannelsRepository.TABLE_NAME_CHANNELS;
import static com.kilogramm.mattermost.database.repository.PostsRepository.TABLE_NAME_POSTS;
import static com.kilogramm.mattermost.database.repository.UsersRepository.TABLE_NAME_USERS;

/**
 * Created by kepar on 06.02.17.
 */

public class DBHelper extends SQLiteOpenHelper {

    static final String DB_NAME = "mattermostDb, db";
    static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        addUsersTable(db);
        addPostsTable(db);
        addChannelsTable(db);
        addTeamsTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void addUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE" +  TABLE_NAME_USERS + "("
                + "_id TEXT PRIMARY KEY,"
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
        db.execSQL("CREATE TABLE" +  TABLE_NAME_POSTS + "("
                + "_id TEXT PRIMARY KEY"
                + PostsRepository.FIELD_CREATED_AT + " BIGINT,"
                + PostsRepository.FIELD_UPDATED_AT + " BIGINT,"
                + PostsRepository.FIELD_DELETED_AT + " BIGINT,"
                + PostsRepository.FIELD_USER_ID + " TEXT FOREIGN KEY REFERENCES Users(userId),"
                + PostsRepository.FIELD_ROOT_ID + " TEXT,"
                + PostsRepository.FIELD_MESSAGE + " TEXT,"
                + PostsRepository.FIELD_CHANNEL_ID + " TEXT FOREIGN KEY REFERENCES Channels(channelId),"
                + PostsRepository.FIELD_PENDING_POST_ID + " TEXT,"
                + PostsRepository.FIELD_PROPS_ID + " INTEGER FOREIGN KEY REFERENCES Props(propsId),"
                + PostsRepository.FIELD_TYPE + " TEXT"
                + ");");
    }

    private void addChannelsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE" +  TABLE_NAME_CHANNELS + "("
                + "_id TEXT PRIMARY KEY,"
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
                + ChannelsRepository.FIELD_TEAM_ID + " TEXT FOREIGN KEY REFERENCES Teams(teamId)"
                + ");");
    }

    private void addTeamsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE" + TeamsRepository.TABLE_NAME_TEAMS + "("
                + "_id TEXT PRIMARY KEY,"
                + TeamsRepository.FIELD_NAME + " TEXT"
                + ");");
    }
}

