package com.roeico7.dogadopt.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers( {
        "Content-Type: application/json",
        "Authorization:key=AAAAHlubAi4:APA91bEMCofdkR0ZMi1LzQiykFDcHJSjh45g6CwweGnkFLhBREh17pG0SOaJzisFchqUyEjJOvviXy0z54D1cTdI6gcyWfTOaQ5Ab_Ax-aGom3EKkeBAn4jiF9pKK4uRb4NITf9P77Yh"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body  Sender body);


}
