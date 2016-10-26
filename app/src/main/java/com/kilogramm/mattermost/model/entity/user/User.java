package com.kilogramm.mattermost.model.entity.user;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.notifyProps.NotifyProps;
import com.kilogramm.mattermost.model.entity.ThemeProps;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Evgeny on 25.07.2016.
 */
public class User extends RealmObject implements Parcelable {

    public static final Long SYSTEM_AT_DATA = 0l;

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    public static final String REFRESH = "refresh";
    public static final String AWAY = "away";

    @PrimaryKey
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("create_at")
    @Expose
    private Long createAt;
    @SerializedName("update_at")
    @Expose
    private Long updateAt;
    @SerializedName("delete_at")
    @Expose
    private Long deleteAt;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("auth_data")
    @Expose
    private String authData;
    @SerializedName("auth_service")
    @Expose
    private String authService;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("email_verified")
    @Expose
    private Boolean emailVerified;
    @SerializedName("nickname")
    @Expose
    private String nickname;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("roles")
    @Expose
    private String roles;
    @SerializedName("last_activity_at")
    @Expose
    private Long lastActivityAt;
    @SerializedName("last_ping_at")
    @Expose
    private Long lastPingAt;
    @SerializedName("allow_marketing")
    @Expose
    private Boolean allowMarketing;
    @SerializedName("notify_props")
    @Expose
    private NotifyProps notifyProps;
    @SerializedName("theme_props")
    @Expose
    private ThemeProps themeProps;
    @SerializedName("last_password_update")
    @Expose
    private Long lastPasswordUpdate;
    @SerializedName("last_picture_update")
    @Expose
    private Long lastPictureUpdate;
    @SerializedName("locale")
    @Expose
    private String locale;

    private String status = "offline";

    public User(){

    }

