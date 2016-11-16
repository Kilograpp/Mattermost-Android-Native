package com.kilogramm.mattermost.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.Data;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;
import com.kilogramm.mattermost.tools.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class ManagerBroadcast {

    public static final String TAG = "ObjectUtil";

    public Context mContext;


    private Gson gson;

    public ManagerBroadcast(Context mContext) {
        this.mContext = mContext;
        gson = NetworkUtil.createGson();
    }

    public WebSocketObj praseMessage(String message){
        try {
            return parseWebSocketObject(message, mContext);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private WebSocketObj parseWebSocketObject(String json, Context context) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject dataJSON = jsonObject.getJSONObject(WebSocketObj.DATA);
        WebSocketObj webSocketObj = new WebSocketObj();
        webSocketObj.setDataJSON(jsonObject.getString(WebSocketObj.DATA));
        Log.d(TAG,jsonObject.toString());
        webSocketObj = new Gson().fromJson(jsonObject.toString(),WebSocketObj.class);
        if(webSocketObj.getSeqReplay()!=null){
            webSocketObj.setEvent(WebSocketObj.ALL_USER_STATUS);
        }
        String event = webSocketObj.getEvent();
        Data data = null;
        switch (event){
            case WebSocketObj.EVENT_CHANNEL_VIEWED:
                break;
            case WebSocketObj.EVENT_POSTED:
                String mentions = null;
                if(dataJSON.has(WebSocketObj.MENTIONS))
                    mentions = dataJSON.getString(WebSocketObj.MENTIONS);
                data = new WebSocketObj.BuilderData()
                                .setChannelDisplayName(dataJSON.getString(WebSocketObj.CHANNEL_DISPLAY_NAME))
                                .setChannelType(dataJSON.getString(WebSocketObj.CHANNEL_TYPE))
                                .setMentions((mentions!=null)
                                        ?mentions
                                        :"")
                        .setSenderName(dataJSON.getString(WebSocketObj.SENDER_NAME))
                        .setTeamId(dataJSON.getString(WebSocketObj.TEAM_ID))
                        .setPost(gson.fromJson(dataJSON.getString(WebSocketObj.CHANNEL_POST), Post.class))
                        .build();

                savePost(data.getPost());
                if(!data.getPost().getUserId().equals(MattermostPreference.getInstance().getMyUserId())){
                    createNotification(data.getPost(), context);
                    //createNotificationNEW(data.getPost(), context);
                }
                Log.d(TAG, data.getPost().getMessage());
                break;
            case WebSocketObj.EVENT_TYPING:
                data = new WebSocketObj.BuilderData()
                        .setParentId(dataJSON.getString(WebSocketObj.PARENT_ID))
                        .setTeamId(webSocketObj.getTeamId())
                        .build();
                break;
            case WebSocketObj.EVENT_POST_EDITED:
                data = getPost(dataJSON);
                UserRepository.updateUserMessage(data.getPost().getId(), data.getPost().getMessage());
                break;
            case WebSocketObj.EVENT_POST_DELETED:
                data = getPost(dataJSON);
                break;
            case WebSocketObj.EVENT_STATUS_CHANGE:
                data = getStatus(dataJSON);
                break;
            case WebSocketObj.ALL_USER_STATUS:
                data = getMapStatus(dataJSON);
                break;
        }
        webSocketObj.setData(data);
        return webSocketObj;
    }

    private Data getMapStatus(JSONObject dataJSON) {
        return new WebSocketObj.BuilderData()
                .setMapUserStatus((new Gson()).fromJson(dataJSON.toString(),  new TypeToken<HashMap<String, Object>>() {}.getType()))
                .build();
    }

    private Data getStatus(JSONObject dataJSON) throws JSONException {
        return new WebSocketObj.BuilderData()
                .setStatus(dataJSON.getString(WebSocketObj.STATUS))
                .build();
    }

    private Data getPost(JSONObject dataJSON) throws JSONException {
        return new WebSocketObj.BuilderData()
                .setPost(gson.fromJson(dataJSON.getString(WebSocketObj.CHANNEL_POST), Post.class))
                .build();
    }


    private static void createNotification(Post post, Context context) {
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("New message from " + post.getUser().getUsername())
                .setContentText(Html.fromHtml(post.getMessage()))
                .setSmallIcon(R.mipmap.icon);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notification);
    }

    private static void createNotificationNEW(Post post, Context context) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_custom);
        remoteViews.setImageViewResource(R.id.imagenotileft, R.drawable.ic_person_grey_24dp);
        remoteViews.setImageViewResource(R.id.imagenotiright, R.drawable.ic_close_grey_stroke);
        remoteViews.setTextViewText(R.id.title, post.getUser().getUsername());
        remoteViews.setTextViewText(R.id.text, Html.fromHtml(post.getMessage()));

        // open to jump on dialog
//        Intent intent = new Intent(context, ActivityTopSnackBar.class);
//        intent.putExtra("title", post.getUser().getUsername());
//        intent.putExtra("text", post.getMessage());
//        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_person_grey_24dp)
                .setContentTitle("New message from " + post.getUser().getUsername())
                .setContentText(Html.fromHtml(post.getMessage()))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContent(remoteViews);
//                .setContentIntent(pIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    public static void savePost(Post post){
        PostRepository.prepareAndAddPost(post);
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
