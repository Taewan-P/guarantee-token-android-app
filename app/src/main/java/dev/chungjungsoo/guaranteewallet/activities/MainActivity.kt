package dev.chungjungsoo.guaranteewallet.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialog
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.GetInfoResult
import dev.chungjungsoo.guaranteewallet.dataclass.PingResult
import dev.chungjungsoo.guaranteewallet.fragments.InvalidFragment
import dev.chungjungsoo.guaranteewallet.fragments.ManufacturerFragment
import dev.chungjungsoo.guaranteewallet.fragments.ResellerFragment
import dev.chungjungsoo.guaranteewallet.fragments.UserFragment
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import java.io.IOException
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    companion object { lateinit var prefs: PreferenceUtil }
    lateinit var progressDialog : AppCompatDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val fragmentManager = this.supportFragmentManager

        val token :String = prefs.getString("jwt", "")
        progressDialog = AppCompatDialog(this)

        if (token != "") {
            // Move to main screen
            Log.d("MAIN", "Token exists")
            setContentView(R.layout.activity_main)
            showProgress(this)

            thread {
                val pingRes = pingServer(prefs.getString("jwt", null))

                var pingStatus = false
                if (pingRes == null) {
                    Log.d("PING", "Ping Failed")
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Server connection unstable. Please check your network status", Toast.LENGTH_SHORT).show()
                    }
                }
                if (pingRes?.status ?: "error" == "Geth node is connected.") {
                    Log.d("PING", "Ping Successful")
                    if (pingRes?.token_status ?: "invalid" == "valid") {
                        Log.d("PING", "Token is validated")
                        pingStatus = true
                    }
                    else {
                        Log.d("PING", "Token is invalid")
                        Toast.makeText(applicationContext, "Login expired. Please re-login.", Toast.LENGTH_SHORT).show()
                        prefs.resetToken()
                        runOnUiThread {
                            hideProgress()
                            val loginIntent = Intent(applicationContext, LoginActivity::class.java)
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
                        Toast.makeText(this@MainActivity, "Network connection unstable. Please check your network status", Toast.LENGTH_SHORT).show()
                    }
                }

                if (pingStatus) {
                    val infoRes = getInfo(prefs.getString("jwt", null))

                    if (infoRes == null) {
                        Log.d("GETINFO", "User information fetch failed")
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Server connection unstable. Please check your network status", Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (infoRes?.err != null) {
                        runOnUiThread {
                            hideProgress()
                            fragmentManager.beginTransaction()
                                .replace(R.id.main_fragment, InvalidFragment())
                                .commitAllowingStateLoss()
                        }
                    }
                    else {
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
        else {
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


    private fun pingServer(token: String?) : PingResult? {
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

    private fun getInfo(token: String?) : GetInfoResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.getInfo(token).execute()
            response.body()
        } catch (e: IOException) {
            GetInfoResult(user_id = "", account = "", user_type = "", err = "Network Error")
        } catch (e: NullPointerException) {
            GetInfoResult(user_id = "", account = "", user_type = "", err = "Invalid User")
        }
    }


    private fun showProgress(activity: Activity) {
        if (activity.isFinishing) { return }

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
}