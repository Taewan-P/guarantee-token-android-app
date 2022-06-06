package dev.chungjungsoo.guaranteewallet.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.GetManufacturerAddressBody
import dev.chungjungsoo.guaranteewallet.dataclass.GetManufacturerAddressResult
import org.w3c.dom.Text
import java.io.IOException
import kotlin.concurrent.thread

class ManufacturerValidationActivity : AppCompatActivity() {
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_manufacturer_validation)
        progressBar = findViewById(R.id.validation_result_progress_bar)

        val tid = intent.getIntExtra("tid", -1)
        val address = intent.getStringExtra("address") ?: ""
        val manufacturerText = findViewById<TextView>(R.id.manufacturer_text)

        showProgress(this)

        thread {
            val manuAddress = getManufacturerAddress(tid = tid)

            if (manuAddress == null) {
                Log.e("MANUREQ", "Manufacturer address call failed with result null.")
            }
            else {
                if (manuAddress.result != "error") {
                    // Request success. Comparing...
                    if (address == manuAddress.detail) {
                        // Token is from valid manufacturer
                        Log.d("MANUVAL", "Token is from valid manufacturer")
                        runOnUiThread {
                            hideProgress()
                            manufacturerText.visibility = View.VISIBLE
                            findViewById<TextView>(R.id.this_is_a).text = "This is a"
                            findViewById<TextView>(R.id.manufacturer_validity).text = "valid"
                            findViewById<TextView>(R.id.manufacturer_validity).setTextColor(ContextCompat.getColor(this, R.color.cardColor3))
                            findViewById<TextView>(R.id.manufacturer_address).text = address
                            findViewById<TextView>(R.id.validation_result).text = "This token is minted from a proper manufacturer."
                        }
                    }
                    else {
                        // Token is not from valid manufacturer
                        Log.e("MANUVAL", "Token minter and address does not match")
                        runOnUiThread {
                            hideProgress()
                            manufacturerText.visibility = View.VISIBLE
                            findViewById<TextView>(R.id.this_is_a).text = "This is an"
                            findViewById<TextView>(R.id.manufacturer_validity).text = "invalid"
                            findViewById<TextView>(R.id.manufacturer_validity).setTextColor(ContextCompat.getColor(this, R.color.red))
                            findViewById<TextView>(R.id.manufacturer_address).text = address
                            findViewById<TextView>(R.id.validation_result).text = "This token is not minted from a proper manufacturer."
                        }
                    }
                }
                else {
                    Log.e("MANUVAL", "err: ${manuAddress.detail}")
                    runOnUiThread {
                        hideProgress()
                        manufacturerText.visibility = View.VISIBLE
                        findViewById<TextView>(R.id.this_is_a).text = "This is an"
                        findViewById<TextView>(R.id.manufacturer_validity).text = "invalid"
                        findViewById<TextView>(R.id.manufacturer_validity).setTextColor(ContextCompat.getColor(this, R.color.red))
                        findViewById<TextView>(R.id.manufacturer_address).text = address
                        findViewById<TextView>(R.id.validation_result).text = "This token is not minted from a proper manufacturer."
                    }
                }
            }
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

    private fun showProgress(activity: Activity) {
        if (activity.isFinishing) {
            return
        }

        if (progressBar.visibility != View.VISIBLE) {
            progressBar.visibility = View.VISIBLE
        }
    }


    private fun hideProgress() {
        if (progressBar.visibility == View.VISIBLE) {
            progressBar.visibility = View.GONE
        }
    }
}