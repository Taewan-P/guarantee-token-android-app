package dev.chungjungsoo.guaranteewallet.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.adapter.ManuFragmentAdapter
import dev.chungjungsoo.guaranteewallet.tabfragments.*

class ManufacturerFragment : Fragment() {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
            } else {
                Log.i("Permission: ", "Denied")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.type_manu_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireActivity(), R.color.mainColor)
        WindowInsetsControllerCompat(requireActivity().window, view).isAppearanceLightStatusBars =
            false

        val fragmentAdapter = ManuFragmentAdapter(requireActivity())

        fragmentAdapter.addFragment(ListTokenFragment())
        fragmentAdapter.addFragment(VerifyTokenFragment())
        fragmentAdapter.addFragment(MintFragment())
        fragmentAdapter.addFragment(HistoryFragment())
        fragmentAdapter.addFragment(MorePageFragment())


        val pageAdapter: ViewPager2 = requireView().findViewById(R.id.manu_view)

        pageAdapter.adapter = fragmentAdapter

        val bottomNav: TabLayout = requireView().findViewById(R.id.manu_bottom_nav)
        TabLayoutMediator(bottomNav, pageAdapter, false, false) { _, _ -> }.attach()

        val customTab = LayoutInflater.from(context).inflate(R.layout.layout_manu_tab, null, false)
        bottomNav.getTabAt(0)?.customView =
            customTab.findViewById(R.id.list_token_tab) as LinearLayout
        bottomNav.getTabAt(1)?.customView =
            customTab.findViewById(R.id.verify_token_tab) as LinearLayout
        bottomNav.getTabAt(2)?.customView =
            customTab.findViewById(R.id.mint_token_tab) as LinearLayout
        bottomNav.getTabAt(3)?.customView =
            customTab.findViewById(R.id.history_tab) as LinearLayout
        bottomNav.getTabAt(4)?.customView =
            customTab.findViewById(R.id.more_tab) as LinearLayout

        pageAdapter.isUserInputEnabled = false

        pageAdapter.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == 1) {
                    when {
                        ContextCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            Log.d("CAMERA", "Permission is already granted")
                        }

                        ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.CAMERA
                        ) -> {
                            Toast.makeText(
                                requireContext(),
                                "Camera Permission is Required.",
                                Toast.LENGTH_SHORT
                            ).show()
                            requestPermissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }

                        else -> {
                            requestPermissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    }
                }
            }
        })

    }

}