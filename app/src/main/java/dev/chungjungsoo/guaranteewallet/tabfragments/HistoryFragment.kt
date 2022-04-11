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
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
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
                requireActivity().runOnUiThread {
                    // Error handling
                    TODO()
                }
            }

            if (historyCall?.err == null) {
                // Successful request
                if (historyCall?.result!!.isNotEmpty()) {
                    // Owns history
                    for (h in historyCall.result) {
                        val tid = h?.get(0)
                        val from = h?.get(1)
                        val to = h?.get(2)
                        val date = h?.get(3)

                        val tableRow = createTableRow((tid as Double).toInt(), from.toString(),
                            to.toString(), date.toString()
                        )
                        requireActivity().runOnUiThread {
                            historyTable.addView(tableRow)
                        }
                    }


                }
                else {
                    // No history
                }
            }
            else {
                // Invalid

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
                bound = "UNKNOWN"
                oneAddress = ""
            }
        }

        val row = TableRow(requireContext())
        val lp = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT)
        row.layoutParams = lp

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
        TextViewCompat.setAutoSizeTextTypeWithDefaults(addressTextView, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
        addressTextView.maxLines = 1
        addressTextView.ellipsize = TextUtils.TruncateAt.END

        row.addView(dateTextView)
        row.addView(tidTextView)
        row.addView(boundTextView)
        row.addView(addressTextView)

        return row
    }
}