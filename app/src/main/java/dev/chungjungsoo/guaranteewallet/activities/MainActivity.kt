package dev.chungjungsoo.guaranteewallet.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialog
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.GetInfoResult
import dev.chungjungsoo.guaranteewallet.dataclass.PingResult
import dev.chungjungsoo.guaranteewallet.fragments.InvalidFragment
import dev.chungjungsoo.guaranteewallet.fragments.ManufacturerFragment
import dev.chungjungsoo.guaranteewallet.fragments.ResellerFragment
import dev.chungjungsoo.guaranteewallet.fragments.UserFragment
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
            }
            else {
                Log.d("PW", "Input cancelled")
            }
        }

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
                    } else {
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