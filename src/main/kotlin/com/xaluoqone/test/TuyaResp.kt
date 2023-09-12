package com.xaluoqone.test

import com.google.gson.annotations.SerializedName

data class TuyaResp(
    val success: Boolean,
    val code: String,
    val msg: String,
    @SerializedName("t")
    val timestamp: Long,
    val tid: String,
)

data class TuyaTokenResp(
    val success: Boolean,
    @SerializedName("t")
    val timestamp: Long,
    val tid: String,
    val result: TuyaTokenResult
)

data class TuyaTokenResult(
    @SerializedName("uid")
    val userId: String,
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expire_time")
    val expireTime: Float
)