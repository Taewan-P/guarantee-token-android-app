package dev.chungjungsoo.guaranteewallet.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.CreateQRCodeBody
import dev.chungjungsoo.guaranteewallet.dataclass.ValidateTokenResult
import java.io.IOException
import java.lang.NullPointerException
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

class VerificationResultActivity : AppCompatActivity() {
    lateinit var progressBar: ProgressBar
    lateinit var tokenInfoLayout: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_result)
        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)
        WindowCompat.getInsetsController(window, window.decorView)!!.isAppearanceLightStatusBars =
            false

        val tid = intent.getIntExtra("tid", -1)
        val owner = intent.getStringExtra("owner")
        val exp = intent.getBooleanExtra("exp", true)
        val exp2 = intent.getBooleanExtra("exp2", true)

        progressBar = findViewById(R.id.verification_result_progress_bar)
        tokenInfoLayout = findViewById(R.id.result_text_layout)
        val resultIcon = findViewById<ImageView>(R.id.result_icon)
        val resultText = findViewById<TextView>(R.id.result_text)


        val textViewID = findViewById<TextView>(R.id.verification_result_tid)
        val textViewName = findViewById<TextView>(R.id.verification_result_name)
        val textViewBrand = findViewById<TextView>(R.id.verification_result_brand)
        val textViewProdDate = findViewById<TextView>(R.id.verification_result_prod_date)
        val textViewExpDate = findViewById<TextView>(R.id.verification_result_exp_date)
        val textViewDetails = findViewById<TextView>(R.id.verification_result_details)

        showProgress(this)
        
        if (exp2) {
            hideProgress()
            resultIcon.setImageResource(R.drawable.ic_invalid)
            resultText.text = "Expired"
            textViewID.text = "N/A"
            textViewName.text = "N/A"
            textViewBrand.text = "N/A"
            textViewProdDate.text = "N/A"
            textViewExpDate.text = "N/A"
            textViewDetails.text = "N/A"
        }
        else if (tid == -1 || exp) {
            // Invalid intent result, or expired token
            hideProgress()
            resultIcon.setImageResource(R.drawable.ic_invalid)
            resultText.text = "Invalid"
            textViewID.text = "N/A"
            textViewName.text = "N/A"
            textViewBrand.text = "N/A"
            textViewProdDate.text = "N/A"
            textViewExpDate.text = "N/A"
            textViewDetails.text = "N/A"
        }
        else {
            thread {
                val validationCall = validateToken(tid = tid, owner = owner ?: "")

                if (validationCall == null) {
                    Log.d("VALIDATE", "Token validation failed")
                    runOnUiThread {
                        hideProgress()
                        resultIcon.setImageResource(R.drawable.ic_invalid)
                        resultText.text = "Invalid"
                        textViewID.text = "N/A"
                        textViewName.text = "N/A"
                        textViewBrand.text = "N/A"
                        textViewProdDate.text = "N/A"
                        textViewExpDate.text = "N/A"
                        textViewDetails.text = "N/A"
                    }
                }

                if (validationCall?.error == null) {
                    // Successful request
                    if (validationCall?.result == "valid") {
                        // Valid Token
                        val tokenInfo = validationCall.info

                        if (tokenInfo == null) {
                            // This should not happen
                            Log.d("VALIDATE", "Token info not found")

                            runOnUiThread {
                                hideProgress()
                                resultIcon.setImageResource(R.drawable.ic_invalid)
                                resultText.text = "Invalid"
                                textViewID.text = "N/A"
                                textViewName.text = "N/A"
                                textViewBrand.text = "N/A"
                                textViewProdDate.text = "N/A"
                                textViewExpDate.text = "N/A"
                                textViewDetails.text = "N/A"
                            }
                        }
                        else {
                            // Regular route. Successfully retrieved information
                            runOnUiThread {
                                hideProgress()
                                resultIcon.setImageResource(R.drawable.ic_certified)
                                resultText.text = "Valid"
                                textViewID.text = "No. ${tokenInfo!!.tid}"
                                textViewName.text = tokenInfo.name
                                textViewBrand.text = tokenInfo.brand
                                textViewProdDate.text = tokenInfo.prodDate
                                textViewExpDate.text = tokenInfo.expDate
                                textViewDetails.text = tokenInfo.details
                            }
                        }
                    }
                    else {
                        // Invalid Token
                        runOnUiThread {
                            hideProgress()
                            resultIcon.setImageResource(R.drawable.ic_invalid)
                            resultText.text = "Invalid"
                            textViewID.text = "N/A"
                            textViewName.text = "N/A"
                            textViewBrand.text = "N/A"
                            textViewProdDate.text = "N/A"
                            textViewExpDate.text = "N/A"
                            textViewDetails.text = "N/A"
                        }
                    }
                }
                else {
                    // Request Error
                    Log.e("VALIDATE", "${validationCall.error}")

                    runOnUiThread {
                        hideProgress()
                        resultIcon.setImageResource(R.drawable.ic_invalid)
                        resultText.text = "Invalid"
                        textViewID.text = "N/A"
                        textViewName.text = "N/A"
                        textViewBrand.text = "N/A"
                        textViewProdDate.text = "N/A"
                        textViewExpDate.text = "N/A"
                        textViewDetails.text = "N/A"

                        when (validationCall.error) {
                            "Network Error" -> {
                                Toast.makeText(applicationContext, "Network Error", Toast.LENGTH_SHORT).show()
                            }

                            "Invalid Request" -> {
                                Toast.makeText(applicationContext, "Invalid Request. Try again.", Toast.LENGTH_SHORT).show()
                            }

                            else -> {
                                Toast.makeText(applicationContext, "Unknown Error!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }


    private fun validateToken(tid: Int, owner: String): ValidateTokenResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.validateToken(CreateQRCodeBody(tid = tid, owner = owner)).execute()
            response.body()
        } catch (e: IOException) {
            ValidateTokenResult(result = "invalid", error = "Network Error", txHistory = null, info = null, detail = null)
        } catch (e: NullPointerException) {
            ValidateTokenResult(result = "invalid", error = "Invalid Request", txHistory = null, info = null, detail = null)
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
            tokenInfoLayout.visibility = View.VISIBLE
        }
    }
}