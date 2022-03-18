package dev.chungjungsoo.guaranteewallet.service

import dev.chungjungsoo.guaranteewallet.dataclass.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface RetrofitService {
    @GET("/node")
    fun ping(@Header("x-access-token") token: String?): Call<PingResult>

    @POST("/account/login")
    fun login(@Body body: LoginBody): Call<LoginResult>

    @GET("/account/get_info")
    fun getInfo(@Header("x-access-token") token: String?): Call<GetInfoResult>

    @POST("/node/tokens")
    fun getTokenList(
        @Header("x-access-token") token: String?,
        @Body body: GetTokenListBody
    ): Call<GetTokenListResult>

    @POST("/tokens/tokenInfo")
    fun getTokenInfo(@Body body: TokenInfoBody): Call<TokenInfoResult>

    @POST("/tokens/create_qr")
    fun createQRCode(
        @Header("X-access-token") token: String?,
        @Body body: CreateQRCodeBody
    ): Call<CreateQRCodeResult>

    @POST("/node/validate")
    fun validateToken(@Body body: CreateQRCodeBody): Call<ValidateTokenResult>

    @POST("/node/transfer")
    fun transferToken(@Body body: TransferTokenBody): Call<TransferTokenResult>

}