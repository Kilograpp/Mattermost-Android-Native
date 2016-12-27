package com.kilogramm.mattermost.network;

import android.util.Log;

import com.kilogramm.mattermost.model.entity.Preference.Preferences;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.fromnet.ChannelsWithMembers;

import java.util.List;

import rx.Observable;

/**
 * Created by Evgeny on 26.12.2016.
 */

public class ServerMethod {

    private static ServerMethod instance;

    private static ApiMethod mApi;

    private ServerMethod(ApiMethod api){
        mApi = api;
    }

    public static void buildServerMethod(ApiMethod api){
        if(instance==null){
            new ServerMethod(api);
        }
    }

    public static ServerMethod getInstance() {
        return instance;
    }

    public Observable<ChannelsWithMembers> getChannelsTeam(String teamId){
        return mApi.getChannelsTeam(teamId);
    }

    public Observable<Channel> saveOrCreateDirectChannel(List<Preferences> preferences,
                                                         String teamId,
                                                         String userId){
        User user = new User();
        user.setId(userId);
        return Observable.defer(() -> Observable.zip(mApi.save(preferences), mApi.createDirect(teamId, user),
                (b, channel)-> Log.d(TAG, "saveOrCreateDirectChannel: sd"))),
        )
    }

}
