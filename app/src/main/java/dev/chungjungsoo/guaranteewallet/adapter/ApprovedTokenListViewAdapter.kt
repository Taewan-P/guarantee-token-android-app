package dev.chungjungsoo.guaranteewallet.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.ListViewItem
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil

class ApprovedTokenListViewAdapter(private val items: MutableList<ListViewItem>) : BaseAdapter() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var sheetView: View
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

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        TODO("Not yet implemented")
    }
}