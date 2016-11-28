package com.kilogramm.mattermost.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.R;
import com.kilogramm.mattermost.model.entity.Data;
import com.kilogramm.mattermost.model.entity.channel.Channel;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.kilogramm.mattermost.model.entity.member.MembersRepository;
import com.kilogramm.mattermost.model.entity.post.Post;
import com.kilogramm.mattermost.model.entity.post.PostRepository;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.model.extroInfo.ExtroInfoRepository;
import com.kilogramm.mattermost.model.websocket.WebSocketObj;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.rxtest.GeneralRxActivity;
import com.kilogramm.mattermost.tools.NetworkUtil;
import com.kilogramm.mattermost.view.chat.PostViewHolder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.realm.RealmList;
import rx.schedulers.Schedulers;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;
import static com.kilogramm.mattermost.view.chat.PostViewHolder.getSpannableStringBuilder;
import static com.kilogramm.mattermost.view.direct.WholeDirectListHolder.getImageUrl;

/**
 * Created by Evgeny on 31.08.2016.
 */
public class ManagerBroadcast {
    public static final String TAG = "ObjectUtil";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    public static final String CLOSE_NOTIFICATION = "close_notification";
    public static final String CHANNEL_ID = "CHANNEL_ID";
    public static final String CHANNEL_NAME = "CHANNEL_NAME";
    public static final String CHANNEL_TYPE = "CHANNEL_TYPE";

    private static final int NOTIFY_ID = 1;

    public Context mContext;
    private ApiMethod service;

    private Gson gson;

    public ManagerBroadcast(Context mContext) {
        this.mContext = mContext;
        MattermostApp mMattermostApp = MattermostApp.getSingleton();
        service = mMattermostApp.getMattermostRetrofitService();
        gson = NetworkUtil.createGson();
    }

