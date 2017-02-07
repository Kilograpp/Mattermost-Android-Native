package com.kilogramm.mattermost.database.repository;

/**
 * Created by kepar on 06.02.17.
 */

public class PostsRepository {

    public static final String TABLE_NAME_POSTS = "Posts";

    public static final String FIELD_NAME_CREATED_AT = "createdAt";
    public static final String FIELD_NAME_UPDATED_AT = "updatedAt";
    public static final String FIELD_NAME_DELETED_AT = "deletedAt";
    public static final String FIELD_NAME_USER_ID = "userId";
    public static final String FIELD_NAME_ROOT_ID = "rootId";
    public static final String FIELD_NAME_MESSAGE = "message";
    public static final String FIELD_NAME_CHANNEL_ID = "channelId";
    public static final String FIELD_NAME_PENDING_POST_ID = "pendingPostId";
    public static final String FIELD_NAME_PROPS_ID = "propsId";
}
