package com.kilogramm.mattermost.model.entity.user_v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.annotations.PrimaryKey;

/**
 * Created by ivan on 09.02.17.
 */

public class UserV2 {
    public static final String FIELD_USER_NAME = "userName";
    public static final String FIELD_NICK_NAME = "nickName";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_FIRST_NAME = "firstName";
    public static final String FIELD_LAST_NAME = "lastName";
    public static final String FIELD_UPDATED_AT = "updatedAt";
    public static final String FIELD_LAST_ACTIVITY_AT = "lastActivityAt";
    public static final String FIELD_LAST_PICTURE_UPDATE = "lastPictureUpdate";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IN_TEAM = "inTeam";
    public static final String FIELD_IS_SHOW = "isShow";

    @PrimaryKey
    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("nickname")
    @Expose
    private String nickname;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("first_name")
    @Expose
    private String firstName;

    @SerializedName("last_name")
    @Expose
    private String lastName;

    @SerializedName("update_at")
    @Expose
    private Long updateAt;

    @SerializedName("last_activity_at")
    @Expose
    private Long lastActivityAt;

    @SerializedName("last_picture_update")
    @Expose
    private Long lastPictureUpdate;

    @SerializedName("locale")
    @Expose
    private String locale;

    private String status = "offline";
    private String inTeam = "offline";
    private String isShow = "offline";

    public UserV2(){

    }
}
