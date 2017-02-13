package com.kilogramm.mattermost.rxtest.left_menu.model;

import com.kilogramm.mattermost.model.UserMember;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.response.ResponsedUser;

import java.util.List;
import java.util.Map;

/**
 * Created by Evgeny on 13.01.2017.
 */

public class ResponseLeftMenuData {

    private Map<String, User> stringUserMap;
    private List<UserMember> userMembers;
    private List<Channel> channels;
    private List<Member> members;

    public void setData(Map<String, User> stringUserMap,List<UserMember> userMembers,
                        List<Channel> channels, List<Member> members) {
        this.stringUserMap = stringUserMap;
        this.userMembers = userMembers;
        this.channels = channels;
        this.members = members;
    }

    public Map<String, User> getStringUserMap() {
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
