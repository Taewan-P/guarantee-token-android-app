package dev.chungjungsoo.guaranteewallet.tabfragments

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.activities.MainActivity
import dev.chungjungsoo.guaranteewallet.activities.PasswordInputActivity
import dev.chungjungsoo.guaranteewallet.activities.RetrofitClass
import dev.chungjungsoo.guaranteewallet.dataclass.MintTokenBody
import dev.chungjungsoo.guaranteewallet.dataclass.MintTokenResult
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import java.io.IOException
import java.lang.NullPointerException
import java.util.*
import kotlin.concurrent.thread

class MintFragment : Fragment() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }
    lateinit var progressDialog: AppCompatDialog
    private val passwordActivityLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            Log.d("PW", "PW input successful")
            val prodDatePickerBtn = requireView().findViewById<Button>(R.id.prod_date_btn)
            val expDatePickerBtn = requireView().findViewById<Button>(R.id.exp_date_btn)

            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_transfer_result, null)
            val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

            val productName = requireView().findViewById<EditText>(R.id.prod_name_input).text.toString()
            val productionDate = prodDatePickerBtn.text.toString()
            val expirationDate = expDatePickerBtn.text.toString()
            val details = requireView().findViewById<EditText>(R.id.details_input).text.toString()

            val pw = it.data?.getStringExtra("pw") ?: ""

            thread {
                val mintResult = mintToken(pw, productName = productName,
                    productionDate = productionDate,
                    expirationDate = expirationDate,
                    details = details)

                if (mintResult == null) {
                    Log.e("MINT", "Mint failed with result null.")
                }

                when (mintResult?.result ?: "failed") {
                    "success" -> {
                        Log.d("MINT", "Mint Successful.")
                        requireActivity().runOnUiThread {
                            resultText.text = "Mint Successful."

                            val alertDialog = AlertDialog.Builder(requireContext())
                                .setTitle("Result")
                                .setPositiveButton("OK") { _, _ -> }
                                .create()

                            alertDialog.setView(dialogView)
                            alertDialog.show()
                            hideProgress()
                            clearInput()
                        }
                    }
                    "failed" -> {
                        when (mintResult?.err ?: "Unknown error") {
                            "Network Error" -> {
                                Log.e("MINT", "Network Error")
                                requireActivity().runOnUiThread {
                                    resultText.text = "Network Error. Try again in a few moments."

                                    val alertDialog = AlertDialog.Builder(requireContext())
                                        .setTitle("Result")
                                        .setPositiveButton("OK") { _, _ -> }
                                        .create()

                                    alertDialog.setView(dialogView)
                                    alertDialog.show()
                                    hideProgress()
                                }
                            }
                            "Node Network Error" -> {
                                Log.e("MINT", "Node Network Error")
                                requireActivity().runOnUiThread {
                                    resultText.text = "Node Network Error. Try again in a few moments."

                                    val alertDialog = AlertDialog.Builder(requireContext())
                                        .setTitle("Result")
                                        .setPositiveButton("OK") { _, _ -> }
                                        .create()

                                    alertDialog.setView(dialogView)
                                    alertDialog.show()
                                    hideProgress()
                                }
                            }
                            "Authentication Error" -> {
                                Log.e("MINT", "Authentication Error")
                                requireActivity().runOnUiThread {
                                    resultText.text = "Authentication Error. Is your password correct?"

                                    val alertDialog = AlertDialog.Builder(requireContext())
                                        .setTitle("Result")
                                        .setPositiveButton("OK") { _, _ -> }
                                        .create()

                                    alertDialog.setView(dialogView)
                                    alertDialog.show()
                                    hideProgress()
                                }
                            }
                            "Invalid Address" -> {
                                Log.e("MINT", "Address Error")
                                requireActivity().runOnUiThread {
                                    resultText.text = "Invalid address. Please try again."

                                    val alertDialog = AlertDialog.Builder(requireContext())
                                        .setTitle("Result")
                                        .setPositiveButton("OK") { _, _ -> }
                                        .create()

                                    alertDialog.setView(dialogView)
                                    alertDialog.show()
                                    hideProgress()
                                }
                            }
                            "Unknown Error" -> {
                                Log.e("MINT", "Unknown Error")
                                requireActivity().runOnUiThread {
                                    resultText.text = "Unknown Error."

                                    val alertDialog = AlertDialog.Builder(requireContext())
                                        .setTitle("Result")
                                        .setPositiveButton("OK") { _, _ -> }
                                        .create()

                                    alertDialog.setView(dialogView)
                                    alertDialog.show()
                                    hideProgress()
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            Log.d("PW", "Input cancelled")
            requireActivity().runOnUiThread {
                hideProgress()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefs = PreferenceUtil(requireContext())
        return inflater.inflate(R.layout.tab_mint_token_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val prodDatePickerBtn = requireView().findViewById<Button>(R.id.prod_date_btn)
        val expDatePickerBtn = requireView().findViewById<Button>(R.id.exp_date_btn)
        val mintBtn = requireView().findViewById<Button>(R.id.mint_btn)

        prodDatePickerBtn.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val dateString = "${year}-${month+1}-${dayOfMonth}"
                prodDatePickerBtn.text = dateString
            }, year, month, day).show()
        }

        expDatePickerBtn.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val dateString = "${year}-${month+1}-${dayOfMonth}"
                expDatePickerBtn.text = dateString
            }, year, month, day).show()
        }

        mintBtn.setOnClickListener {
            if (checkRequirements()) {
                val pwInputLauncher = passwordActivityLauncher
                val intent = Intent(requireContext(), PasswordInputActivity::class.java)
                progressDialog = AppCompatDialog(requireContext())

                showProgress(requireActivity())
                pwInputLauncher.launch(intent)
            }
            else {
                // Do not mint when requirements are false
                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_transfer_result, null)
                val resultText = dialogView.findViewById<TextView>(R.id.transfer_result_text)

                resultText.text = "You cannot mint with missing information."

                val alertDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setPositiveButton("OK") { _, _ -> }
                    .create()

                alertDialog.setView(dialogView)
                alertDialog.show()
            }
        }


    }

    private fun mintToken(password: String,
                          productName: String, productionDate: String,
                          expirationDate: String, details: String): MintTokenResult? {

        val server = RetrofitClass.getInstance()
        val requester = prefs.getString("account", "")
        val jwt = prefs.getString("jwt", null)

        return try {
            val response = server.mintToken(
                jwt, MintTokenBody(account = requester, pw = password,
                                    name = productName, prod_date = productionDate,
                                    exp_date = expirationDate, details = details)).execute()

            Log.d("MINT", response.toString())
            Log.d("MINT", response.raw().toString())
            when {
                response.code() == 200 -> {
                    response.body()
                }
                response.code() == 503 -> {
                    MintTokenResult(result = "failed", txhash = null, err = "Node Network Error")
                }
                response.code() == 401 -> {
                    MintTokenResult(result = "failed", txhash = null, err = "Authentication Error")
                }
                response.code() == 406 -> {
                    MintTokenResult(result = "failed", txhash = null, err = "Invalid Address")
                }
                else -> {
                    MintTokenResult(result = "failed", txhash = null, err = "Unknown Error")
                }
            }
        } catch (e: IOException) {
            MintTokenResult(result = "failed", txhash = null, err = "Network Error")
        } catch (e: NullPointerException) {
            MintTokenResult(result = "failed", txhash = null, err = "Invalid Request")
        }

    }

    private fun showProgress(activity: Activity) {
        if (activity.isFinishing) {
            return
        }

        if (!progressDialog.isShowing) {
            progressDialog.setCancelable(false)
            progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog.setContentView(R.layout.etc_loading_layout)
            progressDialog.show()
        }
    }

    private fun hideProgress() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    private fun clearInput() {
        val productNameInput = requireView().findViewById<EditText>(R.id.prod_name_input)
        val productionDateInput = requireView().findViewById<Button>(R.id.prod_date_btn)
        val expirationDateInput = requireView().findViewById<Button>(R.id.exp_date_btn)
        val detailsInput = requireView().findViewById<EditText>(R.id.details_input)

        productNameInput.text = null
        productionDateInput.text = "SELECT DATE"
        expirationDateInput.text = "SELECT DATE"
        detailsInput.text = null
    }

    private fun checkRequirements() : Boolean {
        val productNameInput = requireView().findViewById<EditText>(R.id.prod_name_input)
        val productionDateInput = requireView().findViewById<Button>(R.id.prod_date_btn)
        val expirationDateInput = requireView().findViewById<Button>(R.id.exp_date_btn)
        val detailsInput = requireView().findViewById<EditText>(R.id.details_input)

        return productNameInput.text != null &&
                (productNameInput.text.toString() != "") &&
                productionDateInput.text.toString() != "SELECT DATE" &&
                expirationDateInput.text.toString() != "SELECT DATE" &&
                detailsInput.text != null &&
                (detailsInput.text.toString() != "")
    }

}