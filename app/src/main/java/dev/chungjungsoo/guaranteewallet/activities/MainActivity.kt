package dev.chungjungsoo.guaranteewallet.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil


class MainActivity : AppCompatActivity() {
    companion object { lateinit var prefs: PreferenceUtil }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val token :String = prefs.getString("jwt", "")

        if (token != "") {
            // Move to main screen
            Log.d("MAIN", "Token exists")
            setContentView(R.layout.activity_main)
        }
        else {
            // Move to login screen
            Log.d("MAIN", "Token doesn't exist")
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
            this.finish()
        }
    }
}