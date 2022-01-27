package dev.chungjungsoo.guaranteewallet.activities

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.*
import dev.chungjungsoo.guaranteewallet.service.RetrofitService


object RetrofitClass {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://gtk-main-server.herokuapp.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val _api = retrofit.create(RetrofitService::class.java)

    fun getInstance(): RetrofitService {
        return _api
    }
}

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val server = RetrofitClass.getInstance()

        server.ping(null).enqueue(object : Callback<PingResult> {
            override fun onResponse(call: Call<PingResult>, response: Response<PingResult>) {
                if (response.isSuccessful) {
                    Log.d("PING", "Ping successful")
                    Toast.makeText(applicationContext, "status: ${response.body()?.status}, token: ${response.body()?.token_status}", Toast.LENGTH_LONG).show()
                }
                else {
                    Log.e("PING", "Ping Client Error")
                }
            }

            override fun onFailure(call: Call<PingResult>, t: Throwable) {
                Log.e("PING", "Ping Server Error")
            }

        })

        val inputUserID : EditText = findViewById<EditText>(R.id.user_id)
        val inputUserPW : EditText = findViewById<EditText>(R.id.user_pw)

        val userID : String = inputUserID.text.toString()
        val userPW : String = inputUserPW.text.toString()

        
    }
}