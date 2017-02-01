package com.kilogramm.mattermost.model.entity;

import java.util.Map;

/**
 * Created by ngers on 20.12.16.
 */

public class BroadcastBilder {
    private String channel_id;
    private Map<String, Boolean> omit_users;
    private String team_id;
    private String user_id;

        public BroadcastBilder setChannelId(String channel_id) {
            this.channel_id = channel_id;
            return this;
        }

        public BroadcastBilder setOmitUsers(Map<String, Boolean> omit_users) {
            this.omit_users = omit_users;
            return this;
        }

        public BroadcastBilder setTeamId(String team_id) {
            this.team_id = team_id;
            return this;
        }

        public BroadcastBilder setUserID(String user_id){
            this.user_id = user_id;
            return this;
        }

        public Broadcast build(){
            return new Broadcast(channel_id,
                    omit_users,
                    team_id,
                    user_id);
        }
}
