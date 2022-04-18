package dev.chungjungsoo.guaranteewallet.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.GetInfoResult
import dev.chungjungsoo.guaranteewallet.dataclass.PingResult
import dev.chungjungsoo.guaranteewallet.dataclass.TransferTokenBody
import dev.chungjungsoo.guaranteewallet.dataclass.TransferTokenResult
import dev.chungjungsoo.guaranteewallet.fragments.*
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import dev.chungjungsoo.guaranteewallet.tabfragments.ListTokenFragment
import java.io.IOException
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }
    private lateinit var barcodeLauncher : ActivityResultLauncher<ScanOptions>
    private lateinit var passwordActivityLauncher : ActivityResultLauncher<Intent>
    lateinit var progressDialog: AppCompatDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceUtil(applicationContext)

        barcodeLauncher = this.registerForActivityResult(
            ScanContract()
        ) { result ->
            if (result.contents == null) {
                Log.d("SCAN_QR", "CANCELLED")
            } else {
                Log.d("SCAN_QR", "Scanned: ${result.contents}")
                val fragment = this.supportFragmentManager.fragments[1] as ListTokenFragment
                fragment.setScannedAddress(result.contents)
            }
        }

        passwordActivityLauncher = this.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                Log.d("PW", "PW input successful")
                val fragment = this.supportFragmentManager.fragments[1] as ListTokenFragment
                val info = fragment.getTokenInfo()
                val tid = info.first
                val receiver = info.second
                val pw = it.data?.getStringExtra("pw") ?: ""

                fragment.disableUI()

                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val dialogView = inflater.inflate(R.layout.layout_transfer_result,null)
                val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                thread {
                    val transferResult = transferToken(tid = tid, receiver = receiver, password = pw)

                    if (transferResult == null) {
                        Log.d("TRANSFER", "Transfer failed with result null.")
                    }

                    when {
                        transferResult?.result ?: "failed" == "success" -> {
                            Log.d("TRANSFER", "Transfer success")
                            runOnUiThread {
                                resultText.text = "Transfer Successful."

                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Result")
                                    .setPositiveButton("OK") { _, _ ->
                                        fragment.dismissDialog(tid)
                                    }
                                    .create()

                                alertDialog.setView(dialogView)
                                alertDialog.show()
                            }
                        }
                        transferResult?.err ?: "Unknown error" == "Node Network Error" -> {
                            Log.d("TRANSFER", "Node Network Error")
                            runOnUiThread {
                                fragment.enableUI()

                                resultText.text = "Node Network Error. Try again in a few moments."
                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Error")
                                    .setPositiveButton("OK", null)
                                    .create()

                                alertDialog.setView(dialogView)
                                alertDialog.show()
                        }
                    }
                        transferResult?.err ?: "Unknown error" == "Network Error" -> {
                            Log.d("TRANSFER", "Network Error")
                            runOnUiThread {
                                fragment.enableUI()

                                resultText.text = "Network Error. Try again in a few moments."

                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Error")
                                    .setPositiveButton("OK", null)
                                    .create()

                                alertDialog.setView(dialogView)
                                alertDialog.show()
                            }
                        }
                        transferResult?.err ?: "Unknown error" == "Invalid Request" -> {
                            Log.d("TRANSFER", "Invalid Request")
                            runOnUiThread {
                                fragment.enableUI()

                                resultText.text = "Invalid request. Please check transfer details and try again."

                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Error")
                                    .setPositiveButton("OK", null)
                                    .create()

                                alertDialog.setView(dialogView)
                                alertDialog.show()
                            }
                        }
                        transferResult?.err ?: "Unknown error" == "Authentication Error" -> {
                            Log.d("TRANSFER", "Invalid Request")
                            runOnUiThread {
                                fragment.enableUI()

                                resultText.text = "Authentication Error. Please check your password!"

                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Error")
                                    .setPositiveButton("OK", null)
                                    .create()

                                alertDialog.setView(dialogView)
                                alertDialog.show()
                            }
                        }
                        transferResult?.err ?: "Unknown error" == "Invalid Address" -> {
                            Log.d("TRANSFER", "Invalid Request")
                            runOnUiThread {
                                fragment.enableUI()

                                resultText.text = "Invalid address input. Please check the sender's address."

                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Error")
                                    .setPositiveButton("OK", null)
                                    .create()

                                alertDialog.setView(dialogView)
                                alertDialog.show()
                            }
                        }
                        else -> {
                            Log.e("TRANSFER", "${transferResult?.err}")
                            runOnUiThread {
                                fragment.enableUI()

                                resultText.text = "Unknown error."

                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Error")
                                    .setPositiveButton("OK", null)
                                    .create()

                                alertDialog.setView(dialogView)
                                alertDialog.show()
                            }
                        }
                    }
                }
            }
            else {
                Log.d("PW", "Input cancelled")
                val fragment = this.supportFragmentManager.fragments[1] as ListTokenFragment
                fragment.enableUI()
            }
        }

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val fragmentManager = this.supportFragmentManager

        val token: String = prefs.getString("jwt", "")
        progressDialog = AppCompatDialog(this)

        if (token != "") {
            // Move to main screen
            Log.d("MAIN", "Token exists")
            setContentView(R.layout.activity_main)

            if (savedInstanceState == null) {
                showProgress(this)
                fragmentManager.beginTransaction()
                    .replace(R.id.main_fragment, LoadingFragment())
                    .commitAllowingStateLoss()

                thread {
                    val pingRes = pingServer(prefs.getString("jwt", null))

                    var pingStatus = false
                    if (pingRes == null) {
                        Log.d("PING", "Ping Failed")
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                "Server connection unstable. Please check your network status",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    if (pingRes?.status ?: "error" == "Geth node is connected.") {
                        Log.d("PING", "Ping Successful")
                        if (pingRes?.token_status ?: "invalid" == "valid") {
                            Log.d("PING", "Token is validated")
                            pingStatus = true
                        } else {
                            Log.d("PING", "Token is invalid")
                            prefs.resetToken()
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "Login expired. Please re-login.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                hideProgress()
                                val loginIntent =
                                    Intent(applicationContext, LoginActivity::class.java)
                                startActivity(loginIntent)
                                finish()
                            }
                        }
                    }
                    else {
                        // Ping failed
                        runOnUiThread {
                            hideProgress()
                            Log.d("PING", "Network Error")
                            Toast.makeText(
                                this@MainActivity,
                                "Network connection unstable. Please check your network status",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    if (pingStatus) {
                        val infoRes = getInfo(prefs.getString("jwt", null))

                        if (infoRes == null) {
                            Log.d("GETINFO", "User information fetch failed")
                            runOnUiThread {
                                Toast.makeText(
                                    applicationContext,
                                    "Server connection unstable. Please check your network status",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        if (infoRes?.err != null) {
                            runOnUiThread {
                                hideProgress()
                                fragmentManager.beginTransaction()
                                    .replace(R.id.main_fragment, InvalidFragment())
                                    .commitAllowingStateLoss()
                            }
                        } else {
                            when (infoRes!!.user_type) {
                                "manufacturer" -> {
                                    runOnUiThread {
                                        hideProgress()
                                        fragmentManager.beginTransaction()
                                            .replace(R.id.main_fragment, ManufacturerFragment())
                                            .commitAllowingStateLoss()
                                    }
                                    prefs.setString("account", infoRes.account)
                                    prefs.setString("type", infoRes.user_type)
                                    prefs.setString("key", infoRes.key)

                                }
                                "reseller" -> {
                                    runOnUiThread {
                                        hideProgress()
                                        fragmentManager.beginTransaction()
                                            .replace(R.id.main_fragment, ResellerFragment())
                                            .commitAllowingStateLoss()
                                    }
                                    prefs.setString("account", infoRes.account)
                                    prefs.setString("type", infoRes.user_type)
                                    prefs.setString("key", infoRes.key)
                                }
                                "customer" -> {
                                    runOnUiThread {
                                        hideProgress()
                                        fragmentManager.beginTransaction()
                                            .replace(R.id.main_fragment, UserFragment())
                                            .commitAllowingStateLoss()
                                    }
                                    prefs.setString("account", infoRes.account)
                                    prefs.setString("type", infoRes.user_type)
                                    prefs.setString("key", infoRes.key)
                                }
                                else -> {
                                    runOnUiThread {
                                        hideProgress()
                                        fragmentManager.beginTransaction()
                                            .replace(R.id.main_fragment, InvalidFragment())
                                            .commitAllowingStateLoss()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Move to login screen
            Log.d("MAIN", "Token doesn't exist")
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            this.finish()
        }
    }

    override fun onDestroy() {
        hideProgress()
        super.onDestroy()
    }


    private fun pingServer(token: String?): PingResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.ping(token).execute()
            response.body()
        } catch (e: IOException) {
            PingResult(status = "error", token_status = "invalid")
        } catch (e: NullPointerException) {
            PingResult(status = "error", token_status = "invalid")
        }
    }


    private fun getInfo(token: String?): GetInfoResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.getInfo(token).execute()
            response.body()
        } catch (e: IOException) {
            GetInfoResult(user_id = "", account = "", user_type = "", key="", err = "Network Error")
        } catch (e: NullPointerException) {
            GetInfoResult(user_id = "", account = "", user_type = "", key="", err = "Invalid User")
        }
    }


    private fun transferToken(tid: Int, receiver: String, password: String): TransferTokenResult? {
        val server = RetrofitClass.getInstance()
        val sender = prefs.getString("account", "")

        val txInfo = TransferTokenBody(sender = sender, transactor = sender, receiver = receiver, tid = tid,  pw = password )
        Log.d("INFO", "sender: '$sender', receiver: '$receiver', password: '$password'")
        Log.d("INFO", "$txInfo")

        return try {
            val response = server.transferToken(prefs.getString("jwt", null), txInfo).execute()
            Log.d("TT", response.toString())
            Log.d("TT", response.raw().toString())
            when {
                response.code() == 200 -> {
                    response.body()
                }
                response.code() == 503 -> {
                    TransferTokenResult(result = "failed", txHash = "", err = "Node Network Error")
                }
                response.code() == 401 -> {
                    TransferTokenResult(result = "failed", txHash = "", err = "Authentication Error")
                }
                response.code() == 406 -> {
                    TransferTokenResult(result = "failed", txHash = "", err = "Invalid Address")
                }
                else -> {
                    TransferTokenResult(result = "failed", txHash = "", err = "Unknown Error")
                }
            }
        } catch (e: IOException) {
            TransferTokenResult(result = "failed", txHash = "", err = "Network Error")
        } catch (e: java.lang.NullPointerException) {
            TransferTokenResult(result = "failed", txHash = "", err = "Invalid Request")
        }
    }



    private fun showProgress(activity: Activity) {
        if (activity.isFinishing) {
            return
        }

        if (!progressDialog.isShowing) {
            progressDialog.setCancelable(false)
            progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog.setContentView(R.layout.etc_loading_layout)
            progressDialog.show()
        }

    }


    private fun hideProgress() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }


    fun getQRCodeLauncher(): ActivityResultLauncher<ScanOptions> {
        return barcodeLauncher
    }

    fun getPWInputLauncher(): ActivityResultLauncher<Intent> {
        return passwordActivityLauncher
    }
}