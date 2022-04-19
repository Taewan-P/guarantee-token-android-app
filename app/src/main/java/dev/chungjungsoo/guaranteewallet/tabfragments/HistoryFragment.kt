package dev.chungjungsoo.guaranteewallet.tabfragments

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.activities.RetrofitClass
import dev.chungjungsoo.guaranteewallet.dataclass.GetHistoryBody
import dev.chungjungsoo.guaranteewallet.dataclass.GetHistoryResult
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import org.w3c.dom.Text
import java.io.IOException
import java.lang.NullPointerException
import kotlin.concurrent.thread

class HistoryFragment : Fragment() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }

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
        val historyTable = requireView().findViewById<TableLayout>(R.id.history_table)

        thread {
            val historyCall = getHistory(token, account)

            if (historyCall == null) {
                Log.e("HISTORY", "History fetch failed")
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        // Error handling
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "History unavailable. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            if (historyCall?.err == null) {
                // Successful request
                Log.d("HISTORY", "History fetch successful")
                if (historyCall?.result!!.isNotEmpty()) {
                    // Owns history
                    for (h in historyCall.result) {
                        val tid = h!!.tid
                        val from = h.from
                        val to = h.to
                        val date = h.time

                        val tableRow = createTableRow(tid, from.toString(), to, date)

                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                historyTable.addView(tableRow)
                            }
                        }
                    }
                }
                else {
                    // No history. Do nothing.
                }
            }
            else {
                // Invalid
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error occurred.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val pullToRefresh = requireView().findViewById<SwipeRefreshLayout>(R.id.history_refresh)

        pullToRefresh.setOnRefreshListener {
            thread {
                val historyCall = getHistory(token, account)

                if (historyCall == null) {
                    Log.e("HISTORY", "History refresh failed")
                    requireActivity().runOnUiThread {
                        // Error handling
                        pullToRefresh.isRefreshing = false
                        Toast.makeText(requireContext(), "History unavailable. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                if (historyCall?.err == null) {
                    // Successful request
                    Log.d("HISTORY", "History refresh successful")
                    val records = historyTable.childCount

                    if (historyCall?.result!!.isNotEmpty()) {
                        // Owns history
                        requireActivity().runOnUiThread {
                            pullToRefresh.isRefreshing = false
                            historyTable.removeViews(1, records-1)
                        }
                        for (h in historyCall.result) {
                            val tid = h!!.tid
                            val from = h.from
                            val to = h.to
                            val date = h.time

                            val tableRow = createTableRow(tid, from.toString(), to, date)

                            requireActivity().runOnUiThread {
                                historyTable.addView(tableRow)
                            }
                        }
                    }
                    else {
                        // No history
                        requireActivity().runOnUiThread {
                            pullToRefresh.isRefreshing = false
                        }
                    }
                }
                else {
                    // Invalid
                    requireActivity().runOnUiThread {
                        pullToRefresh.isRefreshing = false
                        Toast.makeText(requireContext(), "Error occurred.", Toast.LENGTH_SHORT).show()
                    }
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

    private fun createTableRow(tid: Int, from: String?, to: String, date: String): TableRow {
        val address = prefs.getString("account", null)
        val oneAddress : String
        val bound : String
        when {
            from == "null" || from == null -> {
                // Token Minting
                bound = "MINT"
                oneAddress = ""
            }
            from == address -> {
                // Me sending the token
                bound = "SENT"
                oneAddress = to

            }
            to == address -> {
                // Me receiving the token
                bound = "RECEIVED"
                oneAddress = from
            }
            else -> {
                // Error?
                bound = "UNKNOWN"
                oneAddress = ""
            }
        }

        val row = TableRow(requireContext())
        val lp = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT)
        row.layoutParams = lp
        row.setBackgroundResource(R.drawable.table_border)

        val tidTextView = TextView(requireContext())
        val boundTextView = TextView(requireContext())
        val addressTextView = TextView(requireContext())
        val dateTextView = TextView(requireContext())

        tidTextView.text = "$tid"
        boundTextView.text = bound
        addressTextView.text = oneAddress
        dateTextView.text = date

        tidTextView.setTextColor(Color.parseColor("#FFFFFF"))
        boundTextView.setTextColor(Color.parseColor("#FFFFFF"))
        addressTextView.setTextColor(Color.parseColor("#FFFFFF"))
        dateTextView.setTextColor(Color.parseColor("#FFFFFF"))

        tidTextView.gravity = Gravity.CENTER
        boundTextView.gravity = Gravity.CENTER

        row.addView(dateTextView)
        row.addView(tidTextView)
        row.addView(boundTextView)
        row.addView(addressTextView)

        row.setPadding(dpToPixel(10), dpToPixel(10), dpToPixel(10), dpToPixel(10))

        return row
    }

    private fun dpToPixel(dp: Int) : Int {
        val density = requireContext().resources.displayMetrics.density
        return (dp * density).toInt()
    }
}