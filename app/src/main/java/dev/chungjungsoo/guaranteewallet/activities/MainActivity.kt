package dev.chungjungsoo.guaranteewallet.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.PingResult
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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

            val server = RetrofitClass.getInstance()
            server.ping(prefs.getString("jwt", null)).enqueue(object :
                Callback<PingResult> {
                override fun onResponse(call: Call<PingResult>, response: Response<PingResult>) {
                    if (response.isSuccessful) {
                        Log.d("PING", "Ping successful")

                        if (response.body()?.token_status != "valid") {
                            // Invalid token
                            Log.d("PING", "Login token invalid")
                            Toast.makeText(applicationContext, "Login expired. Please re-login.", Toast.LENGTH_SHORT).show()
                            prefs.resetToken()
                            val loginIntent = Intent(applicationContext, LoginActivity::class.java)
                            startActivity(loginIntent)
                            this@MainActivity.finish()
                        }
                    }
                    else {
                        Log.e("PING", "Ping Client Error")
                        Toast.makeText(applicationContext, "Server connection unstable. Please check your network status", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PingResult>, t: Throwable) {
                    Log.e("PING", "Ping Server Error")
                    Toast.makeText(applicationContext, "Network Error. Please check your network status.", Toast.LENGTH_SHORT).show()
                }

            })


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