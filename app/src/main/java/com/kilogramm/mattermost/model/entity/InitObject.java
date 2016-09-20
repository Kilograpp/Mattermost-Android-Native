package com.kilogramm.mattermost.model.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.user.User;

import java.util.Map;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by Evgeny on 25.07.2016.
 */
public class InitObject extends RealmObject {

        @SerializedName("user")
        @Expose
        private User user;
        @SerializedName("team_members")
        @Expose
        private RealmList<User> teamMembers;
        @SerializedName("teams")
        @Expose
        private RealmList<Team> teamList;
        @SerializedName("direct_profiles")
        @Expose
        @Ignore
        private Map<String, Channel> mapDerectProfile;
        @SerializedName("preferences")
        @Expose
        @Ignore
        private Object preferences;
        @SerializedName("client_cfg")
        @Expose
        private ClientCfg clientCfg;
        @SerializedName("license_cfg")
        @Expose
        private LicenseCfg licenseCfg;
        @SerializedName("no_accounts")
        @Expose
        private boolean noAccounts;

        private RealmList<User> directProfiles;


        public Map<String, Channel> getMapDerectProfile() {
                return mapDerectProfile;
        }

        public void setMapDerectProfile(Map<String, Channel> mapDerectProfile) {
                this.mapDerectProfile = mapDerectProfile;
        }

        /**
         * @return The user
         */
        public Object getUser() {
            return user;
        }

        /**
         * @param user The user
         */
        public void setUser(User user) {
            this.user = user;
        }

        /**
         * @return The teamMembers
         */
        public RealmList<User> getTeamMembers() {
            return teamMembers;
        }

        /**
         * @param teamMembers The team_members
         */
        public void setTeamMembers(RealmList<User> teamMembers) {
            this.teamMembers = teamMembers;
        }

        /**
         * @return The teams
         */
        public RealmList<Team> getTeams() {
            return teamList;
        }

        /**
         * @param teams The teams
         */
        public void setTeams(RealmList<Team> teams) {
                this.teamList = teams;
        }

        /**
         * @return The directProfiles
         */
        public RealmList<User> getDirectProfiles() {
            return directProfiles;
        }

        /**
         * @param directProfiles The direct_profiles
         */
        public void setDirectProfiles(RealmList<User> directProfiles) {
            this.directProfiles = directProfiles;
        }

        /**
         * @return The preferences
         */
        public Object getPreferences() {
            return preferences;
        }

        /**
         * @param preferences The preferences
         */
        public void setPreferences(Object preferences) {
            this.preferences = preferences;
        }

        /**
         * @return The clientCfg
         */
        public ClientCfg getClientCfg() {
            return clientCfg;
        }

        /**
         * @param clientCfg The client_cfg
         */
        public void setClientCfg(ClientCfg clientCfg) {
            this.clientCfg = clientCfg;
        }

        /**
         * @return The licenseCfg
         */
        public LicenseCfg getLicenseCfg() {
            return licenseCfg;
        }

        /**
         * @param licenseCfg The license_cfg
         */
        public void setLicenseCfg(LicenseCfg licenseCfg) {
            this.licenseCfg = licenseCfg;
        }

        /**
         * @return The noAccounts
         */
        public boolean isNoAccounts() {
            return noAccounts;
        }

        /**
         * @param noAccounts The no_accounts
         */
        public void setNoAccounts(boolean noAccounts) {
            this.noAccounts = noAccounts;
        }

    }
