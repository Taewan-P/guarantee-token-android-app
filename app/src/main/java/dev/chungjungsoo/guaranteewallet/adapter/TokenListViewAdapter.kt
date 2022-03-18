package dev.chungjungsoo.guaranteewallet.adapter

import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
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

                val checkTokenID = bottomSheetDialog.findViewById<TextView>(R.id.token_id_check_value)
                val checkTokenName = bottomSheetDialog.findViewById<TextView>(R.id.token_name_check_value)

                checkTokenID!!.text = "${item.tokenID}"
                checkTokenName!!.text = item.productName

                val scanBtn = bottomSheetDialog.findViewById<RelativeLayout>(R.id.scan_address_btn)

                scanBtn?.setOnClickListener {
                    val barcodeScanner = (parent.context as MainActivity).getQRCodeLauncher()
                    val options = ScanOptions()
                    options.captureActivity = AddressScanActivity::class.java
                    options.setBeepEnabled(false)
                    barcodeScanner.launch(options)
                }

                val sendBtn = bottomSheetDialog.findViewById<Button>(R.id.send_token_btn_next)
                val sendProgressBar = bottomSheetDialog.findViewById<ProgressBar>(R.id.send_progress_bar)
                val reviewCheckBox = bottomSheetDialog.findViewById<CheckBox>(R.id.send_token_review_checkbox)

                disableSend()

                val receiverAddressInput = bottomSheetDialog.findViewById<EditText>(R.id.send_to_input)

                receiverAddressInput!!.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

                    override fun afterTextChanged(s: Editable?) { }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val address = s.toString()
                        if (isAddress(address) && reviewCheckBox!!.isChecked) {
                            enableSend()
                        }
                        else {
                            if (!isAddress(address)) {
                                Log.d("SENDTOKEN", "Address Invalid")
                            }
                            else {
                                Log.d("SENDTOKEN", "Checkbox not checked")
                            }
                            disableSend()
                        }
                    }
                })

                reviewCheckBox!!.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && isAddress(receiverAddressInput.text.toString())) {
                        enableSend()
                    }
                    else {
                        disableSend()
                    }
                }

                sendBtn?.setOnClickListener {
                    val pwInputLauncher = (parent.context as MainActivity).getPWInputLauncher()
                    val intent = Intent(parent.context as MainActivity, PasswordInputActivity::class.java)

                    sendProgressBar!!.visibility = View.VISIBLE
                    sendBtn.isEnabled = false
                    sendBtn.alpha = 0.5F
                    sendBtn.setTextColor(ContextCompat.getColor(parent.context, R.color.cardColor6))
                    bottomSheetDialog.setCancelable(false)
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

    fun getTokenReceiverInfo(): Pair<Int, String> {
        val tid = sheetView.findViewById<TextView>(R.id.token_id_check_value).text.toString().toInt()
        val receiver = sheetView.findViewById<EditText>(R.id.send_to_input).text.toString()

        return Pair(tid, receiver)
    }

    fun disableSend() {
        val sendBtn = sheetView.findViewById<Button>(R.id.send_token_btn_next)
        sendBtn.isEnabled = false
        sendBtn.alpha = 0.5F
        sendBtn.setTextColor(Color.parseColor("#a7a9ac"))
    }

    fun enableSend() {
        val sendBtn = sheetView.findViewById<Button>(R.id.send_token_btn_next)
        sendBtn.isEnabled = true
        sendBtn.alpha = 1F
        sendBtn.setTextColor(Color.parseColor("#000000"))
    }

    fun isAddress(address: String): Boolean {
        val addressRegex = """^(0x)[0-9a-fA-F]{40}$""".toRegex()
        if (addressRegex.matchEntire(address)?.value == null) { return false }

        return addressRegex.matchEntire(address)?.value == address
    }
}