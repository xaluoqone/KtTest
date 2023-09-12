package com.xaluoqone.test

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.net.URL
import java.net.URLDecoder
import java.util.*

// tuya 开发者accessId
const val accessId = "mhy3gxxcad9wf9wjr78e"

// tuya 开发者accessKey
const val accessKey = "7baca903ed1d400fb56db1194755f8a9"

const val baseUrl = "https://openapi.tuyaus.com/"

private var token = ""
val tuyaCloudAPI: TuyaCloudAPI by lazy {
    val loggingInterceptor = HttpLoggingInterceptor {
        println("okhttp: $it")
    }
    loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
            OkHttpClient()
                .newBuilder()
                .addInterceptor(TuyaCloudAPIInterceptor())
                .addInterceptor(loggingInterceptor)
                .build()
        )
        .build()
        .create(TuyaCloudAPI::class.java)
}

interface TuyaCloudAPI {
    @GET("/v1.0/token")
    fun getToken(@Query("grant_type") grantType: Int): Call<TuyaTokenResp>

    @GET("/v1.0/devices/{deviceId}/statistics/hours")
    suspend fun getHourStatistics(
        @Path("deviceId") deviceId: String,
        @Query("code") code: String,
        @Query("end_hour") endHour: String,
        @Query("one_day") oneDay: String,
        @Query("start_hour") startHour: String,
        @Query("stat_type") statType: String
    ): TuyaResp

    @GET("/v1.0/devices/{deviceId}/all-statistic-type")
    suspend fun getStatisticType(@Path("deviceId") deviceId: String): TuyaResp
}

class TuyaCloudAPIInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toUrl().path
        if (token.isEmpty() && !url.contains("v1.0/token")) {
            // token 为空且 当前请求不是请求token
            val tokenResp = tuyaCloudAPI.getToken(1).execute()
            token = tokenResp.body()?.result?.accessToken ?: ""
        }
        val headers = request.headers
        val nonce = headers["nonce"] ?: ""
        val t = "${System.currentTimeMillis()}"
        val stringToSign = stringToSign(request, headers, request.body)
        val headersBuilder = Headers.Builder()
            .add("client_id", accessId)
            .add("lang", "zh")
            .add("t", t)
            .add("sign_method", "HMAC-SHA256")
            .add("Signature-Headers", headers["Signature-Headers"] ?: "")
            .add("nonce", nonce)
            .add("sign", sign(t, nonce, stringToSign))
        if (token.isNotBlank()) {
            headersBuilder.add("access_token", token)
        }
        return chain.proceed(
            request.newBuilder()
                .headers(headersBuilder.build())
                .build()
        )
    }

    private fun stringToSign(request: Request, headers: Headers, body: RequestBody?): String {
        val signLines = mutableListOf<String>()
        signLines.add(request.method.uppercase(Locale.getDefault()))
        var bodyHash =
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"    // body 为空字符串时的hash值
        val contentType = headers["Content-Type"] ?: ""
        if (contentType.contains("application/json", true) && (body?.contentLength() ?: 0) > 0) {
            bodyHash = Sha256Util.encryption(body.toString())
        }
        signLines.add(bodyHash)
        val signatureHeaders = headers["Signature-Headers"]
        if (signatureHeaders != null) {
            val sighHeaderNames =
                signatureHeaders.split("\\s*:\\s*".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            signLines.add(sighHeaderNames.map { obj: String -> obj.trim { it <= ' ' } }
                .filter { it.isNotEmpty() }
                .joinToString("\n") { it + ":" + headers[it] })
        }
        val paramSortedPath = getPathAndSortParam(request.url.toUrl())
        signLines.add("\n$paramSortedPath")
        return signLines.joinToString("\n")
    }

    private fun getPathAndSortParam(url: URL): String {
        return try {
            // supported the query contains zh-Han char
            val query = URLDecoder.decode(url.query, "UTF-8")
            val path = url.path
            if (query.isNullOrBlank()) {
                return path
            }
            val kvMap: MutableMap<String, String> = TreeMap()
            val kvs = query.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (kv in kvs) {
                val kvArr = kv.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (kvArr.size > 1) {
                    kvMap[kvArr[0]] = kvArr[1]
                } else {
                    kvMap[kvArr[0]] = ""
                }
            }
            "$path?" + kvMap.entries.joinToString("&") { (key, value): Map.Entry<String, String> -> "$key=$value" }
        } catch (e: Exception) {
            url.path
        }
    }

    private fun sign(t: String, nonce: String, stringToSign: String): String {
        val sb = StringBuilder(accessId)
        if (token.isNotBlank()) {
            sb.append(token)
        }
        sb.append(t)
        if (nonce.isNotBlank()) {
            sb.append(nonce)
        }
        sb.append(stringToSign)
        return Sha256Util.sha256HMAC(sb.toString(), accessKey)
    }
}
