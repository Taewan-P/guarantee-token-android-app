package dev.chungjungsoo.guaranteewallet.activities

import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dev.chungjungsoo.guaranteewallet.R

class TransactionDetailActivity : AppCompatActivity() {
    private lateinit var transactionHistoryLayout : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)
        transactionHistoryLayout = findViewById(R.id.transaction_history_layout)

        val historyList = intent.getStringArrayListExtra("history")

        drawHistories(historyList as ArrayList<ArrayList<String?>> )
    }

    private fun drawHistories(history : ArrayList<ArrayList<String?>>) {

        for (i in history.indices) {
            // History element
            val historyText = TextView(this)
            historyText.setTextColor(this.getColor(R.color.white))
            historyText.setTextSize(Dimension.SP, 17F)
            val textParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            textParams.gravity = Gravity.CENTER
            historyText.layoutParams = textParams

            // Arrow element
            val arrowDown = ImageView(this)
            val arrowParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            arrowParam.gravity = Gravity.CENTER
            arrowParam.setMargins(0,25, 0, 25)
            arrowDown.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_arrow_down))
            arrowDown.layoutParams = arrowParam

            // Add them
            when (i) {
                0 -> {
                    val mintedFrom = TextView(this)
                    mintedFrom.text = "Minted From"
                    mintedFrom.setTextColor(this.getColor(R.color.white))
                    mintedFrom.setTextSize(Dimension.SP, 20F)
                    mintedFrom.layoutParams = textParams

                    transactionHistoryLayout.addView(mintedFrom)
                    historyText.text = history[i][1]
                    transactionHistoryLayout.addView(historyText)
                    if (history.size != 1) {
                        transactionHistoryLayout.addView(arrowDown)
                    }
                }
                history.size - 1 -> {
                    val currentOwner = TextView(this)
                    currentOwner.text = "Current Owner"
                    currentOwner.setTextColor(this.getColor(R.color.white))

                    currentOwner.setTextSize(Dimension.SP, 20F)
                    currentOwner.layoutParams = textParams

                    transactionHistoryLayout.addView(currentOwner)

                    historyText.text = history[i][1]
                    transactionHistoryLayout.addView(historyText)

                }
                else -> {
                    historyText.text = history[i][1]
                    transactionHistoryLayout.addView(historyText)
                    transactionHistoryLayout.addView(arrowDown)
                }
            }
        }
    }
}