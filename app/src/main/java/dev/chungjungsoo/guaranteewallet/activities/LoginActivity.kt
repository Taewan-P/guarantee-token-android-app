package dev.chungjungsoo.guaranteewallet.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.LoginBody
import dev.chungjungsoo.guaranteewallet.dataclass.LoginResult
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import dev.chungjungsoo.guaranteewallet.service.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClass {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://capstone-337506.du.r.appspot.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val _api = retrofit.create(RetrofitService::class.java)

    fun getInstance(): RetrofitService {
        return _api
    }
}

class LoginActivity : AppCompatActivity() {
    lateinit var progressDialog: AppCompatDialog

    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val server = RetrofitClass.getInstance()

        val inputUserID: EditText = findViewById(R.id.user_id)
        val inputUserPW: EditText = findViewById(R.id.user_pw)
        val loginBtn: Button = findViewById(R.id.login_btn)

        disableLoginBtn()

        var userID: String = ""
        var userPW: String = ""

        var idStatus = false
        var pwStatus = false

        inputUserID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                userID = s.toString()
                if (!validateLoginText(userID)) {
                    inputUserID.error = "ID should be longer than 5 character"
                    idStatus = false
                } else {
                    idStatus = true
                }
                if (idStatus && pwStatus) {
                    enableLoginBtn()
                }
                else {
                    disableLoginBtn()
                }
            }
        })

        inputUserPW.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                userPW = s.toString()
                if (!validateLoginText(userPW)) {
                    inputUserPW.error = "PW should be longer than 5 character"
                    pwStatus = false
                } else {
                    pwStatus = true
                }

                if (idStatus && pwStatus) {
                    enableLoginBtn()
                }
                else {
                    disableLoginBtn()
                }
            }
        })

        loginBtn.setOnClickListener {
            val reqBody = LoginBody(userID, userPW)
            progressDialog = AppCompatDialog(this)
            showProgress(this)

            server.login(reqBody).enqueue(object : Callback<LoginResult> {
                override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {
                    if (response.isSuccessful) {
                        Log.d("LOGIN", "Login Successful")
                        prefs.setString("jwt", response.body()!!.jwt)
                        hideProgress()
                        val mainIntent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(mainIntent)
                        this@LoginActivity.finish()
                    } else {
                        Log.d("LOGIN", "Login Failed")
                        hideProgress()
                        Toast.makeText(applicationContext, "ID/PW mismatch", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<LoginResult>, t: Throwable) {
                    Log.d("LOGIN", "Login Error")
                    hideProgress()
                    Toast.makeText(
                        applicationContext,
                        "Login Failed. Check your network condition.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
        }
    }

    private fun validateLoginText(str: String): Boolean {
        val trimmed = str.trim()
        if (TextUtils.isEmpty(trimmed)) {
            return false
        }

        return trimmed.length >= 6
    }

    private fun showProgress(activity: Activity) {
        if (activity.isFinishing) {
            return
        }

        if (!progressDialog.isShowing) {
            progressDialog.setCancelable(false)
            progressDialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            progressDialog.setContentView(R.layout.etc_loading_layout)
            progressDialog.show()
        }

    }

    fun hideProgress() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    fun disableLoginBtn() {
        val loginBtn = findViewById<Button>(R.id.login_btn)
        loginBtn.isEnabled = false
        loginBtn.alpha = 0.5F
        loginBtn.setTextColor(Color.parseColor("#a7a9ac"))
    }

    fun enableLoginBtn() {
        val loginBtn = findViewById<Button>(R.id.login_btn)
        loginBtn.isEnabled = true
        loginBtn.alpha = 1F
        loginBtn.setTextColor(Color.parseColor("#000000"))
    }
}