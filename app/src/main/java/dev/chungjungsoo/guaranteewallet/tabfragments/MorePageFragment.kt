package dev.chungjungsoo.guaranteewallet.tabfragments

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.activities.MainActivity
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil


class MorePageFragment : Fragment() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_more_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceUtil(requireContext())

        val logOutButton = requireView().findViewById<Button>(R.id.logout_btn)

        logOutButton.setOnClickListener {
            prefs.resetToken()
            restart()
        }

    }

    private fun restart() {
        val packageManager = requireContext().packageManager
        val intent = packageManager.getLaunchIntentForPackage(requireContext().packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        requireContext().startActivity(mainIntent)
        requireActivity().finish()
    }
}