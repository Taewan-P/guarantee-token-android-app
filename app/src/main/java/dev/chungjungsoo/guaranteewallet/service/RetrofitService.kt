package dev.chungjungsoo.guaranteewallet.service

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import dev.chungjungsoo.guaranteewallet.dataclass.*
import retrofit2.http.GET
import retrofit2.http.Header

interface RetrofitService {
    @GET("/node")
    fun ping(@Header("x-access-token") token: String?) : Call<PingResult>

    @POST("/account/login")
    fun login(@Body body: LoginBody) : Call<LoginResult>

    @GET("/account/get_info")
    fun getInfo(@Header("x-access-token") token: String?) : Call<GetInfoResult>

    @POST("/node/tokens")
    fun getTokenList(@Header("x-access-token") token: String?, @Body body : GetTokenListBody) : Call<GetTokenListResult>
}