package dev.chungjungsoo.guaranteewallet.dataclass

import com.google.gson.annotations.SerializedName


data class LoginBody(
    @SerializedName("user_id") val user_id: String,
    @SerializedName("password") val password: String
)

data class LoginResult(
    @SerializedName("jwt") val jwt: String,
    @SerializedName("error") val err: String
)

data class PingResult(
    @SerializedName("status") val status: String,
    @SerializedName("token_status") val token_status: String
)

data class GetInfoResult(
    @SerializedName("uid") val user_id: String,
    @SerializedName("account") val account: String,
    @SerializedName("user_type") val user_type: String,
    @SerializedName("public_key") val key: String,
    @SerializedName("error") val err: String
)

data class GetTokenListBody(
    @SerializedName("address") val address: String
)

data class GetTokenListResult(
    @SerializedName("account") val account: String,
    @SerializedName("tokens") val tokens: List<Int>,
    @SerializedName("error") val err: String
)

data class TokenInfoBody(
    @SerializedName("token_list") val tokens: List<Int>
)

data class TokenInfo(
    @SerializedName("TokenID") val tid: Int,
    @SerializedName("Brand") val brand: String,
    @SerializedName("ProductName") val name: String,
    @SerializedName("ProductionDate") val prodDate: String,
    @SerializedName("ExpirationDate") val expDate: String,
    @SerializedName("Details") val details: String
)

data class TokenInfoResult(
    @SerializedName("tokenInfo") val tokens: List<TokenInfo>,
    @SerializedName("NotFounded") val missing: List<Int>
)

data class ListViewItem(
    val tokenID: Int,
    val brand: String,
    val productName: String,
    val productionDate: String,
    val expirationDate: String,
    val details: String
)

data class CreateQRCodeBody(
    @SerializedName("tid") val tid: Int,
    @SerializedName("owner") val owner: String
)

data class CreateQRCodeResult(
    @SerializedName("result") val result: String?,
    @SerializedName("error") val error: String?
)

data class ValidateTokenResult(
    @SerializedName("result") val result: String,
    @SerializedName("txHistory") val txHistory: List<String>?,
    @SerializedName("info") val info: TokenInfo?,
    @SerializedName("detail") val detail: String?,
    @SerializedName("error") val error: String?
)

data class QRToken(
    @SerializedName("tid") val tid: Int,
    @SerializedName("owner") val owner: String,
    @SerializedName("exp") val exp: Int
)

data class TransferTokenBody(
    @SerializedName("sender") val sender: String,
    @SerializedName("transactor") val transactor: String,
    @SerializedName("receiver") val receiver: String,
    @SerializedName("tid") val tid: Int,
    @SerializedName("wallet_password") val pw: String
)

data class TransferTokenResult(
    @SerializedName("result") val result: String,
    @SerializedName("txhash") val txHash: String,
    @SerializedName("error") val err: String
)

data class GetHistoryBody(
    @SerializedName("address") val address: String
)

data class HistoryItem(
    @SerializedName("token_id") val tid: Int,
    @SerializedName("token_from") val from: String?,
    @SerializedName("token_to") val to: String,
    @SerializedName("event_time") val time: String
)

data class GetHistoryResult(
    @SerializedName("result") val result: List<HistoryItem?>,
    @SerializedName("error") val err: String?
)

data class MintTokenBody(
    @SerializedName("address") val account: String,
    @SerializedName("wallet_password") val pw: String,
    @SerializedName("product_name") val name: String,
    @SerializedName("prod_date") val prod_date: String,
    @SerializedName("exp_date") val exp_date: String,
    @SerializedName("details") val details: String
)

data class MintTokenResult(
    @SerializedName("result") val result: String?,
    @SerializedName("txhash") val txhash: String?,
    @SerializedName("error") val err: String?
)