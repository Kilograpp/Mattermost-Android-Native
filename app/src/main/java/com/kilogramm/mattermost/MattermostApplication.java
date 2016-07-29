package com.kilogramm.mattermost;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;
import com.kilogramm.mattermost.network.ApiMethod;
import com.kilogramm.mattermost.network.MattermostRetrofitService;
import com.kilogramm.mattermost.network.TestApiGuthubMethod;
import com.kilogramm.mattermost.network.TestGithubRetrofitService;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

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
}
