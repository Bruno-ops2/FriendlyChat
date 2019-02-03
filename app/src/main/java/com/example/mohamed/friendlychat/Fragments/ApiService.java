package com.example.mohamed.friendlychat.Fragments;

import com.example.mohamed.friendlychat.Notifications.MyResponse;
import com.example.mohamed.friendlychat.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAKclTMcU:APA91bGrrn40XtqhuYmV1FoN3Uy2lOYBIRf__-KrZwh5Q2uyM5X-GJhP9ffqUMXe8FdKLwZpnAexpqxfDoesNZ_aMcRp6p8o5fAxWJkxbjx_fhIziQ0y1Cfwxk1QD6uIcOzIAbMYX1lU"
            }
            )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);


}
