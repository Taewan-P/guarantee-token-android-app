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
import dev.chungjungsoo.guaranteewallet.dataclass.TokenInfoBody
import dev.chungjungsoo.guaranteewallet.dataclass.TokenInfoResult
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
        val adapter = TokenListViewAdapter(items)
        val tokenListView = requireView().findViewById<ListView>(R.id.token_listview)

        tokenListView.adapter = adapter

        thread {
            val tokenCall = getTokenList(prefs.getString("jwt", ""), prefs.getString("account", ""))

            var tokenStatus = false
            var tokenList: List<Int> = listOf()
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

            if (tokenStatus) {
                val tokenInfoCall = getTokenInfo(tokenList)

                if (tokenInfoCall == null) {
                    Log.d("TOKENINFO", "Token information fetch failed")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Server connection failed", Toast.LENGTH_SHORT).show()
                    }
                }
                val tokenInfo = tokenInfoCall?.tokens ?: listOf()
                Log.d("TOKENINFO", "$tokenInfoCall")
                if (tokenInfo.isNotEmpty()) {
                    tokenInfo.forEach {
                        items.add(ListViewItem(it.name, it.details, it.expDate))
                    }
                    Log.d("ITEMS", "$items")
                    requireActivity().runOnUiThread {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }


    }

    private fun getTokenList(token : String, address : String) : GetTokenListResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.getTokenList(token, GetTokenListBody(address=address)).execute()
            response.body()
        } catch (e: IOException) {
            GetTokenListResult(account = address, tokens = listOf(), err = "Network Error")
        } catch (e: NullPointerException) {
            GetTokenListResult(account = address, tokens = listOf(), err = "Invalid Request")
        }

    }

    private fun getTokenInfo(tokens : List<Int>) : TokenInfoResult? {
        val server = RetrofitClass.getInstance()

        return try {
            val response = server.getTokenInfo(TokenInfoBody(tokens = tokens)).execute()
            Log.d("getTokenInfo", "$response")
            response.body()
        } catch (e: IOException) {
            Log.d("getTokenInfo", "IOEXCEPTION")
            TokenInfoResult(tokens = listOf(), missing = listOf())
        } catch (e : NullPointerException) {
            Log.d("getTokenInfo", "NULLEXCEPTION")
            TokenInfoResult(tokens = listOf(), missing = listOf())
        }
    }
}