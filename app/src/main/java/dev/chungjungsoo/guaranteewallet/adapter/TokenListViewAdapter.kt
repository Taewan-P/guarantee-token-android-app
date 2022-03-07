package dev.chungjungsoo.guaranteewallet.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.ListViewItem
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil


class TokenListViewAdapter(private val items: MutableList<ListViewItem>) : BaseAdapter() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }
    private val colorList = listOf(
        R.drawable.shape_listiview_tokens,
        R.drawable.shape_listiview_tokens_color2,
        R.drawable.shape_listiview_tokens_color3,
        R.drawable.shape_listiview_tokens_color4,
        R.drawable.shape_listiview_tokens_color5,
        R.drawable.shape_listiview_tokens_color6
    ).shuffled()

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
        prefs = PreferenceUtil(parent!!.context)
        var listView = convertView

        if (listView == null) {
            listView = LayoutInflater.from(parent.context)
                .inflate(R.layout.listview_item_list_token, parent, false)
        }

        val item: ListViewItem = items[position]

        listView!!.findViewById<TextView>(R.id.token_id).text = "No. ${item.tokenID}"
        listView.findViewById<TextView>(R.id.product_name).text = item.productName
        listView.findViewById<TextView>(R.id.brand_name).text = item.brand
        listView.findViewById<TextView>(R.id.token_exp_date).text = item.expirationDate
        listView.findViewById<ImageView>(R.id.product_logo)
            .setImageResource(R.drawable.ic_apple_logo_black)
        listView.setBackgroundResource(colorList[position % colorList.size])

        listView.findViewById<ImageView>(R.id.send_token_btn).setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(parent.context)
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_send_sheet, parent, false)

            view.findViewById<EditText>(R.id.send_from_input).setText(prefs.getString("account", ""))


            bottomSheetDialog.setCancelable(true)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.dismissWithAnimation = true

            bottomSheetDialog.findViewById<ImageView>(R.id.dismiss_send_btn)?.setOnClickListener {
                bottomSheetDialog.cancel()
            }
            bottomSheetDialog.show()
        }

        return listView
    }
}