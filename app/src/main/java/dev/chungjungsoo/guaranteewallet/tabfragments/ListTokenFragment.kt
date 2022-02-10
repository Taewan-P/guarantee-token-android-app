package dev.chungjungsoo.guaranteewallet.tabfragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.activities.MainActivity
import dev.chungjungsoo.guaranteewallet.activities.RetrofitClass
import dev.chungjungsoo.guaranteewallet.adapter.ListViewItem
import dev.chungjungsoo.guaranteewallet.adapter.TokenListViewAdapter
import dev.chungjungsoo.guaranteewallet.dataclass.GetTokenListBody
import dev.chungjungsoo.guaranteewallet.dataclass.GetTokenListResult
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import java.io.IOException
import java.lang.NullPointerException
import kotlin.concurrent.thread

class ListTokenFragment : Fragment() {
    companion object { lateinit var prefs: PreferenceUtil }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_list_token_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceUtil(requireContext())
        val items = mutableListOf<ListViewItem>()

        thread {
            val tokenCall = getTokenList(prefs.getString("jwt", ""), prefs.getString("account", ""))

            var tokenStatus = false
            var tokenList: List<Int>
            if (tokenCall == null) {
                Log.d("TOKENLIST", "Token List fetch failed")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Cannot connect to server", Toast.LENGTH_SHORT).show()
                }
            }
            if (tokenCall?.err == null) {
                // Successful request
                if (tokenCall?.tokens!!.isNotEmpty()) {
                    // Owns token
                    tokenStatus = true
                    tokenList = tokenCall.tokens
                    Log.d("TOKENLIST", "$tokenList")
                }
            }
            else {
                // Invalid. Error exists
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Invalid Request", Toast.LENGTH_SHORT).show()
                }
            }


        }


        items.add(ListViewItem("Airpods Pro", "Apple care+", "2022-07-18"))
        items.add(ListViewItem("Macbook Pro 16 inch", "Basic warranty", "2022-11-25"))
        items.add(ListViewItem("iPad Pro 11 inch", "Apple care+", "2022-05-01"))
        items.add(ListViewItem("Dell U3219Q", "5 Year full care warranty", "2024-11-30"))

        val adapter = TokenListViewAdapter(items)
        val tokenListView = requireView().findViewById<ListView>(R.id.token_listview)

        tokenListView.adapter = adapter
    }

    fun getTokenList(token : String, address : String) : GetTokenListResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.getTokenList(token, GetTokenListBody(address=address)).execute()
            response.body()
        } catch (e: IOException)  {
            GetTokenListResult(account = address, tokens = listOf(), err = "Network Error")
        } catch (e: NullPointerException) {
            GetTokenListResult(account = address, tokens = listOf(), err = "Invalid Request")
        }

    }
}