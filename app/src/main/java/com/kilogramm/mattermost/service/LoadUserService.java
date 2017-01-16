package com.kilogramm.mattermost.service;

import android.databinding.repacked.google.common.util.concurrent.AbstractScheduledService;

import com.kilogramm.mattermost.model.entity.user.User;
import com.kilogramm.mattermost.model.entity.user.UserRepository;
import com.kilogramm.mattermost.network.ServerMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.realm.Realm;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by kraftu on 13.01.17.
 */

public class LoadUserService {

    private ConcurrentHashMap<String,Subscriber> userMap = new ConcurrentHashMap<String,Subscriber>();

    public void startLoadUser(final String userId){
        if(!userMap.containsKey(userId)) {
            Observable<Map<String, User>> observable = ServerMethod.getInstance().getUsersById(new ArrayList<>(Arrays.asList(new String[]{userId})));
            Subscriber subscriber = new Subscriber<Map<String, User>>() {
                @Override
                public void onCompleted() {
                    userMap.remove(userId);
                }

                @Override
                public void onError(Throwable e) {
                    userMap.remove(userId);
                }

                @Override
                public void onNext(Map<String, User> stringUserMap) {
                    if(stringUserMap!=null && !stringUserMap.isEmpty()){
                        UserRepository.add(stringUserMap.values());
                    }
                }
            };
            observable.subscribeOn(Schedulers.io()).subscribe(subscriber);
            userMap.put(userId, subscriber);
        }
    }

    public void cancelLoadUser(String userId){
        Subscriber subscriber = userMap.get(userId);
        if(subscriber!=null){
            subscriber.unsubscribe();
        }
    }

    public void cancelAllLoadUser(){

    }
}
