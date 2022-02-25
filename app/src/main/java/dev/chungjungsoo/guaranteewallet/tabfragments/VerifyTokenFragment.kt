package dev.chungjungsoo.guaranteewallet.tabfragments

import android.content.Intent
import android.os.Bundle
import java.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import dev.chungjungsoo.guaranteewallet.R
import dev.chungjungsoo.guaranteewallet.activities.VerificationResultActivity
import dev.chungjungsoo.guaranteewallet.dataclass.QRToken
import dev.chungjungsoo.guaranteewallet.preference.PreferenceUtil
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import java.lang.Exception
import java.security.Key



class VerifyTokenFragment : Fragment() {
    lateinit var barcodeView: DecoratedBarcodeView
    lateinit var tokenInfo: Claims
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_verify_token_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferenceUtil(requireContext())

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
        val key = Base64.getDecoder().decode(prefs.getString("key", ""))
        if (result.text != null) {
            barcodeView.pause()
            val resultString = result.text
            tokenInfo = try {
                Jwts.parserBuilder().setSigningKeyResolver(signingKeyResolver).build().parseClaimsJws(resultString).body
            } catch (e: io.jsonwebtoken.ExpiredJwtException) {
                Log.d("JWT", "Token Expired")
                invalid = true
                e.claims
            } catch (e: io.jsonwebtoken.UnsupportedJwtException) {
                Log.e("JWT", "Token not supported")
//                invalid = true
                val splitted = resultString.split(".")[1]
                val decoded = Base64.getDecoder().decode(splitted).decodeToString()
                val jsonObject = JsonParser().parse(decoded).asJsonObject
                val qrObject: QRToken = Gson().fromJson(jsonObject, QRToken::class.java)
                val c = Jwts.claims()
                c["tid"] = qrObject.tid
                c["owner"] = qrObject.owner
                c["exp"] = qrObject.exp
                c
            }

            val tid = tokenInfo.getValue("tid") as Int
            val owner = tokenInfo.getValue("owner") as String
            val exp = (tokenInfo.getValue("exp") as Int).toString()

            val intent = Intent(activity, VerificationResultActivity::class.java)

            intent.putExtra("tid", tid)
            intent.putExtra("owner", owner)
            intent.putExtra("exp", invalid)

            startActivity(intent)
        }
    }

    private val signingKeyResolver =  object : SigningKeyResolverAdapter() {
        override fun resolveSigningKey(header: JwsHeader<*>?, claims: Claims?): Key? {
            val kid = header?.keyId
            return null
        }
    }
}