package com.tuan2101.chatme.network

import retrofit2.Retrofit

class ApiClient {
    companion object {
        val retrofit : Retrofit by lazy { Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/fcm/")
            .build() }

//        fun getClient() : Retrofit {
//            if (retrofit == null) {
//                retrofit = Retrofit.Builder()
//                    .baseUrl("https://fcm.googleapis.com/fcm/")
//                    .build()
//            }
//            return retrofit!!
//        }
    }
}