    public User(String id, String username, String firstName) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.deleteAt = SYSTEM_AT_DATA;
    }

    public User(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     * @return
     * The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The createAt
     */
    public Long getCreateAt() {
        return createAt;
    }

    /**
     *
     * @param createAt
     * The create_at
     */
    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    /**
     *
     * @return
     * The updateAt
     */
    public Long getUpdateAt() {
        return updateAt;
    }

    /**
     *
     * @param updateAt
     * The update_at
     */
    public void setUpdateAt(Long updateAt) {
        this.updateAt = updateAt;
    }

    /**
     *
     * @return
     * The deleteAt
     */
    public Long getDeleteAt() {
        return deleteAt;
    }

    /**
     *
     * @param deleteAt
     * The delete_at
     */
    public void setDeleteAt(Long deleteAt) {
        this.deleteAt = deleteAt;
    }

    /**
     *
     * @return
     * The username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     * The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     * The authData
     */
    public String getAuthData() {
        return authData;
    }

    /**
     *
     * @param authData
     * The auth_data
     */
    public void setAuthData(String authData) {
        this.authData = authData;
    }

    /**
     *
     * @return
     * The authService
     */
    public String getAuthService() {
        return authService;
    }

    /**
     *
     * @param authService
     * The auth_service
     */
    public void setAuthService(String authService) {
        this.authService = authService;
    }

    /**
     *
     * @return
     * The email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @return
     * The emailVerified
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     *
     * @param emailVerified
     * The email_verified
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     *
     * @return
     * The nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     *
     * @param nickname
     * The nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     *
     * @return
     * The firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     * @param firstName
     * The first_name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *
     * @return
     * The lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     *
     * @param lastName
     * The last_name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *
     * @return
     * The roles
     */
    public String getRoles() {
        return roles;
    }

    /**
     *
     * @param roles
     * The roles
     */
    public void setRoles(String roles) {
        this.roles = roles;
    }

    /**
     *
     * @return
     * The lastActivityAt
     */
    public Long getLastActivityAt() {
        return lastActivityAt;
    }

    /**
     *
     * @param lastActivityAt
     * The last_activity_at
     */
    public void setLastActivityAt(Long lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    /**
     *
     * @return
     * The lastPingAt
     */
    public Long getLastPingAt() {
        return lastPingAt;
    }

    /**
     *
     * @param lastPingAt
     * The last_ping_at
     */
    public void setLastPingAt(Long lastPingAt) {
        this.lastPingAt = lastPingAt;
    }

    /**
     *
     * @return
     * The allowMarketing
     */
    public boolean isAllowMarketing() {
        return allowMarketing;
    }

    /**
     *
     * @param allowMarketing
     * The allow_marketing
     */
    public void setAllowMarketing(boolean allowMarketing) {
        this.allowMarketing = allowMarketing;
    }

    /**
     *
     * @return
     * The notifyProps
     */
    public NotifyProps getNotifyProps() {
        return notifyProps;
    }

    /**
     *
     * @param notifyProps
     * The notify_props
     */
    public void setNotifyProps(NotifyProps notifyProps) {
        this.notifyProps = notifyProps;
    }

    /**
     *
     * @return
     * The themeProps
     */
    public ThemeProps getThemeProps() {
        return themeProps;
    }

    /**
     *
     * @param themeProps
     * The theme_props
     */
    public void setThemeProps(ThemeProps themeProps) {
        this.themeProps = themeProps;
    }

    /**
     *
     * @return
     * The lastPasswordUpdate
     */
    public Long getLastPasswordUpdate() {
        return lastPasswordUpdate;
    }

    /**
     *
     * @param lastPasswordUpdate
     * The last_password_update
     */
    public void setLastPasswordUpdate(Long lastPasswordUpdate) {
        this.lastPasswordUpdate = lastPasswordUpdate;
    }

    /**
     *
     * @return
     * The lastPictureUpdate
     */
    public Long getLastPictureUpdate() {
        return lastPictureUpdate;
    }

    /**
     *
     * @param lastPictureUpdate
     * The last_picture_update
     */
    public void setLastPictureUpdate(Long lastPictureUpdate) {
        this.lastPictureUpdate = lastPictureUpdate;
    }

    /**
     *
     * @return
     * The locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     *
     * @param locale
     * The locale
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeValue(this.createAt);
        dest.writeValue(this.updateAt);
        dest.writeValue(this.deleteAt);
        dest.writeString(this.username);
        dest.writeString(this.authData);
        dest.writeString(this.authService);
        dest.writeString(this.email);
        dest.writeValue(this.emailVerified);
        dest.writeString(this.nickname);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.roles);
        dest.writeValue(this.lastActivityAt);
        dest.writeValue(this.lastPingAt);
        dest.writeValue(this.allowMarketing);
        dest.writeParcelable(this.notifyProps, flags);
        dest.writeParcelable(this.themeProps, flags);
        dest.writeValue(this.lastPasswordUpdate);
        dest.writeValue(this.lastPictureUpdate);
        dest.writeString(this.locale);
        dest.writeString(this.status);
    }

    protected User(Parcel in) {
        this.id = in.readString();
        this.createAt = (Long) in.readValue(Long.class.getClassLoader());
        this.updateAt = (Long) in.readValue(Long.class.getClassLoader());
        this.deleteAt = (Long) in.readValue(Long.class.getClassLoader());
        this.username = in.readString();
        this.authData = in.readString();
        this.authService = in.readString();
        this.email = in.readString();
        this.emailVerified = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.nickname = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.roles = in.readString();
        this.lastActivityAt = (Long) in.readValue(Long.class.getClassLoader());
        this.lastPingAt = (Long) in.readValue(Long.class.getClassLoader());
        this.allowMarketing = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.notifyProps = in.readParcelable(NotifyProps.class.getClassLoader());
        this.themeProps = in.readParcelable(ThemeProps.class.getClassLoader());
        this.lastPasswordUpdate = (Long) in.readValue(Long.class.getClassLoader());
        this.lastPictureUpdate = (Long) in.readValue(Long.class.getClassLoader());
        this.locale = in.readString();
        this.status = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
