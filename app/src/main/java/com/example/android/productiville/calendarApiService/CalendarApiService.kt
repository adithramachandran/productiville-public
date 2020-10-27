package com.example.android.productiville.calendarApiService

import com.example.android.productiville.utils.Constants
import com.google.api.client.util.DateTime
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.time.ZonedDateTime

private val moshi =
    Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

private val retrofit =
    Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(Constants.BASE_URL)
        .build()

interface CalendarApiService {

    @GET("events/")
    fun getEvents(
        @Query("key")
        key: String = Constants.API_KEY,
        @Query("timeMin")
        timeMin: String = ZonedDateTime
            .parse(
                DateTime(System.currentTimeMillis()).toString())
            .withNano(0).toString(),
        @Query("timeMax")
        timeMax: String = ZonedDateTime
            .parse(
                DateTime(System.currentTimeMillis()).toString())
            .plusDays(7).withNano(0).toString(),
        @Query("singleEvents")
        singleEvents: String = "true",
        @Header("Authorization")
        authToken: String,
        @Header("Accept")
        aJson: String = "application/json"
    ) : Deferred<EventResponse>

    @DELETE("events/{id}/")
    fun deleteEvent(
        @Path("id")
        eventId: String,
        @Header("Authorization")
        authToken: String
    ): Call<Void>

    @POST("events/")
    fun insertEvent(
        @Body
        postEvent: PostEvent,
        @Header("Authorization")
        authToken: String
    ): Call<Void>

    @PUT("events/{id}/")
    fun updateEvent(
        @Path("id")
        eventId: String,
        @Body
        postEvent: PostEvent,
        @Header("Authorization")
        authToken: String
    ): Call<Void>
}

object CalendarApi {
    val retrofitService : CalendarApiService by lazy { retrofit.create(CalendarApiService::class.java) }
}
