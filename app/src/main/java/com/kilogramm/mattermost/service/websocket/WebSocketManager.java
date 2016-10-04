package com.kilogramm.mattermost.service.websocket;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
import com.kilogramm.mattermost.model.entity.channel.ChannelRepository;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.util.List;

import okhttp3.Cookie;

/**
 * Created by Evgeny on 20.08.2016.
 */
public class WebSocketManager {

    private static final String TAG = "Websocket";

    private static final String HEADER_WEB_SOCKET = "Cookie";
    public static final int TIME_REPEAT_CONNET = 10*1000;
    public static final int TIME_REPEAT_RECONNECT = 30*1000;
    public static final int TIME_REPEAT_UPDATEUSER = 30*1000;

    private static WebSocket webSocket = null;

    private WebSocketMessage mWebSocketMessage;

    private HandlerThread handlerThread;

    private Handler handler;

    private CheckStatusSocket mCheckStatusSocket;


    private ChannelRepository channelRepository;


    public WebSocketManager(WebSocketMessage webSocketMessage) {
        this.mWebSocketMessage = webSocketMessage;

        channelRepository = new ChannelRepository();

        handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);

        mCheckStatusSocket = new CheckStatusSocket();

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        handler.postDelayed(mCheckStatusSocket,TIME_REPEAT_RECONNECT);

    }

    public void setHeader(WebSocket webSocket){
        if(webSocket == null) return;
        List<Cookie> cookies = MattermostPreference.getInstance().getCookies();
        webSocket.removeHeaders(HEADER_WEB_SOCKET);
        if(cookies!=null && cookies.size()!=0) {
            Cookie cookie = cookies.get(0);
            webSocket.addHeader(HEADER_WEB_SOCKET, cookie.name() + "=" + cookie.value());
        }
    }

    public void create() {
        Log.d(TAG, "create");
        try {
            webSocket = new WebSocketFactory().createSocket(MattermostApp.URL_WEB_SOCKET);
            setHeader(webSocket);
            webSocket.addListener(new MWebSocketListener(){
                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    super.onTextMessage(websocket, text);
                    String webMessage = new String(text);
                    Log.d(TAG, "onMessage " + webMessage);
                    websocket.flush();

                    if(mWebSocketMessage!=null) mWebSocketMessage.receiveMessage(webMessage);
                }

                @Override
                public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onPingFrame");
                }

                @Override
                public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    Log.d(TAG, "onPongFrame");
                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                    Log.d(TAG, "onError"+cause.getMessage());
                }

                @Override
                public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
                    switch (newState){
                        case OPEN:{
                            Log.d(TAG, "OPEN");
                            break;
                        }
                        case CLOSING:
                            websocket.sendClose();
                    }
                }
                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    Log.d(TAG, "onDisconnected");
                }


            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onDisconnct() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                webSocket.sendClose();
            }
        });
    }

    public void start() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    connect();
                } catch (Exception e) {
                    Log.d(TAG, "error"+e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void connect() throws Exception {
        Log.d(TAG, "try connect");


        if(webSocket != null && webSocket.getState() != WebSocketState.CREATED){
            webSocket.disconnect();
            webSocket = webSocket.recreate();
            setHeader(webSocket);
        }

        if(webSocket == null){
            create();
        }
        webSocket.connect();

    }

    private boolean hasWebsocket(){
        return ((webSocket!=null)&&(!(webSocket.getState() == WebSocketState.CLOSED)))?true:false;
    }

    public void reconnect() throws WebSocketException {
        if(!hasWebsocket() && !webSocket.isOpen()){
            Log.d(TAG, "reconnect");
        }
    }

    public void onDestroy(){

        handler.removeCallbacks(mCheckStatusSocket);

        if(webSocket!=null){
            webSocket.disconnect();
        }

        handler.post(() -> handlerThread.quit());

    }

    public interface WebSocketMessage{
        void receiveMessage(String message);
    }

    public class CheckStatusSocket implements Runnable{

        @Override
        public void run() {
            handler.postDelayed(this,TIME_REPEAT_RECONNECT);
            Log.d(TAG, "check state");
            if(webSocket!=null) {
                Log.d(TAG, "web socket State:"+webSocket.getState().toString());
                if(webSocket.getState() == WebSocketState.CLOSED){
                    start();
                }
            }
            else  Log.d(TAG, "web socket not created");
        }
    }
    //TODO Review code


}
