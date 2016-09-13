package com.kilogramm.mattermost.tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.Post;
import com.kilogramm.mattermost.model.entity.User;
import com.kilogramm.mattermost.model.websocket.WebScoketTyping;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;
import com.kilogramm.mattermost.model.websocket.WebSocketPosted;
import com.kilogramm.mattermost.view.chat.ChatFragment;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class ObjectUtil {

    public static final String TAG = "ObjectUtil";

    public static void parseWebSocketObject(String json, Context context) throws JSONException {
        Gson gson = new Gson();
        JSONObject jsonObject = new JSONObject(json);
        JSONObject props = jsonObject.getJSONObject(WebSocketObj.PROPS);
        WebSocketObj webSocketObj = new WebSocketObj();
        webSocketObj.setTeamId(jsonObject.getString(WebSocketObj.TEAM_ID));
        webSocketObj.setUserId(jsonObject.getString(WebSocketObj.USER_ID));
        webSocketObj.setChannelId(jsonObject.getString(WebSocketObj.CHANNEL_ID));
        webSocketObj.setProps(jsonObject.getString(WebSocketObj.PROPS));
        webSocketObj.setAction(jsonObject.getString(WebSocketObj.ACTION));
        String action = webSocketObj.getAction();
        switch (action){
            case WebSocketObj.ACTION_CHANNEL_VIEWED:
                break;
            case WebSocketObj.ACTION_POSTED:
                WebSocketPosted posted = new WebSocketPosted();
                posted.setChannelDisplayName(props.getString(WebSocketPosted.CHANNEL_DISPLAY_NAME));
                posted.setChannelType(props.getString(WebSocketPosted.CHANNEL_TYPE));
                posted.setMentions(props.getString(WebSocketPosted.MENTIONS));
                posted.setSenderName(props.getString(WebSocketPosted.SENDER_NAME));
                posted.setTeamId(props.getString(WebSocketPosted.TEAM_ID));
                User user = new User();
                user.setId(webSocketObj.getUserId());
                user.setUsername(posted.getSenderName());
                posted.setPost(gson.fromJson(props.getString(WebSocketPosted.CHANNEL_POST), Post.class));
                posted.getPost().setUser(user);
                savePost(posted.getPost());
                if(!posted.getPost().getUserId().equals(MattermostPreference.getInstance().getMyUserId())){
                    createNotification(posted.getPost(), context);
                }
                Log.d(TAG, posted.getPost().getMessage());
                break;
            case WebSocketObj.ACTION_TYPING:
                String channelId = "";
                if((channelId = ChatFragment.getChannelId())!=null){
                    if(channelId.equals(webSocketObj.getChannelId())){
                        ChatFragment.showTyping();
                    }
                }
                break;
        }
    }

    private static void createNotification(Post post, Context context) {
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("New message from " + post.getUser().getUsername())
                .setContentText(post.getMessage())
                .setSmallIcon(R.mipmap.ic_launcher);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(post.getId().hashCode(), notification);
    }

    public static void savePost(Post post){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(post);
        realm.commitTransaction();
        realm.close();
    }


    public static String createTypingObj(String channelId, String teamId, String userId) {
        Gson gson = new Gson();
        WebSocketObj obj = new WebSocketObj();
        obj.setChannelId(channelId);
        obj.setTeamId(teamId);
        obj.setUserId(userId);
        obj.setAction(WebSocketObj.ACTION_TYPING);
        WebScoketTyping typing = new WebScoketTyping();
        obj.setProps(gson.toJson(typing));

        String message = gson.toJson(obj);
        return message;
    }
}
