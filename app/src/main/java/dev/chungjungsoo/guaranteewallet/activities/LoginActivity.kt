package dev.chungjungsoo.guaranteewallet.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.*
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
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
    companion object { lateinit var prefs: PreferenceUtil }
    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceUtil(applicationContext)
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

        val inputUserID : EditText = findViewById(R.id.user_id)
        val inputUserPW : EditText = findViewById(R.id.user_pw)
        val loginBtn : ImageButton = findViewById(R.id.login_btn)

        loginBtn.isEnabled = false

        var userID : String = ""
        var userPW : String = ""

        var idStatus = false
        var pwStatus = false

        inputUserID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                userID = s.toString()
                if (!validateLoginText(userID)) {
                    inputUserID.error = "ID should be longer than 5 character"
                    idStatus = false
                }
                else { idStatus = true }

                loginBtn.isEnabled = idStatus && pwStatus
            }
        })

        inputUserPW.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                userPW = s.toString()
                if (!validateLoginText(userPW)) {
                    inputUserPW.error = "PW should be longer than 5 character"
                    pwStatus = false
                }
                else { pwStatus = true }

                loginBtn.isEnabled = idStatus && pwStatus
            }
        })

        loginBtn.setOnClickListener {
            val reqBody = LoginBody(userID, userPW)

            server.login(reqBody).enqueue(object : Callback<LoginResult> {
                override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                    if (response.isSuccessful) {
                        Log.d("LOGIN", "Login Successful")
                        prefs.setString("jwt", response.body()!!.jwt)
                        val mainIntent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(mainIntent)
                        this@LoginActivity.finish()
                    }
                    else {
                        Log.d("LOGIN", "Login Failed")
                        Log.d("LOGIN", response.body().toString())
                        Toast.makeText(applicationContext, response.body()!!.err, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                    Log.d("LOGIN", "Login Error")
                    Toast.makeText(applicationContext, "Login Failed. Check your network condition.", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    private fun validateLoginText(str: String): Boolean {
        val trimmed = str.trim()
        if (TextUtils.isEmpty(trimmed)) { return false }

        return trimmed.length >= 6
    }
}