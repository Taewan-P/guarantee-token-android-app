package dev.chungjungsoo.guaranteewallet.activities

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.adapter.TransactionHistoryListViewAdapter

class TransactionDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)


        val historyList = intent.getStringArrayListExtra("history")
        Log.d("HISTORYCHECK", historyList.toString())

        val folded = foldHistories(historyList as ArrayList<ArrayList<String?>>)

        val adapter = TransactionHistoryListViewAdapter(folded)
        val transactionHistoryListView = findViewById<ListView>(R.id.tx_history_listview)
        val transactionHistoryHeaderView = layoutInflater.inflate(R.layout.title_transaction_history_layout, transactionHistoryListView, false)

        transactionHistoryListView.addHeaderView(transactionHistoryHeaderView, null, false)
        transactionHistoryListView.adapter = adapter
    }

    private fun foldHistories(history : ArrayList<ArrayList<String?>>) : ArrayList<String?> {
        val result : ArrayList<String?> = ArrayList()

        for (i in history.indices) {
            result.add(history[i][1])
        }

        return result
    }
}