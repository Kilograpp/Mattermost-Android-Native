package com.kilogramm.mattermost;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.network.MattermostRetrofitService;
import com.kilogramm.mattermost.network.TestApiGuthubMethod;
import com.kilogramm.mattermost.network.TestGithubRetrofitService;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import org.java_websocket.client.WebSocketClient;

import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Scheduler;

/**
 * Created by Evgeny on 25.07.2016.
 */
public class MattermostApplication extends Application{

    private ApiMethod mattermostRetrofitService;
    private TestApiGuthubMethod testApiGuthubMethod;
    private Scheduler defaultSubscribeSchedulder;
    private WebSocketClient mWebSocketClient;

    public static  MattermostApplication get(Context context){
        return (MattermostApplication) context.getApplicationContext();
    }

    public void refreshMattermostRetrofitService(){
        mattermostRetrofitService = MattermostRetrofitService.refreshRetrofitService();
    }

    public TestApiGuthubMethod getGithubService(){
        if(testApiGuthubMethod == null){
            testApiGuthubMethod = TestGithubRetrofitService.create();
        }
        return testApiGuthubMethod;
    }
    public ApiMethod getMattermostRetrofitService() {
        if(mattermostRetrofitService == null){
            mattermostRetrofitService = MattermostRetrofitService.create();
        }
        return mattermostRetrofitService;
    }

    public Scheduler getDefaultSubscribeSchedulder() {
        if(defaultSubscribeSchedulder == null){
            defaultSubscribeSchedulder = rx.schedulers.Schedulers.io();
        }
        return defaultSubscribeSchedulder;
    }

    public void setDefaultSubscribeSchedulder(Scheduler scheduler){
        defaultSubscribeSchedulder = scheduler;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //init databsae "realm"
        RealmConfiguration configuration = new RealmConfiguration.Builder(getApplicationContext())
                .name("mattermostDb.realm")
                .build();
        Realm.compactRealm(configuration);
        Realm.removeDefaultConfiguration();
        Realm.setDefaultConfiguration(configuration);

        MattermostPreference.createInstance(getApplicationContext());

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this)
                                .databaseNamePattern(Pattern.compile(".+\\.realm"))
                                .build())
                        .build());
    }

    public void compact() {
        RealmConfiguration configuration = new RealmConfiguration.Builder(getApplicationContext())
                .name("mattermostDb.realm")
                .build();
        Realm.compactRealm(configuration);
    }

    /*private void connectWebSocket() {
        URI uri;
        Map<String, String> headers = new HashMap<>();
        List<Cookie> cookies = MattermostPreference.getInstance().getCookies();
        headers.put("Cookie", cookies.get(0).name() + "=" + cookies.get(0).value());
        try {
            uri = new URI("wss://mattermost.kilograpp.com/api/v3/users/websocket");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        mWebSocketClient = new WebSocketClient(uri, new Draft_10(),headers, 0) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(String s) {
                Log.i("Websocket", "onMessage " + s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
        mWebSocketClient.connect();
        WebSocket.READYSTATE state = mWebSocketClient.getReadyState();
        Log.d("Websocket", "Readi state = " + state.toString());
    }*/
}
