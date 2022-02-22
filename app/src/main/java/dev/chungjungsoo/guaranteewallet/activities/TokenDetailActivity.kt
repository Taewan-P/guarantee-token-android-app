package dev.chungjungsoo.guaranteewallet.activities

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.CreateQRCodeBody
import dev.chungjungsoo.guaranteewallet.dataclass.CreateQRCodeResult
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

class TokenDetailActivity : AppCompatActivity()  {
    companion object { lateinit var prefs: PreferenceUtil }
    lateinit var progressBar : ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_token_info)
        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)
        WindowCompat.getInsetsController(window, window.decorView)!!.isAppearanceLightStatusBars = false

        progressBar = findViewById(R.id.qr_progress_bar)
        prefs = PreferenceUtil(applicationContext)

        val tokenID = intent.getStringExtra("tid")!!.toInt()
        val tokenLogo = intent.getStringExtra("logo")
        val tokenBrand = intent.getStringExtra("brand")
        val tokenProdName = intent.getStringExtra("name")
        val tokenProdDate = intent.getStringExtra("prodDate")
        val tokenExpDate = intent.getStringExtra("expDate")
        val tokenDetails = intent.getStringExtra("details")

        val txtViewID = findViewById<TextView>(R.id.token_detail_id)
        val txtViewName = findViewById<TextView>(R.id.token_detail_name)
        val txtViewBrand = findViewById<TextView>(R.id.token_detail_brand)
        val txtViewProdDate = findViewById<TextView>(R.id.token_detail_prod_date)
        val txtViewExpDate = findViewById<TextView>(R.id.token_detail_exp_date)
        val txtViewDetails = findViewById<TextView>(R.id.token_detail_details)
        val qrCodeImgView = findViewById<ImageView>(R.id.qr_image_view)

        txtViewID.text = "No. $tokenID"
        txtViewName.text = tokenProdName
        txtViewBrand.text = tokenBrand
        txtViewProdDate.text = tokenProdDate
        txtViewExpDate.text = tokenExpDate
        txtViewDetails.text = tokenDetails

        showProgress(this)

        thread {
            val qrCall = getQRCode(prefs.getString("jwt", ""), tokenID, prefs.getString("account", ""))

            if (qrCall == null) {
                Log.d("QRCODE", "QR Code fetch failed")
                runOnUiThread {
                    hideProgress()
                }
            }

            if (qrCall?.error == null) {
                // Successful request
                if (qrCall?.result != null) {
                    // QR received
                    Log.d("QRCODE", "QR Code fetch successful")

                    val decoded = Base64.getDecoder().decode(qrCall.result.split(",")[1])
                    val decodedByte : Bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)

                    runOnUiThread {
                        hideProgress()
                        qrCodeImgView.setImageBitmap(decodedByte)
                    }
                }
            }
            else {
                // Invalid Request
                runOnUiThread {
                    hideProgress()
                }
            }

        }
    }

    private fun getQRCode(token : String, tid : Int, owner : String) : CreateQRCodeResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.createQRCode(token, CreateQRCodeBody(tid=tid, owner=owner)).execute()
            Log.d("REQ", response.body().toString())
            println(response.body().toString())
            response.body()
        } catch (e: IOException) {
            CreateQRCodeResult(result=null, error="Network Error")
        } catch (e: NullPointerException) {
            CreateQRCodeResult(result=null, error="Invalid Request")
        }
    }

    private fun showProgress(activity: Activity) {
        if (activity.isFinishing) { return }

        if (progressBar.visibility != View.VISIBLE) {
            progressBar.visibility = View.VISIBLE
        }

    }


    private fun hideProgress() {
        if (progressBar.visibility == View.VISIBLE) {
            progressBar.visibility = View.GONE
        }
    }

    private fun logLargeString(string: String) {
        if (string.length > 2048) {
            Log.d("LLOG", string.substring(0,2048))
            logLargeString(string.substring(2048))
        }
        else {
            Log.d("LLOG", string)
        }
    }
}