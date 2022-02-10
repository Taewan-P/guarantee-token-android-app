package dev.chungjungsoo.guaranteewallet.dataclass

import com.google.gson.annotations.SerializedName


data class LoginBody (
    @SerializedName("user_id") val user_id: String,
    @SerializedName("password") val password: String
)

data class LoginResult (
    @SerializedName("jwt") val jwt: String,
    @SerializedName("error") val err: String
)

data class PingResult (
    @SerializedName("status") val status : String,
    @SerializedName("token_status") val token_status: String
)

data class GetInfoResult (
    @SerializedName("uid") val user_id: String,
    @SerializedName("account") val account: String,
    @SerializedName("user_type") val user_type: String,
    @SerializedName("error") val err: String
)

data class GetTokenListBody(
    @SerializedName("address") val address : String
)

data class GetTokenListResult (
    @SerializedName("account") val account : String,
    @SerializedName("tokens") val tokens : List<Int>,
    @SerializedName("error") val err : String
)

data class TokenInfoBody(
    @SerializedName("token_list") val tokens: List<Int>
)

data class TokenInfo(
    @SerializedName("TokenID") val tid : Int,
    @SerializedName("Logo") val logo : String,
    @SerializedName("Brand") val brand : String,
    @SerializedName("ProductName") val name : String,
    @SerializedName("ProductionDate") val prodDate : String,
    @SerializedName("ExpirationDate") val expDate : String,
    @SerializedName("Details") val details : String
)

data class TokenInfoResult(
    @SerializedName("tokenInfo") val tokens : List<TokenInfo>,
    @SerializedName("NotFounded") val missing : List<Int>
)

