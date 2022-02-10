package dev.chungjungsoo.guaranteewallet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import dev.chungjungsoo.guaranteewallet.R


data class ListViewItem(val title : String, val content : String, val expDate: String)

class TokenListViewAdapter(private val items: MutableList<ListViewItem>) : BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): ListViewItem {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var listView = convertView

        if (listView == null) {
            listView =  LayoutInflater.from(parent?.context).inflate(R.layout.listview_item_list_token, parent, false)
        }

        val item : ListViewItem = items[position]

        listView!!.findViewById<TextView>(R.id.token_title).text = item.title
        listView.findViewById<TextView>(R.id.token_content).text = item.content
        listView.findViewById<TextView>(R.id.token_exp_date).text = item.expDate

        return listView
    }
}