    public WebSocketObj parseMessage(String message) {
        try {
            return parseWebSocketObject(message, mContext);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private WebSocketObj parseWebSocketObject(String json, Context context) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject dataJSON = jsonObject.getJSONObject(WebSocketObj.DATA);
        WebSocketObj webSocketObj = new WebSocketObj();
        webSocketObj.setDataJSON(jsonObject.getString(WebSocketObj.DATA));
        Log.d(TAG, jsonObject.toString());
        webSocketObj = new Gson().fromJson(jsonObject.toString(), WebSocketObj.class);
        if (webSocketObj.getSeqReplay() != null) {
            webSocketObj.setEvent(WebSocketObj.ALL_USER_STATUS);
        }
        String event = webSocketObj.getEvent();
        Data data = null;
        switch (event) {
            case WebSocketObj.EVENT_CHANNEL_VIEWED:
                break;
            case WebSocketObj.EVENT_POSTED:
                String mentions = null;
                if (dataJSON.has(WebSocketObj.MENTIONS))
                    mentions = dataJSON.getString(WebSocketObj.MENTIONS);
                data = new WebSocketObj.BuilderData()
                        .setChannelDisplayName(dataJSON.getString(WebSocketObj.CHANNEL_DISPLAY_NAME))
                        .setChannelType(dataJSON.getString(WebSocketObj.CHANNEL_TYPE))
                        .setMentions((mentions != null)
                                ? mentions
                                : "")
                        .setSenderName(dataJSON.getString(WebSocketObj.SENDER_NAME))
                        .setTeamId(dataJSON.getString(WebSocketObj.TEAM_ID))
                        .setPost(gson.fromJson(dataJSON.getString(WebSocketObj.CHANNEL_POST), Post.class))
                        .build();

                savePost(data.getPost());
                if (!data.getPost().getUserId().equals(MattermostPreference.getInstance().getMyUserId())) {
//                    createNotification(data.getPost(), context);
                    createNotificationNEW(data.getPost(), context);
                }
                Log.d(TAG, data.getPost().getMessage());
                break;
            case WebSocketObj.EVENT_TYPING:
                data = new WebSocketObj.BuilderData()
                        .setParentId(dataJSON.getString(WebSocketObj.PARENT_ID))
                        .setTeamId(webSocketObj.getTeamId())
                        .build();
                break;
            case WebSocketObj.EVENT_USER_ADDED:
                ExtroInfoRepository.updateAddUser(jsonObject.getString(WebSocketObj.CHANNEL_ID),
                        jsonObject.getString(WebSocketObj.USER_ID));
                break;
            case WebSocketObj.EVENT_USER_REMOVE:
                ExtroInfoRepository.updateRemoveUser(jsonObject.getString(WebSocketObj.CHANNEL_ID),
                        jsonObject.getString(WebSocketObj.USER_ID));
                break;
            case WebSocketObj.EVENT_NEW_USER:
                String ids = jsonObject.getString(WebSocketObj.USER_ID);
                service.getTeamUsers(MattermostPreference.getInstance().getTeamId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(stringUserMap ->
                                UserRepository.add(stringUserMap.get(ids)));
                break;
            case WebSocketObj.EVENT_CHANNEL_DELETED:
                ChannelRepository.remove(new ChannelRepository.ChannelByIdSpecification(
                        jsonObject.getString(WebSocketObj.CHANNEL_ID)));
                ExtroInfoRepository.remove(new ExtroInfoRepository.ExtroInfoByIdSpecification(
                        jsonObject.getString(WebSocketObj.CHANNEL_ID)));
                break;
            case WebSocketObj.EVENT_DIRECT_ADDED:
                service.getChannelsTeam(MattermostPreference.getInstance().getTeamId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(channelsWithMembers -> {
                            RealmList<Channel> channelsList = new RealmList<>();
                            channelsList.addAll(channelsWithMembers.getChannels());
                            ChannelRepository.prepareDirectAndChannelAdd(channelsWithMembers.getChannels());
                            MembersRepository.update(channelsWithMembers.getMembers());
                        });
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
                .setMapUserStatus((new Gson()).fromJson(dataJSON.toString(), new TypeToken<HashMap<String, Object>>() {
                }.getType()))
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

    private Data getUser(JSONObject dataJSON) throws JSONException {
        return new WebSocketObj.BuilderData()
                .setUser(dataJSON.getString(WebSocketObj.USER_ID))
                .build();
    }

//    private static void createNotification(Post post, Context context) {
//        Notification.Builder builder = new Notification.Builder(context)
//                .setContentTitle("New message from " + post.getUser().getUsername())
//                .setContentText(Html.fromHtml(post.getMessage()))
//                .setSmallIcon(R.mipmap.icon);
//        Notification notification = builder.build();
//        notification.flags = Notification.FLAG_AUTO_CANCEL;
//        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(1, notification);
//    }

    private static void createNotificationNEW(Post post, Context context) {
        Notification notification;

        String userName = UserRepository.query(new UserRepository.UserByIdSpecification(post.getUserId()))
                .first()
                .getUsername();

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence receivedPost;
        if (post.getProps() != null)
            receivedPost = PostViewHolder.getMarkdownPost(
                    post.getProps().getAttachments().get(0).getText(), context);
        else
            receivedPost = PostViewHolder.getMarkdownPost(post.getMessage(), context);

        PendingIntent pIntent = PendingIntent.getActivity(context, 0,
                openDialogIntent(post.getChannelId(), context), PendingIntent.FLAG_CANCEL_CURRENT);

        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(context, 0,
                closeNotificationIntent(context), 0);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_custom);
        remoteViews.setImageViewResource(R.id.closeNotification, R.drawable.ic_close_notification);
        remoteViews.setTextViewText(R.id.title, userName);
        remoteViews.setTextViewText(R.id.text, getSpannableStringBuilder(post, context, false, false));
        remoteViews.setOnClickPendingIntent(R.id.closeNotification, pendingIntentClose);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Notification.Builder builder = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.icon)
                    .setContentTitle("New message from " + userName)
                    .setContentText(receivedPost)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pIntent);

            notification = builder.build();
            notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_NO_CLEAR;
            notificationManager.notify(NOTIFY_ID, notification);
        } else {
            NotificationCompat.Builder builderCompat = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_mm)
                    .setContentTitle("New message from " + userName)
                    .setContentText(receivedPost)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(PRIORITY_MAX)
                    .setContentIntent(pIntent)
                    .setContent(remoteViews);

            notification = builderCompat.build();
            notificationManager.notify(NOTIFY_ID, notification);
        }

        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(() ->
                Picasso.with(context.getApplicationContext())
                        .load(getImageUrl(post.getUserId()))
                        .transform(new RoundTransformation(90, 0))
                        .into(remoteViews, R.id.avatar, NOTIFY_ID, notification));
    }

    private static Intent openDialogIntent(String channelId, Context context) {
        Channel channel = ChannelRepository.query(new ChannelRepository.ChannelByIdSpecification(channelId)).first();
        Intent intent = new Intent(context, GeneralRxActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(CHANNEL_ID, channel.getId());
        if (Objects.equals(channel.getType(), Channel.DIRECT)) {
            intent.putExtra(CHANNEL_NAME, channel.getUsername());
        } else {
            intent.putExtra(CHANNEL_NAME, channel.getDisplayName());
        }
        intent.putExtra(CHANNEL_TYPE, channel.getType());
        return intent;
    }

    private static Intent closeNotificationIntent(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), CloseButtonReceiver.class);
        intent.putExtra(NOTIFICATION_ID, NOTIFY_ID);
        intent.setAction(CLOSE_NOTIFICATION);
        return intent;
    }

    public static void savePost(Post post) {
        PostRepository.prepareAndAddPost(post);
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static class CloseButtonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
            NotificationManager manager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);
        }
    }

    private static class RoundTransformation implements com.squareup.picasso.Transformation {

        private final int radius;
        private final int margin;

        public RoundTransformation(final int radius, final int margin) {
            this.radius = radius;
            this.margin = margin;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

            Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawRoundRect(new RectF(margin, margin, source.getWidth() - margin, source.getHeight() - margin), radius, radius, paint);

            if (source != output) {
                source.recycle();
            }
            return output;
        }

        @Override
        public String key() {
            return "rounded";
        }
    }
}