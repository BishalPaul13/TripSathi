package com.example.tripsathi

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.zxing.integration.android.IntentIntegrator

class QRScannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan a QR Code")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {

                val intent = Intent(this, QRResultActivity::class.java)
                intent.putExtra("data", result.contents)
                startActivity(intent)

                finish()

            } else {
                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}