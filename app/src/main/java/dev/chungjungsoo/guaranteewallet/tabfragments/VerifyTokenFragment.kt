package dev.chungjungsoo.guaranteewallet.tabfragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.*
import dev.chungjungsoo.guaranteewallet.R
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import dev.chungjungsoo.guaranteewallet.databinding.ActivityMainBinding as ActivityMainBinding1


class VerifyTokenFragment : Fragment() {
    lateinit var barcodeView : DecoratedBarcodeView
    lateinit var tokenInfo : Claims

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_verify_token_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeView = requireView().findViewById(R.id.barcode_scanner)
        barcodeView.decodeContinuous(callback)
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    private val callback = BarcodeCallback { result ->
        var invalid = false
        if (result.text != null) {
            barcodeView.pause()
            val resultString = result.text
            tokenInfo = try {
                Jwts.parserBuilder().build().parseClaimsJws(resultString).body
            } catch (e: io.jsonwebtoken.ExpiredJwtException) {
                Log.d("JWT", "Token Expired")
                invalid = true
                e.claims
            }


            val tid = tokenInfo.getValue("tid") as Int
            val owner = tokenInfo.getValue("owner") as String
            val exp = (tokenInfo.getValue("exp") as Int).toString()

            Toast.makeText(requireContext(), "$tid / $owner / $exp", Toast.LENGTH_SHORT).show()
        }
    }
}