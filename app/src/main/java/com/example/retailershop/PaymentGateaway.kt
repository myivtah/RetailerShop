package com.example.retailershop

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.models.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder

class MainActivity : AppCompatActivity(), TransactionFinishedCallback {

    // Deklarasi variabel untuk EditText dan Button
    lateinit var amtEdt: EditText
    lateinit var payBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi variabel
        amtEdt = findViewById(R.id.idEdtAmt)
        payBtn = findViewById(R.id.idBtnMakePayment)

        // Inisialisasi SDK Midtrans
        initMidtransSdk()

        // Tombol pembayaran
        payBtn.setOnClickListener {
            val amount = amtEdt.text.toString()

            // Validasi input
            if (amount.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Memulai pembayaran
            startPayment(amount.toDouble())
        }
    }

    private fun initMidtransSdk() {
        SdkUIFlowBuilder.init()
            .setContext(this)
            .setClientKey("YOUR_CLIENT_KEY") // Ganti dengan Client Key Anda
            .setMerchantBaseUrl("https://YOUR_SERVER_URL/") // URL server Anda
            .enableLog(true)
            .buildSDK()
    }

    private fun startPayment(amount: Double) {
        // Konfigurasi transaksi Midtrans
        MidtransSDK.getInstance().transactionRequest = MidtransSDK.getInstance()
            .transactionRequest.apply {
                transactionAmount = amount
                customerDetails = com.midtrans.sdk.corekit.models.CustomerDetails(
                    "test@domain.com",
                    "Customer Name",
                    "08123456789",
                    "test@domain.com"
                )
            }

        // Memulai UI Flow Midtrans
        MidtransSDK.getInstance().startPaymentUiFlow(this)
    }

    override fun onTransactionFinished(result: TransactionResult?) {
        // Callback hasil transaksi
        if (result != null) {
            when {
                result.status == TransactionResult.STATUS_SUCCESS -> {
                    Toast.makeText(this, "Transaction Successful", Toast.LENGTH_SHORT).show()
                }
                result.status == TransactionResult.STATUS_PENDING -> {
                    Toast.makeText(this, "Transaction Pending", Toast.LENGTH_SHORT).show()
                }
                result.status == TransactionResult.STATUS_FAILED -> {
                    Toast.makeText(this, "Transaction Failed", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Transaction Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Transaction Error", Toast.LENGTH_SHORT).show()
        }
    }
}
