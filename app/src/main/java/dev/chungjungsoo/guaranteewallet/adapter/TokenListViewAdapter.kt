package dev.chungjungsoo.guaranteewallet.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.journeyapps.barcodescanner.ScanOptions
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.activities.AddressScanActivity
import dev.chungjungsoo.guaranteewallet.activities.MainActivity
import dev.chungjungsoo.guaranteewallet.activities.PasswordInputActivity
import dev.chungjungsoo.guaranteewallet.dataclass.ListViewItem
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil


class TokenListViewAdapter(private val items: MutableList<ListViewItem>) : BaseAdapter() {
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

        var clicked = false

        listView.findViewById<ImageView>(R.id.send_token_btn).setOnClickListener {
            if (!clicked) {
                bottomSheetDialog = BottomSheetDialog(parent.context)
                sheetView = LayoutInflater.from(parent.context).inflate(R.layout.layout_send_sheet, parent, false)

                clicked = true

                bottomSheetDialog.setCancelable(true)
                bottomSheetDialog.setContentView(sheetView)
                bottomSheetDialog.dismissWithAnimation = true

                val scanBtn = bottomSheetDialog.findViewById<RelativeLayout>(R.id.scan_address_btn)

                scanBtn?.setOnClickListener {
                    val barcodeScanner = (parent.context as MainActivity).getQRCodeLauncher()
                    val options = ScanOptions()
                    options.captureActivity = AddressScanActivity::class.java
                    options.setBeepEnabled(false)
                    barcodeScanner.launch(options)
                }

                val sendBtn = bottomSheetDialog.findViewById<Button>(R.id.send_token_btn_next)

                sendBtn?.setOnClickListener {
                    val pwInputLauncher = (parent.context as MainActivity).getPWInputLauncher()
                    val intent = Intent(parent.context as MainActivity, PasswordInputActivity::class.java)
                    pwInputLauncher.launch(intent)
                }

                bottomSheetDialog.setOnCancelListener {
                    clicked = false
                }
                bottomSheetDialog.show()

            }

        }

        return listView
    }

    fun setScannedAddress(string: String) {
        sheetView.findViewById<EditText>(R.id.send_to_input).setText(string)
    }
}