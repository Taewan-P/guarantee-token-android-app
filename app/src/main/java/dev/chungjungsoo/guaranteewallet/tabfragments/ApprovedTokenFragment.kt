package dev.chungjungsoo.guaranteewallet.tabfragments

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.activities.RetrofitClass
import dev.chungjungsoo.guaranteewallet.adapter.ApprovedTokenListViewAdapter
import dev.chungjungsoo.guaranteewallet.dataclass.*
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import java.io.IOException
import kotlin.concurrent.thread

class ApprovedTokenFragment : Fragment() {

//    This is for resellers to check what tokens they have permissions to send.
    companion object {
        lateinit var prefs: PreferenceUtil
    }

        private var mContainer: ViewGroup? = null
        lateinit var adapter : ApprovedTokenListViewAdapter
        lateinit var progressDialog: ProgressBar
        lateinit var items: MutableList<ListViewItem>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContainer = container
        return inflater.inflate(R.layout.tab_approved_token_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = requireView().findViewById(R.id.list_progress_bar)
        prefs = PreferenceUtil(requireContext())
        items = mutableListOf()
        adapter = ApprovedTokenListViewAdapter(items)

        val tokenListView = requireView().findViewById<ListView>(R.id.token_listview)
        val emptyListTextView = requireView().findViewById<TextView>(R.id.no_items_text)

        val tokenListHeaderView = layoutInflater.inflate(R.layout.title_tokens_layout, tokenListView, false)
        tokenListHeaderView.findViewById<TextView>(R.id.token_title).text = "Approved"
        tokenListHeaderView.findViewById<ImageView>(R.id.qr_icon_btn).visibility = View.GONE
        tokenListView.addHeaderView(tokenListHeaderView, null, false)
        tokenListView.adapter = adapter

        showProgress(requireActivity())

        thread {
            val approvedCall = getApprovedTokenList(prefs.getString("jwt", null), prefs.getString("account", ""))

            var tokenStatus = false
            var tokenList: List<Int> = listOf()

            if (approvedCall == null) {
                Log.e("APPROVEDLIST", "Approved token list fetch failed")

                if (isAdded) {
                    requireActivity().runOnUiThread {
                        val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                        val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                        resultText.text = "Cannot connect to server."
                        val alertDialog = AlertDialog.Builder(requireContext())
                            .setTitle("Error")
                            .setPositiveButton("OK") { _, _ -> }
                            .create()

                        alertDialog.setView(dialogView)
                        alertDialog.show()
                        hideProgress()
                        emptyListTextView.visibility = View.VISIBLE
                    }
                    Thread.currentThread().interrupt()
                }
            }

            if (approvedCall?.err == null) {
                // Successful request
                if (approvedCall?.approved != null) {
                    // Reseller has called the request
                    if (approvedCall.approved.isNotEmpty()) {
                        // Owns approved token
                        tokenStatus = true
                        tokenList = approvedCall.approved
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                emptyListTextView.visibility = View.GONE
                            }
                            Log.d("APPROVEDLIST", "Approved token list fetch successful")
                        }
                    }
                    else {
                        // Empty token list
                        tokenStatus = false
                        Log.d("APPROVEDLIST", "Empty token list")
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                hideProgress()
                                emptyListTextView.visibility = View.VISIBLE
                            }
                        }
                    }
                }
                else {
                    // Should not show anything. Not a reseller
                    tokenStatus = false
                    Log.d("APPROVEDLIST", "Not a reseller")
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            hideProgress()
                            emptyListTextView.visibility = View.VISIBLE
                        }
                    }
                }
            }
            else {
                // Invalid. Error exists
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                        val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                        resultText.text = "Invalid request. Please try again."
                        val alertDialog = android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Error")
                            .setPositiveButton("OK") { _, _ -> }
                            .create()

                        alertDialog.setView(dialogView)
                        alertDialog.show()
                        hideProgress()
                        emptyListTextView.visibility = View.VISIBLE
                    }
                    Thread.currentThread().interrupt()
                }
            }

            if (tokenStatus) {
                val tokenInfoCall = getTokenInfo(tokenList)
                if (tokenInfoCall == null) {
                    Log.e("APPROVEDTOKENINFO", "Approved token information fetch failed")
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                            val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                            resultText.text = "Server connection failed. Please check your network status."
                            val alertDialog = android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Error")
                                .setPositiveButton("OK") { _, _ -> }
                                .create()

                            alertDialog.setView(dialogView)
                            alertDialog.show()
                            hideProgress()
                        }
                        Thread.currentThread().interrupt()
                    }
                }

                val tokenInfo = tokenInfoCall?.tokens ?: listOf()
                if (tokenInfo.isNotEmpty()) {
                    tokenInfo.forEach {
                        items.add(
                            ListViewItem(
                                it.tid,
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
                            hideProgress()
                        }
                        Log.d("APPROVEDTOKENINFO", "Token information fetch successful")
                    }
                }
                else {
                    // Empty list.
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            emptyListTextView.visibility = View.VISIBLE
                            hideProgress()
                        }
                    }
                }
            }
        }

        val pullToRefresh = requireView().findViewById<SwipeRefreshLayout>(R.id.swipe_to_refresh)

        pullToRefresh.setOnRefreshListener {
            thread {
                val approvedCall = getApprovedTokenList(prefs.getString("jwt", null), prefs.getString("account", ""))

                var tokenStatus = false
                var tokenList: List<Int> = listOf()

                if (approvedCall == null) {
                    Log.e("APPROVEDLIST", "Approved token list refresh failed")

                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                            val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                            resultText.text = "Server connection unstable. Please check your network status."
                            val alertDialog = android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Error")
                                .setPositiveButton("OK") { _, _ -> }
                                .create()

                            alertDialog.setView(dialogView)
                            alertDialog.show()

                            emptyListTextView.visibility = View.VISIBLE
                            pullToRefresh.isRefreshing = false
                        }
                        Thread.currentThread().interrupt()
                    }
                }

                else {
                    if (approvedCall.err == null) {
                        // Successful Request
                        if (approvedCall.approved != null) {
                            // Reseller has called the request
                            if (approvedCall.approved.isNotEmpty()) {
                                // Owns approved token
                                tokenStatus = true
                                tokenList = approvedCall.approved
                                Log.d("APPROVEDLIST", "Approved token list refresh successful.")
                            }
                            else {
                                // Empty token list
                                tokenStatus = false
                                Log.d("APPROVEDLIST", "Empty token list")
                                if (isAdded) {
                                    requireActivity().runOnUiThread {
                                        hideProgress()
                                        emptyListTextView.visibility = View.VISIBLE
                                        pullToRefresh.isRefreshing = false
                                    }
                                }
                            }
                        }
                        else {
                            // Not a reseller
                            tokenStatus = false
                            Log.d("APPROVEDLIST", "Not a reseller")
                            if (isAdded) {
                                requireActivity().runOnUiThread {
                                    hideProgress()
                                    emptyListTextView.visibility = View.VISIBLE
                                    pullToRefresh.isRefreshing = false
                                }
                            }
                        }
                    }
                    else {
                        // Invalid. Error exists
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                                val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                                resultText.text = "Invalid request. Please try again."
                                val alertDialog = AlertDialog.Builder(requireContext())
                                    .setTitle("Error")
                                    .setPositiveButton("OK") { _, _ -> }
                                    .create()

                                alertDialog.setView(dialogView)
                                alertDialog.show()

                                hideProgress()
                                emptyListTextView.visibility = View.VISIBLE
                                pullToRefresh.isRefreshing = false
                            }
                            Thread.currentThread().interrupt()
                        }
                    }


                    if (tokenStatus) {
                        val tokenInfoCall = getTokenInfo(tokenList)

                        if (tokenInfoCall == null) {
                            Log.e("APPROVEDTOKENINFO", "Approved token info refresh failed")
                            if (isAdded) {
                                requireActivity().runOnUiThread {
                                    val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                                    val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                                    resultText.text = "Server connection unstable. Please check your network status."
                                    val alertDialog = android.app.AlertDialog.Builder(requireContext())
                                        .setTitle("Error")
                                        .setPositiveButton("OK") { _, _ -> }
                                        .create()

                                    alertDialog.setView(dialogView)
                                    alertDialog.show()

                                    pullToRefresh.isRefreshing = false
                                }
                                Thread.currentThread().interrupt()
                            }
                        }
                        else {
                            val tokenInfo = tokenInfoCall.tokens

                            if (tokenInfo.isNotEmpty()) {
                                items.clear()
                                tokenInfo.forEach {
                                    items.add(
                                        ListViewItem(
                                            it.tid,
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
                                        pullToRefresh.isRefreshing = false
                                    }
                                    Log.d("APPROVEDTOKENINFO", "Token information refresh successful")
                                }
                            }
                            else {
                                // Empty list
                                if (isAdded) {
                                    requireActivity().runOnUiThread {
                                        emptyListTextView.visibility = View.VISIBLE
                                        pullToRefresh.isRefreshing = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

    private fun getApprovedTokenList(token: String, address: String): GetTokenListResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.getTokenList(token, GetTokenListBody(address = address)).execute()
            response.body()
        } catch (e: IOException) {
            GetTokenListResult(account = address, tokens = listOf(), approved = null, err = "Network Error")
        } catch (e: NullPointerException) {
            GetTokenListResult(account = address, tokens = listOf(), approved = null, err = "Invalid Request")
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

    fun setScannedAddress(string: String) {
        adapter.setScannedAddress(string)
    }

    fun getTokenInfo(): Pair<Int, String> {
        return adapter.getTokenReceiverInfo()
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
