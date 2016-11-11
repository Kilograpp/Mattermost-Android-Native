package com.kilogramm.mattermost.model.fromnet;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.member.Member;

/**
 * Created by Evgeny on 09.11.2016.
 */

public class ChannelWithMember {
    @SerializedName("channel")
    @Expose
    private Channel channel;

    @SerializedName("member")
    @Expose
    private Member member;


    public Channel getChannel() {
        return channel;
    }

    public Member getMember() {
        return member;
    }
}
