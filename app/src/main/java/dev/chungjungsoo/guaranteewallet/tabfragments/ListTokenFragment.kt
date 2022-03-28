package dev.chungjungsoo.guaranteewallet.tabfragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.activities.RetrofitClass
import dev.chungjungsoo.guaranteewallet.activities.TokenDetailActivity
import dev.chungjungsoo.guaranteewallet.adapter.TokenListViewAdapter
import dev.chungjungsoo.guaranteewallet.dataclass.*
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import java.io.IOException
import kotlin.concurrent.thread

class ListTokenFragment : Fragment() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    private var mContainer: ViewGroup? = null
    lateinit var adapter : TokenListViewAdapter
    lateinit var progressDialog: ProgressBar
    lateinit var items: MutableList<ListViewItem>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContainer = container
        return inflater.inflate(R.layout.tab_list_token_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = requireView().findViewById(R.id.list_progress_bar)
        prefs = PreferenceUtil(requireContext())
        items = mutableListOf()
        adapter = TokenListViewAdapter(items)
        val tokenListView = requireView().findViewById<ListView>(R.id.token_listview)
        val emptyListTextView = requireView().findViewById<TextView>(R.id.no_items_text)

        val tokenListHeaderView = layoutInflater.inflate(R.layout.title_tokens_layout, tokenListView, false)
        tokenListView.addHeaderView(tokenListHeaderView, null, false)
        tokenListView.adapter = adapter

        showProgress(requireActivity())

        thread {
            val tokenCall = getTokenList(prefs.getString("jwt", ""), prefs.getString("account", ""))

            var tokenStatus = false
            var tokenList: List<Int> = listOf()
            if (tokenCall == null) {
                Log.d("TOKENLIST", "Token List fetch failed")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Cannot connect to server", Toast.LENGTH_SHORT).show()
                    tokenListView.emptyView = emptyListTextView
                    hideProgress()
                }
            }
            if (tokenCall?.err == null) {
                // Successful request
                if (tokenCall?.tokens!!.isNotEmpty()) {
                    // Owns token
                    tokenStatus = true
                    tokenList = tokenCall.tokens
                    Log.d("TOKENLIST", "Token list fetch successful")
                }
            } else {
                // Invalid. Error exists
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Invalid Request", Toast.LENGTH_SHORT).show()
                    tokenListView.emptyView = emptyListTextView
                    hideProgress()
                }
            }

            if (tokenStatus) {
                val tokenInfoCall = getTokenInfo(tokenList)

                if (tokenInfoCall == null) {
                    Log.d("TOKENINFO", "Token information fetch failed")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Server connection failed", Toast.LENGTH_SHORT)
                            .show()
                        tokenListView.emptyView = emptyListTextView
                        hideProgress()
                    }
                }
                val tokenInfo = tokenInfoCall?.tokens ?: listOf()
                if (tokenInfo.isNotEmpty()) {
                    tokenInfo.forEach {
                        items.add(
                            ListViewItem(
                                it.tid,
                                it.logo,
                                it.brand,
                                it.name,
                                it.prodDate,
                                it.expDate,
                                it.details
                            )
                        )
                    }
                    if (activity != null) {
                        requireActivity().runOnUiThread {
                            adapter.notifyDataSetChanged()
                            tokenListView.emptyView = emptyListTextView
                            hideProgress()
                        }
                        Log.d("TOKENINFO", "Token information fetch successful")
                    }
                } else {
                    // Empty list.
                    requireActivity().runOnUiThread {
                        tokenListView.emptyView = emptyListTextView
                        hideProgress()
                    }
                }
            }
        }


        val pullToRefresh = requireView().findViewById<SwipeRefreshLayout>(R.id.swipe_to_refresh)

        pullToRefresh.setOnRefreshListener {
            thread {
                val tokenCall =
                    getTokenList(prefs.getString("jwt", ""), prefs.getString("account", ""))

                var tokenStatus = false
                var tokenList: List<Int> = listOf()
                if (tokenCall == null) {
                    Log.d("TOKENLIST", "Token List refresh failed")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Cannot connect to server", Toast.LENGTH_SHORT)
                            .show()
                        pullToRefresh.isRefreshing = false
                    }
                }
                if (tokenCall?.err == null) {
                    // Successful request
                    if (tokenCall?.tokens!!.isNotEmpty()) {
                        // Owns token
                        tokenStatus = true
                        tokenList = tokenCall.tokens
                        Log.d("TOKENLIST", "Token list refresh successful")
                    }
                } else {
                    // Invalid. Error exists
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Invalid Request", Toast.LENGTH_SHORT).show()
                        pullToRefresh.isRefreshing = false
                    }
                }

                if (tokenStatus) {
                    val tokenInfoCall = getTokenInfo(tokenList)

                    if (tokenInfoCall == null) {
                        Log.d("TOKENINFO", "Token information refresh failed")
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, "Server connection failed", Toast.LENGTH_SHORT)
                                .show()
                            pullToRefresh.isRefreshing = false
                        }
                    }
                    val tokenInfo = tokenInfoCall?.tokens ?: listOf()
                    if (tokenInfo.isNotEmpty()) {
                        items.clear()
                        tokenInfo.forEach {
                            items.add(
                                ListViewItem(
                                    it.tid,
                                    it.logo,
                                    it.brand,
                                    it.name,
                                    it.prodDate,
                                    it.expDate,
                                    it.details
                                )
                            )
                        }
                        items.sortBy { it.tokenID }
                        requireActivity().runOnUiThread {
                            adapter.notifyDataSetChanged()
                            pullToRefresh.isRefreshing = false
                        }
                        Log.d("TOKENINFO", "Token information refresh successful")
                    } else {
                        // Empty list.
                        requireActivity().runOnUiThread {
                            tokenListView.emptyView = emptyListTextView
                            pullToRefresh.isRefreshing = false
                        }
                    }
                }
            }
        }

        tokenListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selectedItem: ListViewItem = parent.getItemAtPosition(position) as ListViewItem
                val intent = Intent(activity, TokenDetailActivity::class.java)

                intent.putExtra("tid", selectedItem.tokenID.toString())
                intent.putExtra("logo", selectedItem.logo)
                intent.putExtra("brand", selectedItem.brand)
                intent.putExtra("name", selectedItem.productName)
                intent.putExtra("prodDate", selectedItem.productionDate)
                intent.putExtra("expDate", selectedItem.expirationDate)
                intent.putExtra("details", selectedItem.details)

                val itemView = parent.getChildAt(position - parent.firstVisiblePosition) as RelativeLayout

                val colorList = adapter.getColorList()
                intent.putExtra("color", colorList[((position-1) % colorList.size)].toString())

                ActivityCompat.startActivity(
                    requireContext(), intent,
                    ActivityOptionsCompat
                        .makeSceneTransitionAnimation(
                            requireActivity(),
                            androidx.core.util.Pair(itemView, itemView.transitionName),
                        ).toBundle()
                )
            }

        val myQrBtn = tokenListHeaderView.findViewById<ImageView>(R.id.qr_icon_btn)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_qr_sheet, mContainer,false)
        var qrBtnClicked = false

        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.dismissWithAnimation = true

        bottomSheetDialog.setOnCancelListener {
            qrBtnClicked = false
        }

        val qrImage = createQRCode(prefs.getString("account", ""))
        bottomSheetDialog.findViewById<ImageView>(R.id.my_qr_img)?.setImageBitmap(qrImage)

        myQrBtn.setOnClickListener {
            if (!qrBtnClicked) {
                bottomSheetDialog.show()
                qrBtnClicked = true
            }
        }

    }

    private fun getTokenList(token: String, address: String): GetTokenListResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.getTokenList(token, GetTokenListBody(address = address)).execute()
            response.body()
        } catch (e: IOException) {
            GetTokenListResult(account = address, tokens = listOf(), err = "Network Error")
        } catch (e: NullPointerException) {
            GetTokenListResult(account = address, tokens = listOf(), err = "Invalid Request")
        }

    }

    private fun getTokenInfo(tokens: List<Int>): TokenInfoResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.getTokenInfo(TokenInfoBody(tokens = tokens)).execute()
            response.body()
        } catch (e: IOException) {
            TokenInfoResult(tokens = listOf(), missing = listOf())
        } catch (e: NullPointerException) {
            TokenInfoResult(tokens = listOf(), missing = listOf())
        }
    }

    private fun showProgress(activity: Activity) {
        if (activity.isFinishing) {
            return
        }

        if (progressDialog.visibility != View.VISIBLE) {
            progressDialog.visibility = View.VISIBLE
        }

    }

    private fun hideProgress() {
        if (progressDialog.visibility == View.VISIBLE) {
            progressDialog.visibility = View.GONE
        }
    }

    private fun createQRCode(string: String): Bitmap {
        val size = 250
        val hints = hashMapOf<EncodeHintType, Int>().also { it[EncodeHintType.MARGIN] = 1 }
        val bits = QRCodeWriter().encode(string, BarcodeFormat.QR_CODE, size, size, hints)

        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
        }
    }

    fun setScannedAddress(string: String) {
        adapter.setScannedAddress(string)
    }

    fun getTokenInfo(): Pair<Int, String> {
        return adapter.getTokenReceiverInfo()
    }

    fun disableSendBtn() {
        adapter.disableSend()
    }

    fun disableUI() {
        adapter.disableUI()
    }

    fun enableUI() {
        adapter.enableUI()
    }

    fun dismissDialog(item: Int) {
        adapter.dismissDialog()
        items.removeIf { it.tokenID == item }
        adapter.notifyDataSetChanged()
    }
}