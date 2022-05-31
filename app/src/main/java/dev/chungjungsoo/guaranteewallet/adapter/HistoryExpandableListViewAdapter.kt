package dev.chungjungsoo.guaranteewallet.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.dataclass.HistoryItem
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil

class HistoryExpandableListViewAdapter internal constructor(private val context: Context, private val itemList: List<HistoryItem>, private val detailList: HashMap<HistoryItem, List<Pair<String, String>>>) : BaseExpandableListAdapter() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun getGroupCount(): Int {
        return itemList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return this.detailList[this.itemList[groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): HistoryItem {
        return itemList[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Pair<String, String> {
        return this.detailList[this.itemList[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var listView = convertView
        prefs = PreferenceUtil(parent!!.context)

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            listView = inflater.inflate(R.layout.listview_item_history, null)
        }

        val item: HistoryItem = getGroup(groupPosition)
        val transactionType = getTransactionType(item)

        listView!!.findViewById<TextView>(R.id.history_tid).text = "No. ${item.tid}"
        listView.findViewById<TextView>(R.id.history_type).text = transactionType.first
        listView.findViewById<TextView>(R.id.history_type).setTextColor(transactionType.second)
        listView.findViewById<TextView>(R.id.history_date).text = item.time

        return listView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var listView = convertView
        prefs = PreferenceUtil(parent!!.context)

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            listView = inflater.inflate(R.layout.listview_detail_history, null)
        }

        val item: Pair<String, String> = getChild(groupPosition, childPosition)

        listView!!.findViewById<TextView>(R.id.dropdown_from).text = item.first
        listView.findViewById<TextView>(R.id.dropdown_to).text = item.second

        return listView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
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