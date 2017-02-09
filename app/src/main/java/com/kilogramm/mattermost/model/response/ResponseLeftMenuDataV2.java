package com.kilogramm.mattermost.model.response;

import com.kilogramm.mattermost.model.UserMember;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;

import java.util.List;
import java.util.Map;

/**
 * Created by melkshake on 09.02.17.
 */

public class ResponseLeftMenuDataV2 {
    private Map<String, ResponsedUser> stringUserMap;
    private List<UserMember> userMembers;
    private List<Channel> channels;
    private List<Member> members;

    public void setDataV2(Map<String, ResponsedUser> stringUserMap,List<UserMember> userMembers,
                          List<Channel> channels, List<Member> members) {
        this.stringUserMap = stringUserMap;
        this.userMembers = userMembers;
        this.channels = channels;
        this.members = members;
    }

    public Map<String, ResponsedUser> getStringUserMap() {
        return this.stringUserMap;
    }

    public List<Channel> getChannels() {
        return this.channels;
    }

    public List<Member> getMembers() {
        return this.members;
    }

    public List<UserMember> getUserMembers() {
        return userMembers;
    }
}
