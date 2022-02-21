package dev.chungjungsoo.guaranteewallet.tabfragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.activities.RetrofitClass
import dev.chungjungsoo.guaranteewallet.activities.TokenDetailActivity
import dev.chungjungsoo.guaranteewallet.adapter.TokenListViewAdapter
import dev.chungjungsoo.guaranteewallet.dataclass.*
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import java.io.IOException
import kotlin.concurrent.thread

class ListTokenFragment : Fragment() {
    companion object { lateinit var prefs: PreferenceUtil }
    lateinit var progressDialog : ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_list_token_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = requireView().findViewById(R.id.list_progress_bar)
        prefs = PreferenceUtil(requireContext())
        val items = mutableListOf<ListViewItem>()
        val adapter = TokenListViewAdapter(items)
        val tokenListView = requireView().findViewById<ListView>(R.id.token_listview)
        val emptyListTextView = requireView().findViewById<TextView>(R.id.no_items_text)

        tokenListView.addHeaderView(layoutInflater.inflate(R.layout.title_tokens_layout, tokenListView, false), null, false)
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
            }
            else {
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
                        Toast.makeText(context, "Server connection failed", Toast.LENGTH_SHORT).show()
                        tokenListView.emptyView = emptyListTextView
                        hideProgress()
                    }
                }
                val tokenInfo = tokenInfoCall?.tokens ?: listOf()
                if (tokenInfo.isNotEmpty()) {
                    tokenInfo.forEach {
                        items.add(ListViewItem(it.tid, it.logo, it.brand, it.name, it.prodDate, it.expDate, it.details))
                    }
                    items.sortBy { it.tokenID }
                    requireActivity().runOnUiThread {
                        adapter.notifyDataSetChanged()
                        tokenListView.emptyView = emptyListTextView
                        hideProgress()
                    }
                    Log.d("TOKENINFO", "Token information fetch successful")
                }
                else {
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
                val tokenCall = getTokenList(prefs.getString("jwt", ""), prefs.getString("account", ""))

                var tokenStatus = false
                var tokenList: List<Int> = listOf()
                if (tokenCall == null) {
                    Log.d("TOKENLIST", "Token List refresh failed")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Cannot connect to server", Toast.LENGTH_SHORT).show()
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
                }
                else {
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
                            Toast.makeText(context, "Server connection failed", Toast.LENGTH_SHORT).show()
                            pullToRefresh.isRefreshing = false
                        }
                    }
                    val tokenInfo = tokenInfoCall?.tokens ?: listOf()
                    if (tokenInfo.isNotEmpty()) {
                        items.clear()
                        tokenInfo.forEach {
                            items.add(ListViewItem(it.tid, it.logo, it.brand, it.name, it.prodDate, it.expDate, it.details))
                        }
                        items.sortBy { it.tokenID }
                        requireActivity().runOnUiThread {
                            adapter.notifyDataSetChanged()
                            pullToRefresh.isRefreshing = false
                        }
                        Log.d("TOKENINFO", "Token information refresh successful")
                    }
                    else {
                        // Empty list.
                        requireActivity().runOnUiThread {
                            tokenListView.emptyView = emptyListTextView
                            pullToRefresh.isRefreshing = false
                        }
                    }
                }
            }
        }


        tokenListView.onItemClickListener = AdapterView.OnItemClickListener {
                parent, _, position, _ ->
            val selectedItem : ListViewItem = parent.getItemAtPosition(position) as ListViewItem
            val intent = Intent(activity, TokenDetailActivity::class.java)

            intent.putExtra("tid", selectedItem.tokenID.toString())
            intent.putExtra("logo", selectedItem.logo)
            intent.putExtra("brand", selectedItem.brand)
            intent.putExtra("name", selectedItem.productName)
            intent.putExtra("prodDate", selectedItem.productionDate)
            intent.putExtra("expDate", selectedItem.expirationDate)
            intent.putExtra("details", selectedItem.details)
            startActivity(intent)
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
            response.body()
        } catch (e: IOException) {
            TokenInfoResult(tokens = listOf(), missing = listOf())
        } catch (e : NullPointerException) {
            TokenInfoResult(tokens = listOf(), missing = listOf())
        }
    }

    private fun showProgress(activity: Activity) {
        if (activity.isFinishing) { return }

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