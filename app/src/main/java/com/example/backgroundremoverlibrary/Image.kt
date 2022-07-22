package com.example.backgroundremoverlibrary

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface Image {


    @Multipart
    @POST("/lastoria/dress-gallery/")
    suspend fun uploadEmployeeProfileImage(
        @Part img: MultipartBody.Part
    ): Response<ResponseData>

}