package com.kilogramm.mattermost.service;

import android.util.Log;

import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;

/**
 * Created by Evgeny on 23.09.2016.
 */
public class MattermostNotificationManager {

    private static final String TAG = "Websocket";

    private MattermostService service;

    private UserRepository userRepository;

    private ChannelRepository channelRepository;

    private PostRepository postRepository;


    public MattermostNotificationManager(MattermostService service) {
        this.service = service;
        userRepository = new UserRepository();
        channelRepository = new ChannelRepository();
        postRepository = new PostRepository();
    }

    public void handleSocket(WebSocketObj webSocketObj){
        switch (webSocketObj.getEvent()){
            case WebSocketObj.EVENT_CHANNEL_VIEWED:
                break;
            case WebSocketObj.EVENT_POST_DELETED:
                postRepository.remove(webSocketObj.getData().getPost());
                break;
            case WebSocketObj.EVENT_POST_EDITED:
                break;
            case WebSocketObj.EVENT_POSTED:
                break;
            case WebSocketObj.EVENT_STATUS_CHANGE:
                Log.d(TAG, "EVENT_STATUS_CHANGE: useid = "+ webSocketObj.getUserId() + "\n" +
                        "status = " + webSocketObj.getData().getStatus());
                userRepository.updateUserStatus(webSocketObj.getUserId(), webSocketObj.getData().getStatus());
                break;
            case WebSocketObj.EVENT_TYPING:
                break;
            case WebSocketObj.ALL_USER_STATUS:
                for (String s : webSocketObj.getData().getStatusMap().keySet()) {
                    Log.d(TAG, "EVENT_ALL_USER_STATUS: useid = "+ s + "\n" +
                            "status = " + webSocketObj.getData().getStatusMap().get(s));
                    userRepository.updateUserStatus(s,webSocketObj.getData().getStatusMap().get(s));
                }
        }
    }
}
