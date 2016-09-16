package com.kilogramm.mattermost.service.websocket;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.kilogramm.mattermost.MattermostApp;
import com.kilogramm.mattermost.MattermostPreference;
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

    public static final int TIME_REPEAT_CONNET = 10*1000;
    public static final int TIME_REPEAT_RECONNECT = 30*1000;


    private static WebSocket webSocket = null;

    private WebSocketMessage mWebSocketMessage;

    private HandlerThread handlerThread;

    private Handler handler;

    public WebSocketManager(WebSocketMessage webSocketMessage) {
        this.mWebSocketMessage = webSocketMessage;
        handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        handler = new Handler(looper);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this,TIME_REPEAT_RECONNECT);
                Log.d(TAG, "check state");
                if(webSocket!=null) {
                    Log.d(TAG, "State:"+webSocket.getState().toString());
                    if(webSocket.getState() == WebSocketState.CLOSED){
                        start();
                    }
                }
                else  Log.d(TAG, "not create");
            }
        },TIME_REPEAT_RECONNECT);
        create();
    }

    public void setHeader(WebSocket webSocket){
        List<Cookie> cookies = MattermostPreference.getInstance().getCookies();
        if(cookies!=null && cookies.size()!=0) {
            Cookie cookie = cookies.get(0);
            webSocket.addHeader("Cookie", cookie.name() + "=" + cookie.value());
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
        if(webSocket!=null){
            webSocket.disconnect();
        }
        handlerThread.quit();
    }

    public interface WebSocketMessage{
        void receiveMessage(String message);
    }

}
