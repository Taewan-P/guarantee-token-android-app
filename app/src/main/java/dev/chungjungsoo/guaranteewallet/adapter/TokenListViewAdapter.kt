package dev.chungjungsoo.guaranteewallet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.ListViewItem


class TokenListViewAdapter(private val items: MutableList<ListViewItem>) : BaseAdapter() {
    private val colorList = listOf(R.drawable.shape_listiview_tokens, R.drawable.shape_listiview_tokens_color2, R.drawable.shape_listiview_tokens_color3, R.drawable.shape_listiview_tokens_color4, R.drawable.shape_listiview_tokens_color5, R.drawable.shape_listiview_tokens_color6).shuffled()

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

        listView!!.findViewById<TextView>(R.id.token_id).text = "No. ${item.tokenID}"
        listView.findViewById<TextView>(R.id.product_name).text = item.productName
        listView.findViewById<TextView>(R.id.brand_name).text = item.brand
        listView.findViewById<TextView>(R.id.token_exp_date).text = item.expirationDate
        listView.findViewById<ImageView>(R.id.product_logo).setImageResource(R.drawable.ic_apple_logo_black)
        listView.setBackgroundResource(colorList[position % colorList.size])

        return listView
    }
}