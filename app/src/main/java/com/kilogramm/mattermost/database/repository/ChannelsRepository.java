package com.kilogramm.mattermost.database.repository;

import android.content.ContentValues;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.database.DBHelper;
import com.kilogramm.mattermost.database.MattermostContentProvider;
import com.kilogramm.mattermost.model.response.ResponsedChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kepar on 06.02.17.
 */

public class ChannelsRepository {

    public static final String TABLE_NAME_CHANNELS = "Channels";

    public static final String FIELD_TYPE = "type";
    public static final String FIELD_DISPLAY_NAME = "displayName";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_HEADER = "header";
    public static final String FIELD_PURPOSE = "purpose";
    public static final String FIELD_MESSAGE_COUNT = "messageCount";
    public static final String FIELD_TOTAL_MESSAGE_COUNT = "totalMessageCount";
    public static final String FIELD_MENTIONS_COUNT = "mentionsCount";
    public static final String FIELD_MEMBER_COUNT = "memberCount";
    public static final String FIELD_CREATOR_ID = "creatorId";
    public static final String FIELD_TEAM_ID = "teamId";

    public static void addResponsedChannel(List<ResponsedChannel> channels) {
        List<ContentValues> valueList = new ArrayList<>();

        for (ResponsedChannel item : channels) {
            ContentValues values = new ContentValues();

            values.put(DBHelper.FIELD_COMMON_ID, item.getId());
            values.put(FIELD_TYPE, item.getType());
            values.put(FIELD_DISPLAY_NAME, item.getDisplayName());
            values.put(FIELD_NAME, item.getName());
            values.put(FIELD_HEADER, item.getHeader());
            values.put(FIELD_PURPOSE, item.getPurpose());
            values.put(FIELD_MESSAGE_COUNT, item.getUnreadedMessage());
            values.put(FIELD_TOTAL_MESSAGE_COUNT, item.getTotalMsgCount());
            values.put(FIELD_MENTIONS_COUNT, item.getTotalMsgCount()); //temp
//            values.put(FIELD_MENTIONS_COUNT, item.get());
//            values.put(FIELD_MEMBER_COUNT, item.get());
            values.put(FIELD_CREATOR_ID, item.getCreatorId());
            values.put(FIELD_TEAM_ID, item.getTeamId());

            valueList.add(values);
        }

        ContentValues[] contentValues = new ContentValues[channels.size()];
        valueList.toArray(contentValues);

        MattermostApp.getSingleton()
                .getApplicationContext()
                .getContentResolver()
                .bulkInsert(MattermostContentProvider.CONTENT_URI_CHANNELS, contentValues);
    }
}
