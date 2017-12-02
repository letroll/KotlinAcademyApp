package com.marcinmoskala.kotlinacademy.backend.api

import com.marcinmoskala.kotlinacademy.backend.Config
import com.marcinmoskala.kotlinacademy.backend.dto.NotificationData
import com.marcinmoskala.kotlinacademy.backend.dto.PushNotificationData
import com.marcinmoskala.kotlinacademy.common.Provider
import okhttp3.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import ru.gildor.coroutines.retrofit.await

interface NotificationsRepository {

    suspend fun sendNotification(title: String, token: String)

    class NotificationsRepositoryImpl(private val secretKey: String) : NotificationsRepository {

        private val api: Api = makeRetrofit("https://fcm.googleapis.com/").create(Api::class.java)

        override suspend fun sendNotification(title: String, token: String) {
            api.pushNotification(
                    authorization = "key=$secretKey",
                    body = PushNotificationData(
                            to = token,
                            notification = NotificationData(
                                    title = title,
                                    body = title + " body"
                            )
                    )
            ).await()
        }

    }

    interface Api {

        @Headers("Content-Type: application/json")
        @POST("fcm/send")
        fun pushNotification(
                @Header("Authorization") authorization: String,
                @Body body: PushNotificationData
        ): Call<Response>
    }

    companion object : Provider<NotificationsRepository?>() {
        override fun create(): NotificationsRepository? = Config.firebaseSecretApiKey?.let(::NotificationsRepositoryImpl)
    }
}