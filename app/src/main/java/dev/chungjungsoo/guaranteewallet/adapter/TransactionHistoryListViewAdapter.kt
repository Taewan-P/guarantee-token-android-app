package dev.chungjungsoo.guaranteewallet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import dev.chungjungsoo.guaranteewallet.R

class TransactionHistoryListViewAdapter(private val items: MutableList<String?>) : BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): String? {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var listView = convertView

        if (listView == null) {
            listView = if (count == 1) {
                LayoutInflater.from(parent!!.context).inflate(R.layout.listview_item_transaction_history_initial_one, parent, false)
            } else if (position == 0) {
                LayoutInflater.from(parent!!.context).inflate(R.layout.listview_item_transaction_history_initial_multiple, parent, false)
            } else if (position == count - 1) {
                LayoutInflater.from(parent!!.context).inflate(R.layout.listview_item_transaction_history_last, parent, false)
            } else {
                LayoutInflater.from(parent!!.context).inflate(R.layout.listview_item_transaction_history_middle, parent, false)
            }
        }

        val item = items[position]
        listView!!.findViewById<TextView>(R.id.tx_history_item_text).text = item

        return listView
    }
}