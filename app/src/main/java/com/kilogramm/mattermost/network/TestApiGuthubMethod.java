package com.kilogramm.mattermost.network;

import com.kilogramm.mattermost.model.entity.user.User;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by Evgeny on 27.07.2016.
 */
// TODO мутод этого функционального интерфейса никогда не вызывается (Kepar)
public interface TestApiGuthubMethod {
    @GET("users")
    Observable<User> getListUser();
}
