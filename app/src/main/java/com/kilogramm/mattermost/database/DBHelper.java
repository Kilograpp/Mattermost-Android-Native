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

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
                + UsersRepository.FIELD_NAME_USER_NAME + " TEXT,"
                + UsersRepository.FIELD_NAME_NICK_NAME + " TEXT,"
                + UsersRepository.FIELD_NAME_EMAIL + " TEXT,"
                + UsersRepository.FIELD_NAME_FIRST_NAME + " TEXT,"
                + UsersRepository.FIELD_NAME_LAST_NAME + " BIGINT,"
                + UsersRepository.FIELD_NAME_UPDATED_AT + " BIGINT,"
                + UsersRepository.FIELD_NAME_LAST_ACTIVITY_AT + " BIGINT,"
                + UsersRepository.FIELD_NAME_LAST_PICTURE_UPDATE + " BIGINT,"
                + UsersRepository.FIELD_NAME_STATUS + " INTEGER,"
                + UsersRepository.FIELD_NAME_IN_TEAM + " BOOLEAN,"
                + UsersRepository.FIELD_NAME_IS_SHOW + " BOOLEAN"
                + ");");
    }

    private void addPostsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE" +  TABLE_NAME_POSTS + "("
                + "_id TEXT PRIMARY KEY"
                + PostsRepository.FIELD_NAME_CREATED_AT + " BIGINT,"
                + PostsRepository.FIELD_NAME_UPDATED_AT + " BIGINT,"
                + PostsRepository.FIELD_NAME_DELETED_AT + " BIGINT,"
                + PostsRepository.FIELD_NAME_USER_ID + " TEXT FOREIGN KEY REFERENCES Users(userId),"
                + PostsRepository.FIELD_NAME_ROOT_ID + " TEXT,"
                + PostsRepository.FIELD_NAME_MESSAGE + " TEXT,"
                + PostsRepository.FIELD_NAME_CHANNEL_ID + " TEXT FOREIGN KEY REFERENCES Channels(channelId),"
                + PostsRepository.FIELD_NAME_PENDING_POST_ID + " TEXT"
                + PostsRepository.FIELD_NAME_PROPS_ID + " INTEGER FOREIGN KEY REFERENCES Props(propsId)"
                + ");");
    }

    private void addChannelsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE" +  TABLE_NAME_CHANNELS + "("
                + "_id TEXT PRIMARY KEY,"
                + ChannelsRepository.FIELD_NAME_TYPE + " INTEGER,"
                + ChannelsRepository.FIELD_NAME_DISPLAY_NAME + " TEXT,"
                + ChannelsRepository.FIELD_NAME_NAME + " TEXT,"
                + ChannelsRepository.FIELD_NAME_HEADER + " TEXT,"
                + ChannelsRepository.FIELD_NAME_PURPOSE + " TEXT,"
                + ChannelsRepository.FIELD_NAME_MESSAGE_COUNT + " BIGINT,"
                + ChannelsRepository.FIELD_NAME_TOTAL_MESSAGE_COUNT + " BIGINT,"
                + ChannelsRepository.FIELD_NAME_MENTIONS_COUNT + " INTEGER"
                + ChannelsRepository.FIELD_NAME_MEMBER_COUNT + " INTEGER,"
                + ChannelsRepository.FIELD_NAME_CREATOR_ID + " TEXT"
                + ChannelsRepository.FIELD_NAME_TEAM_ID + " TEXT FOREIGN KEY REFERENCES Teams(teamId),"
                + ");");
    }

    private void addTeamsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE" + TeamsRepository.TABLE_NAME_TEAMS + "("
                + "_id TEXT PRIMARY KEY,"
                + TeamsRepository.FIELD_NAME_NAME + " TEXT"
                + ");");
    }
}

