package dev.chungjungsoo.guaranteewallet.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.HistoryItem
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil

class HistoryListViewAdapter(private val items: MutableList<HistoryItem>) : BaseAdapter() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        prefs = PreferenceUtil(parent!!.context)
        var listView = convertView

        if (listView == null) {
            listView = LayoutInflater.from(parent.context).inflate(R.layout.listview_item_history, parent, false)
        }

        val item: HistoryItem = items[position]
        val transactionType = getTransactionType(item)

        listView!!.findViewById<TextView>(R.id.history_tid).text = "No. ${item.tid}"
        listView.findViewById<TextView>(R.id.history_type).text = transactionType.first
        listView.findViewById<TextView>(R.id.history_type).setTextColor(transactionType.second)
        listView.findViewById<TextView>(R.id.history_date).text = item.time

        return listView
    }

    private fun getTransactionType(h: HistoryItem): Pair<String, Int> {
        val address = prefs.getString("account", null)

        return when {
            h.from == "null" || h.from == null -> {
                // Token Minting

                Pair("MINT", Color.parseColor("#88AC4E"))
            }
            h.from == address -> {
                // Me sending the token
                Pair("SENT", Color.parseColor("#3A5FB7"))
            }
            h.to == address -> {
                // Me receiving the token
                Pair("RECEIVED", Color.parseColor("#AC5841"))
            }
            else -> {
                // Error?
                Pair("UNKNOWN", Color.parseColor("#FFFFFF"))
            }
        }
    }
}