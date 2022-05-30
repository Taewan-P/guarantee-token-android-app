package dev.chungjungsoo.guaranteewallet.tabfragments

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.activities.RetrofitClass
import dev.chungjungsoo.guaranteewallet.adapter.HistoryListViewAdapter
import dev.chungjungsoo.guaranteewallet.dataclass.GetHistoryBody
import dev.chungjungsoo.guaranteewallet.dataclass.GetHistoryResult
import dev.chungjungsoo.guaranteewallet.dataclass.HistoryItem
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import org.w3c.dom.Text
import java.io.IOException
import java.lang.NullPointerException
import kotlin.concurrent.thread

class HistoryFragment : Fragment() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }
    lateinit var progressDialog: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_history_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceUtil(requireContext())
        val account = prefs.getString("account", "")
        val token = prefs.getString("jwt", "")

        progressDialog = requireView().findViewById(R.id.list_progress_bar)
        val items = mutableListOf<HistoryItem>()
        val adapter = HistoryListViewAdapter(items)
        val historyListView = requireView().findViewById<ListView>(R.id.history_listview)
        val historyListHeaderView = layoutInflater.inflate(R.layout.title_history_layout, historyListView, false)
        val emptyListTextView = requireView().findViewById<TextView>(R.id.no_items_text)

        historyListView.addHeaderView(historyListHeaderView, null, false)
        historyListView.adapter = adapter

        showProgress(requireActivity())

        if (isAdded) {
            thread {
                val historyCall = getHistory(token, account)

                if (historyCall == null) {
                    Log.e("HISTORY", "History fetch failed")
                    if (isAdded) {
                        // Error handling
                        requireActivity().runOnUiThread {
                            val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                            val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                            resultText.text = "History unavailable. Please try again."
                            val alertDialog = AlertDialog.Builder(requireContext())
                                .setTitle("Error")
                                .setPositiveButton("OK") { _, _ -> }
                                .create()

                            alertDialog.setView(dialogView)
                            alertDialog.show()
                            hideProgress()
                            items.clear()
                            adapter.notifyDataSetChanged()
                            emptyListTextView.visibility = View.GONE
                        }
                        Thread.currentThread().interrupt()
                    }
                }

                if (historyCall?.err == null) {
                    // Successful request
                    val historyResult = historyCall?.result ?: listOf()

                    if (historyCall?.result!!.isNotEmpty()) {
                        // Owns history
                        historyResult.forEach {
                            if (it is HistoryItem) {
                                items.add(
                                    HistoryItem(
                                        it.tid,
                                        it.from,
                                        it.to,
                                        it.time
                                    )
                                )
                            }
                        }
                        if (activity != null) {
                            requireActivity().runOnUiThread {
                                adapter.notifyDataSetChanged()
                                emptyListTextView.visibility = View.GONE
                                hideProgress()
                            }
                            Log.d("HISTORY", "History fetch successful")
                        }
                    }
                    else {
                        // No history. Do nothing.
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                emptyListTextView.visibility = View.VISIBLE
                                hideProgress()
                            }
                        }
                    }
                }
                else {
                    // Invalid
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                            val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                            resultText.text = "Invalid request."
                            val alertDialog = AlertDialog.Builder(requireContext())
                                .setTitle("Error")
                                .setPositiveButton("OK") { _, _ -> }
                                .create()

                            alertDialog.setView(dialogView)
                            alertDialog.show()
                        }
                    }
                }
            }
        }

        val pullToRefresh = requireView().findViewById<SwipeRefreshLayout>(R.id.swipe_to_refresh)

        pullToRefresh.setOnRefreshListener {
            thread {
                val historyCall = getHistory(token, account)

                if (historyCall == null) {
                    Log.e("HISTORY", "History refresh failed")
                    requireActivity().runOnUiThread {
                        // Error handling
                        pullToRefresh.isRefreshing = false
                        val dialogView = layoutInflater.inflate(R.layout.layout_transfer_result,null)
                        val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                        resultText.text = "History unavailable. Please try again."
                        val alertDialog = AlertDialog.Builder(requireContext())
                            .setTitle("Error")
                            .setPositiveButton("OK") { _, _ -> }
                            .create()

                        alertDialog.setView(dialogView)
                        alertDialog.show()
                        items.clear()
                        adapter.notifyDataSetChanged()
                        emptyListTextView.visibility = View.VISIBLE
                    }
                    Thread.currentThread().interrupt()
                }

                if (historyCall?.err == null) {
                    // Successful request
                    Log.d("HISTORY", "History refresh successful")
                    val historyResult = historyCall?.result ?: listOf()
                    if (historyCall?.result?.isNotEmpty() == true) {
                        // Owns history
                        items.clear()
                        historyResult.forEach {
                            if (it is HistoryItem) {
                                items.add(
                                    HistoryItem(
                                        it.tid,
                                        it.from,
                                        it.to,
                                        it.time
                                    )
                                )
                            }
                        }

                        requireActivity().runOnUiThread {
                            pullToRefresh.isRefreshing = false
                            emptyListTextView.visibility = View.GONE
                            adapter.notifyDataSetChanged()
                        }
                    }
                    else {
                        // No history
                        requireActivity().runOnUiThread {
                            pullToRefresh.isRefreshing = false
                            emptyListTextView.visibility = View.VISIBLE
                        }
                    }
                }
                else {
                    // Invalid
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
                        pullToRefresh.isRefreshing = false
                        items.clear()
                        adapter.notifyDataSetChanged()
                        emptyListTextView.visibility = View.VISIBLE
                    }
                    Thread.currentThread().interrupt()
                }
            }
        }
    }

    private fun getHistory(token: String, address: String) : GetHistoryResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.getHistory(token, GetHistoryBody(address=address)).execute()
            response.body()
        } catch (e: IOException) {
            GetHistoryResult(result = listOf(), err = "Network Error")
        } catch (e: NullPointerException) {
            GetHistoryResult(result = listOf(), err = "Invalid Request")
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
}