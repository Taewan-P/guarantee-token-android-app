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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.*
import dev.chungjungsoo.guaranteewallet.fragments.*
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import dev.chungjungsoo.guaranteewallet.tabfragments.ApprovedTokenFragment
import dev.chungjungsoo.guaranteewallet.tabfragments.ListTokenFragment
import java.io.IOException
import java.lang.ClassCastException
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }
    private lateinit var barcodeLauncher : ActivityResultLauncher<ScanOptions>
    private lateinit var approvedBarcodeLauncher : ActivityResultLauncher<ScanOptions>
    private lateinit var passwordActivityLauncher : ActivityResultLauncher<Intent>
    private lateinit var approvedPasswordActivityLauncher : ActivityResultLauncher<Intent>
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
                try {
                    this.supportFragmentManager.beginTransaction().replace(R.id.main_fragment, ListTokenFragment())
                    this.supportFragmentManager.beginTransaction().addToBackStack(null)
                    val fragments = this.supportFragmentManager.fragments
                    Log.d("SCAN_QR", fragments.toString())
                    val fragment = this.supportFragmentManager.fragments.find { it is ListTokenFragment } as ListTokenFragment
                    fragment.setScannedAddress(result.contents)
                } catch (e: ClassCastException) {
                    Log.d("SCAN_QR", this.supportFragmentManager.fragments.toString())
                    Log.e("SCAN_QR", "Class cast exception occurred")
                }
            }
        }

        approvedBarcodeLauncher = this.registerForActivityResult(
            ScanContract()
        ) { result ->
            if (result.contents == null) {
                Log.d("SCAN_QR", "CANCELLED")
            } else {
                Log.d("SCAN_QR", "Scanned: ${result.contents}")
                try {
                    this.supportFragmentManager.beginTransaction().replace(R.id.main_fragment, ApprovedTokenFragment())
                    this.supportFragmentManager.beginTransaction().addToBackStack(null)
                    val fragments = this.supportFragmentManager.fragments
                    Log.d("SCAN_QR", fragments.toString())
                    val fragment = this.supportFragmentManager.fragments.find { it is ApprovedTokenFragment } as ApprovedTokenFragment
                    fragment.setScannedAddress(result.contents)
                } catch (e: ClassCastException) {
                    Log.d("SCAN_QR", this.supportFragmentManager.fragments.toString())
                    Log.e("SCAN_QR", "Class cast exception occurred")
                }
            }
        }

        passwordActivityLauncher = this.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                Log.d("PW", "PW input successful")
                this.supportFragmentManager.beginTransaction().replace(R.id.main_fragment, ListTokenFragment())
                this.supportFragmentManager.beginTransaction().addToBackStack(null)
                val fragments = this.supportFragmentManager.fragments
                Log.d("PW", fragments.toString())
                val fragment = this.supportFragmentManager.fragments.find { it is ListTokenFragment } as ListTokenFragment
                val info = fragment.getTokenInfo()
                val tid = info.first
                val receiver = info.second
                val type = info.third
                val pw = activityResult.data?.getStringExtra("pw") ?: ""

                fragment.disableUI()

                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val dialogView = inflater.inflate(R.layout.layout_transfer_result,null)
                val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                if (type == "send") {
                    thread {
                        val transferResult = transferToken(tid = tid, receiver = receiver, password = pw)

                        if (transferResult == null) {
                            Log.e("TRANSFER", "Transfer failed with result null.")
                        }

                        when {
                            (transferResult?.result ?: "failed") == "success" -> {
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
                            (transferResult?.err ?: "Unknown error") == "Node Network Error" -> {
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
                            (transferResult?.err ?: "Unknown error") == "Network Error" -> {
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
                            (transferResult?.err ?: "Unknown error") == "Invalid Request" -> {
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
                            (transferResult?.err ?: "Unknown error") == "Authentication Error" -> {
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
                            (transferResult?.err ?: "Unknown error") == "Invalid Address" -> {
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
                else if (type == "approve"){
                    // Approve
                    thread {
                        val approveResult = approveToken(tid = tid, receiver = receiver, password = pw)

                        if (approveResult == null) {
                            Log.e("APPROVE", "Approve failed with result null.")
                        }
                        if (approveResult != null) {
                            when {
                                approveResult.result == "success" -> {
                                    // Success
                                    Log.d("APPROVE", "Approve successful")
                                    runOnUiThread {
                                        resultText.text = "Approve Successful."

                                        val alertDialog = AlertDialog.Builder(this)
                                            .setTitle("Result")
                                            .setPositiveButton("OK") { _, _ ->
                                                fragment.dismissDialogWithoutDeletion()
                                            }
                                            .create()

                                        alertDialog.setView(dialogView)
                                        alertDialog.show()
                                    }
                                }

                                approveResult.err == "Node Network Error" -> {
                                    Log.e("APPROVE", "Node Network Error")
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

                                approveResult.err == "Authentication Error" -> {
                                    Log.e("APPROVE", "Authentication Error")
                                    runOnUiThread {
                                        fragment.enableUI()

                                        resultText.text = "Authentication Error"
                                        val alertDialog = AlertDialog.Builder(this)
                                            .setTitle("Error")
                                            .setPositiveButton("OK", null)
                                            .create()

                                        alertDialog.setView(dialogView)
                                        alertDialog.show()
                                    }
                                }

                                approveResult.err == "Invalid Address" -> {
                                    Log.e("APPROVE", "Invalid address")
                                    runOnUiThread {
                                        fragment.enableUI()

                                        resultText.text = "Invalid address. Please check again."
                                        val alertDialog = AlertDialog.Builder(this)
                                            .setTitle("Error")
                                            .setPositiveButton("OK", null)
                                            .create()

                                        alertDialog.setView(dialogView)
                                        alertDialog.show()
                                    }
                                }

                                approveResult.err == "Unknown Error" -> {
                                    Log.e("APPROVE", "Unknown error in request")
                                    runOnUiThread {
                                        fragment.enableUI()

                                        resultText.text = "Unknown error. Please try again."
                                        val alertDialog = AlertDialog.Builder(this)
                                            .setTitle("Error")
                                            .setPositiveButton("OK", null)
                                            .create()

                                        alertDialog.setView(dialogView)
                                        alertDialog.show()
                                    }
                                }

                                approveResult.err == "Network Error" -> {
                                    Log.e("APPROVE", "Network error")
                                    runOnUiThread {
                                        fragment.enableUI()

                                        resultText.text = "Network Error. Try again."
                                        val alertDialog = AlertDialog.Builder(this)
                                            .setTitle("Error")
                                            .setPositiveButton("OK", null)
                                            .create()

                                        alertDialog.setView(dialogView)
                                        alertDialog.show()
                                    }
                                }

                                approveResult.err == "Invalid Request" -> {
                                    Log.e("APPROVE", "Invalid request")
                                    runOnUiThread {
                                        fragment.enableUI()

                                        resultText.text = "Invalid Request. Please check your parameters"
                                        val alertDialog = AlertDialog.Builder(this)
                                            .setTitle("Error")
                                            .setPositiveButton("OK", null)
                                            .create()

                                        alertDialog.setView(dialogView)
                                        alertDialog.show()
                                    }
                                }

                                else -> {
                                    Log.e("APPROVE", "Unknown error in exception")
                                    runOnUiThread {
                                        fragment.enableUI()

                                        resultText.text = "Unknown error. Please try again."
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
                }
            }
            else {
                Log.d("PW", "Input cancelled")
                this.supportFragmentManager.beginTransaction().replace(R.id.main_fragment, ListTokenFragment())
                this.supportFragmentManager.beginTransaction().addToBackStack(null)
                val fragments = this.supportFragmentManager.fragments
                Log.d("PW", fragments.toString())
                val fragment = this.supportFragmentManager.fragments.find { it is ListTokenFragment } as ListTokenFragment
                fragment.enableUI()
            }
        }

        approvedPasswordActivityLauncher = this.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                Log.d("PW", "PW input successful")
                this.supportFragmentManager.beginTransaction().replace(R.id.main_fragment, ApprovedTokenFragment())
                this.supportFragmentManager.beginTransaction().addToBackStack(null)
                val fragments = this.supportFragmentManager.fragments
                Log.d("PW", fragments.toString())
                val fragment = this.supportFragmentManager.fragments.find { it is ApprovedTokenFragment } as ApprovedTokenFragment
                val info = fragment.getTokenInfo()
                val tid = info.first
                val receiver = info.second
                val pw = activityResult.data?.getStringExtra("pw") ?: ""

                fragment.disableUI()

                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val dialogView = inflater.inflate(R.layout.layout_transfer_result,null)
                val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                thread {
                    val manuAddress = getManufacturerAddress(tid)
                    var manuStatus = false
                    if (manuAddress == null) {
                        Log.e("MANUREQ", "Manufacturer address call failed with result null.")
                    }
                    else {
                        if (manuAddress.result != "error") {
                            manuStatus = true
                        }
                        else {
                            Log.e("MANUREQ", "${manuAddress.detail}")
                        }

                        if (manuStatus) {
                            val resellerTransfer = resellerTransferToken(tid = tid, manufacturer = manuAddress.detail!!, receiver = receiver, password = pw)

                            if (resellerTransfer == null) {
                                Log.e("RESELLTRANSFER", "Reseller transfer call failed with result null")
                            }
                            else {
                                when {
                                    resellerTransfer.result == "success" -> {
                                        // Success
                                        Log.d("RESELLTRANSFER", "Reseller transfer successful")
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

                                    resellerTransfer.err == "Node Network Error" -> {
                                        Log.e("RESELLTRANSFER", "Node Network Error")
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

                                    resellerTransfer.err == "Authentication Error" -> {
                                        Log.e("RESELLTRANSFER", "Authentication Error")
                                        runOnUiThread {
                                            fragment.enableUI()

                                            resultText.text = "Authentication Error"
                                            val alertDialog = AlertDialog.Builder(this)
                                                .setTitle("Error")
                                                .setPositiveButton("OK", null)
                                                .create()

                                            alertDialog.setView(dialogView)
                                            alertDialog.show()
                                        }
                                    }

                                    resellerTransfer.err == "Invalid Address" -> {
                                        Log.e("RESELLTRANSFER", "Invalid address")
                                        runOnUiThread {
                                            fragment.enableUI()

                                            resultText.text = "Invalid address. Please check again."
                                            val alertDialog = AlertDialog.Builder(this)
                                                .setTitle("Error")
                                                .setPositiveButton("OK", null)
                                                .create()

                                            alertDialog.setView(dialogView)
                                            alertDialog.show()
                                        }
                                    }

                                    resellerTransfer.err == "Network Error" -> {
                                        Log.e("RESELLTRANSFER", "Network error")
                                        runOnUiThread {
                                            fragment.enableUI()

                                            resultText.text = "Network Error. Try again."
                                            val alertDialog = AlertDialog.Builder(this)
                                                .setTitle("Error")
                                                .setPositiveButton("OK", null)
                                                .create()

                                            alertDialog.setView(dialogView)
                                            alertDialog.show()
                                        }
                                    }

                                    resellerTransfer.err == "Invalid Request" -> {
                                        Log.e("RESELLTRANSFER", "Invalid request")
                                        runOnUiThread {
                                            fragment.enableUI()

                                            resultText.text = "Invalid Request. Please check your parameters"
                                            val alertDialog = AlertDialog.Builder(this)
                                                .setTitle("Error")
                                                .setPositiveButton("OK", null)
                                                .create()

                                            alertDialog.setView(dialogView)
                                            alertDialog.show()
                                        }
                                    }

                                    resellerTransfer.err == "Unknown Error" -> {
                                        Log.e("RESELLTRANSFER", "Unknown error in request")
                                        runOnUiThread {
                                            fragment.enableUI()

                                            resultText.text = "Unknown error. Please try again."
                                            val alertDialog = AlertDialog.Builder(this)
                                                .setTitle("Error")
                                                .setPositiveButton("OK", null)
                                                .create()

                                            alertDialog.setView(dialogView)
                                            alertDialog.show()
                                        }
                                    }

                                    else -> {
                                        Log.e("RESELLTRANSFER", "Unknown error!!")
                                        runOnUiThread {
                                            fragment.enableUI()

                                            resultText.text = "Error! Please check the logs"
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
                            runOnUiThread {
                                fragment.enableUI()

                                resultText.text = "Cannot get token info. Please try again."
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
                this.supportFragmentManager.beginTransaction().replace(R.id.main_fragment, ApprovedTokenFragment())
                this.supportFragmentManager.beginTransaction().addToBackStack(null)
                val fragments = this.supportFragmentManager.fragments
                Log.d("PW", fragments.toString())
                val fragment = this.supportFragmentManager.fragments.find { it is ApprovedTokenFragment } as ApprovedTokenFragment
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
                        Log.e("PING", "Ping Failed")
                        runOnUiThread {
                            val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                            val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                            resultText.text = "Server connection unstable. Please check your network status."
                            val alertDialog = AlertDialog.Builder(this)
                                .setTitle("Error")
                                .setPositiveButton("OK") { _, _ -> }
                                .create()

                            alertDialog.setView(dialogView)
                            alertDialog.show()
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
                            runOnUiThread {
                                val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                                val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                                resultText.text = "Network connection unstable. Please check your network status."
                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Error")
                                    .setPositiveButton("OK") { _, _ ->
                                        finish()
                                    }
                                    .create()

                                alertDialog.setView(dialogView)
                                alertDialog.show()
                            }
                        }
                    }

                    if (pingStatus) {
                        val infoRes = getInfo(prefs.getString("jwt", null))

                        if (infoRes == null) {
                            Log.d("GETINFO", "User information fetch failed")
                            runOnUiThread {
                                val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                                val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                                resultText.text = "Server connection unstable. Please check your network status."
                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("Error")
                                    .setPositiveButton("OK") { _, _ ->
                                        finish()
                                    }
                                    .create()

                                alertDialog.setView(dialogView)
                                alertDialog.show()
                            }
                            Thread.currentThread().interrupt()
                        }
                        if (infoRes?.err != null) {
                            runOnUiThread {
                                hideProgress()
                                fragmentManager.beginTransaction()
                                    .replace(R.id.main_fragment, InvalidFragment())
                                    .commitAllowingStateLoss()
                            }
                        } else {
                            when (infoRes?.user_type) {
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

    private fun approveToken(tid: Int, receiver: String, password: String): ApproveTokenResult? {
        val server = RetrofitClass.getInstance()
        val approveBody = ApproveTokenBody(receiver = receiver, tid = tid, pw = password)

        return try {
            val response = server.approveToken(prefs.getString("jwt", null), approveBody).execute()

            when (response.code()) {
                200 -> {
                    response.body()
                }
                503 -> {
                    ApproveTokenResult(result = "failed", err = "Node Network Error")
                }
                401 -> {
                    ApproveTokenResult(result = "failed", err = "Authentication Error")
                }
                406 -> {
                    ApproveTokenResult(result = "failed", err = "Invalid Address")
                }
                else -> {
                    ApproveTokenResult(result = "failed", err = "Unknown Error")
                }
            }
        } catch (e: IOException) {
            ApproveTokenResult(result = "failed", err = "Network Error")
        } catch (e: java.lang.NullPointerException) {
            ApproveTokenResult(result = "failed", err = "Invalid Request")
        }
    }

    private fun getManufacturerAddress(tid: Int) : GetManufacturerAddressResult? {
        val server = RetrofitClass.getInstance()
        val data = GetManufacturerAddressBody(tid)

        return try {
            val response = server.getManuAddr(data).execute()

            when (response.code()) {
                200 -> {
                    response.body()
                }
                503 -> {
                    GetManufacturerAddressResult("error", "Server error")
                }
                404 -> {
                    GetManufacturerAddressResult("error", "Account error")
                }
                else -> {
                    GetManufacturerAddressResult("error", "Unknown")
                }
            }
        } catch (e: IOException) {
            GetManufacturerAddressResult("error", "Network Error")
        } catch (e: java.lang.NullPointerException) {
            GetManufacturerAddressResult("error", "Invalid Request")
        }
    }

    private fun resellerTransferToken(tid: Int, manufacturer: String, receiver: String, password: String) : TransferTokenResult? {
        val server = RetrofitClass.getInstance()
        val transactor = prefs.getString("account", "")

        val txInfo = TransferTokenBody(sender = manufacturer, transactor = transactor, receiver = receiver, tid = tid, pw = password)

        return try {
            val response = server.transferToken(prefs.getString("jwt", null), txInfo).execute()
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

    fun getApprQRCodeLauncher() : ActivityResultLauncher<ScanOptions> {
        return approvedBarcodeLauncher
    }

    fun getPWInputLauncher(): ActivityResultLauncher<Intent> {
        return passwordActivityLauncher
    }

    fun getApprPWInputLauncher(): ActivityResultLauncher<Intent> {
        return approvedPasswordActivityLauncher
    }
}