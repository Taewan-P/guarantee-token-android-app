package dev.chungjungsoo.guaranteewallet.activities

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.transition.*
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
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

class TokenDetailActivity : AppCompatActivity() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun onStart() {
        super.onStart()

        val tokenID = intent.getStringExtra("tid")!!.toInt()
        val tokenBrand = intent.getStringExtra("brand")
        val tokenProdName = intent.getStringExtra("name")
        val tokenProdDate = intent.getStringExtra("prodDate")
        val tokenExpDate = intent.getStringExtra("expDate")
        val tokenDetails = intent.getStringExtra("details")

        val tokenIDText = findViewById<TextView>(R.id.token_id)
        val productNameText = findViewById<TextView>(R.id.product_name)
        val productLogo = findViewById<ImageView>(R.id.product_logo)
        val brandNameText = findViewById<TextView>(R.id.brand_name)
        val tokenExpDateText = findViewById<TextView>(R.id.token_exp_date)

        tokenIDText.text = "No. $tokenID"
        productNameText.text = tokenProdName
        productLogo.setImageResource(R.drawable.ic_apple_logo_black)
        brandNameText.text = tokenBrand
        tokenExpDateText.text = tokenExpDate

        val sendBtn = findViewById<RelativeLayout>(R.id.send_token_btn)
        sendBtn.animate().alpha(0.0f)

        val color = intent.getStringExtra("color")!!.toInt()
        val tokenView = findViewById<RelativeLayout>(R.id.token_view_details)
        tokenView.setBackgroundResource(color)
    }

    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {

        window.sharedElementEnterTransition =
            TransitionSet().apply {
                interpolator = OvershootInterpolator(0.5F)
                ordering = TransitionSet.ORDERING_TOGETHER
                addTransition(ChangeBounds().apply {
                    pathMotion = ArcMotion()
                })

                addTransition(ChangeTransform())
                addTransition(ChangeClipBounds())
                addTransition(ChangeImageTransform())
            }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token_details)
        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)
        WindowCompat.getInsetsController(window, window.decorView)!!.isAppearanceLightStatusBars =
            false

        progressBar = findViewById(R.id.qr_progress_bar)
        prefs = PreferenceUtil(applicationContext)

        val tokenID = intent.getStringExtra("tid")!!.toInt()
        val tokenProdDate = intent.getStringExtra("prodDate")
        val tokenDetails = intent.getStringExtra("details")

        val txtViewProdDate = findViewById<TextView>(R.id.token_detail_prod_date)
        val txtViewDetails = findViewById<TextView>(R.id.token_detail_details)
        val qrCodeImgView = findViewById<ImageView>(R.id.qr_image_view)

        txtViewProdDate.text = tokenProdDate
        txtViewDetails.text = tokenDetails

        showProgress(this)

        thread {
            val qrCall =
                getQRCode(prefs.getString("jwt", ""), tokenID, prefs.getString("account", ""))

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
                    val decodedByte: Bitmap =
                        BitmapFactory.decodeByteArray(decoded, 0, decoded.size)

                    runOnUiThread {
                        hideProgress()
                        qrCodeImgView.setImageBitmap(decodedByte)
                    }
                }
            } else {
                // Invalid Request
                runOnUiThread {
                    hideProgress()
                }
            }

        }
    }


    override fun onBackPressed() {
        val sendBtn = findViewById<RelativeLayout>(R.id.send_token_btn)
        sendBtn.animate().alpha(1.0f)
        super.onBackPressed()
    }


    private fun getQRCode(token: String, tid: Int, owner: String): CreateQRCodeResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response =
                server.createQRCode(token, CreateQRCodeBody(tid = tid, owner = owner)).execute()
            response.body()
        } catch (e: IOException) {
            CreateQRCodeResult(result = null, error = "Network Error")
        } catch (e: NullPointerException) {
            CreateQRCodeResult(result = null, error = "Invalid Request")
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

    private fun logLargeString(string: String) {
        if (string.length > 2048) {
            Log.d("LLOG", string.substring(0, 2048))
            logLargeString(string.substring(2048))
        } else {
            Log.d("LLOG", string)
        }
    }
}