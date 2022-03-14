package dev.chungjungsoo.guaranteewallet.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import dev.chungjungsoo.guaranteewallet.R

class PasswordInputActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        this.window.statusBarColor = ContextCompat.getColor(this, R.color.mainColor)
        WindowInsetsControllerCompat(this.window, this.window.decorView).isAppearanceLightStatusBars = false

        val buttons = listOf<Button>(findViewById(R.id.pw_btn1), findViewById(R.id.pw_btn2), findViewById(R.id.pw_btn3),
                                     findViewById(R.id.pw_btn4), findViewById(R.id.pw_btn5), findViewById(R.id.pw_btn6),
                                     findViewById(R.id.pw_btn7), findViewById(R.id.pw_btn8), findViewById(R.id.pw_btn9),
                                     findViewById(R.id.pw_btn10))

        val inputSlots = listOf<CheckBox>(findViewById(R.id.pw_input_slot1), findViewById(R.id.pw_input_slot2),
                                          findViewById(R.id.pw_input_slot3), findViewById(R.id.pw_input_slot4),
                                          findViewById(R.id.pw_input_slot5), findViewById(R.id.pw_input_slot6),
                                          findViewById(R.id.pw_input_slot7), findViewById(R.id.pw_input_slot8))

        fun updateSlots(len: Int) {
            for (i in 0 until len) {
                inputSlots[i].isChecked = true
            }

            for (i in len until inputSlots.size) {
                inputSlots[i].isChecked = false
            }
        }

        val numbers = listOf(1,2,3,4,5,6,7,8,9,0).shuffled()

        val pwInput = mutableListOf<Int>()

        for (i in buttons.indices) {
            buttons[i].text = numbers[i].toString()
            buttons[i].setOnClickListener {
                Log.d("KEYPAD", "${numbers[i]} is pressed")
                if (pwInput.size == 7) {
                    pwInput.add(numbers[i])
                    updateSlots(pwInput.size)
                    val _pw = pwInput.joinToString("")
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("pw", _pw)
                    }
                    setResult(RESULT_OK, intent)
                    if (!isFinishing) { finish() }
                }

                if (pwInput.size < 8) {
                    pwInput.add(numbers[i])
                    updateSlots(pwInput.size)
                }
            }
        }

        val delBtn = findViewById<ImageButton>(R.id.pw_btn_del)

        delBtn.setOnClickListener {
            pwInput.removeLastOrNull()
            updateSlots(pwInput.size)
        }
    }
}