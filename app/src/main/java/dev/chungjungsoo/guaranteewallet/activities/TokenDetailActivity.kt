package dev.chungjungsoo.guaranteewallet.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.chungjungsoo.guaranteewallet.R

class TokenDetailActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_token_info)
        window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)
        WindowCompat.getInsetsController(window, window.decorView)!!.isAppearanceLightStatusBars = false

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

        txtViewID.text = "No. $tokenID"
        txtViewName.text = tokenProdName
        txtViewBrand.text = tokenBrand
        txtViewProdDate.text = tokenProdDate
        txtViewExpDate.text = tokenExpDate
        txtViewDetails.text = tokenDetails

    }
}