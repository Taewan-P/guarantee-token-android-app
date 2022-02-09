package dev.chungjungsoo.guaranteewallet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.adapter.ManuFragmentAdapter
import dev.chungjungsoo.guaranteewallet.tabfragments.ListTokenFragment
import dev.chungjungsoo.guaranteewallet.tabfragments.MorePageFragment
import dev.chungjungsoo.guaranteewallet.tabfragments.SendTokenFragment
import dev.chungjungsoo.guaranteewallet.tabfragments.VerifyTokenFragment

class ManufacturerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.type_manu_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.mainColor)
        WindowInsetsControllerCompat(requireActivity().window, view).isAppearanceLightStatusBars = false

        val fragmentAdapter = ManuFragmentAdapter(requireActivity())

        fragmentAdapter.addFragment(ListTokenFragment())
        fragmentAdapter.addFragment(SendTokenFragment())
        fragmentAdapter.addFragment(VerifyTokenFragment())
        fragmentAdapter.addFragment(MorePageFragment())



        val pageAdapter : ViewPager2 = requireView().findViewById(R.id.manu_view)

        pageAdapter.adapter = fragmentAdapter

        val bottomNav : TabLayout = requireView().findViewById(R.id.manu_bottom_nav)
        TabLayoutMediator(bottomNav, pageAdapter) { _, _ -> }.attach()

        val customTab  = LayoutInflater.from(context).inflate(R.layout.layout_manu_tab, null, false)
        bottomNav.getTabAt(0)?.customView = customTab.findViewById(R.id.list_token_tab) as LinearLayout
        bottomNav.getTabAt(1)?.customView = customTab.findViewById(R.id.send_token_tab) as LinearLayout
        bottomNav.getTabAt(2)?.customView = customTab.findViewById(R.id.verify_token_tab) as LinearLayout
        bottomNav.getTabAt(3)?.customView = customTab.findViewById(R.id.more_tab) as LinearLayout


        pageAdapter.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("PAGEADAPTER", "Page ${position + 1}")
            }
        })

    }

}