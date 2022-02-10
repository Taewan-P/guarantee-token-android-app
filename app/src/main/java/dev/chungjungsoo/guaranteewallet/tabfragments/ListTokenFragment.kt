package dev.chungjungsoo.guaranteewallet.tabfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.adapter.ListViewItem
import dev.chungjungsoo.guaranteewallet.adapter.TokenListViewAdapter

class ListTokenFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_list_token_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = mutableListOf<ListViewItem>()

        items.add(ListViewItem("Airpods Pro", "Apple care+", "2022-07-18"))
        items.add(ListViewItem("Macbook Pro 16 inch", "Basic warranty", "2022-11-25"))
        items.add(ListViewItem("iPad Pro 11 inch", "Apple care+", "2022-05-01"))
        items.add(ListViewItem("Dell U3219Q", "5 Year full care warranty", "2024-11-30"))

        val adapter = TokenListViewAdapter(items)
        val tokenListView = requireView().findViewById<ListView>(R.id.token_listview)

        tokenListView.adapter = adapter
    }
}