package com.example.retailershop

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), PaymentResultListener {

    // on below line we are creating
    // variables for our edit text and button
    lateinit var amtEdt: EditText
    lateinit var payBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // on below line we are initializing
        // our variable with their ids.
        amtEdt = findViewById(R.id.idEdtAmt)
        payBtn = findViewById(R.id.idBtnMakePaymanet)

        // on below line adding click listener for pay button
        payBtn.setOnClickListener {

            // on below line getting amount from edit text
            val amt = amtEdt.text.toString()

            // rounding off the amount.
            val amount = Math.round(amt.toFloat() * 100).toInt()

            // on below line we are
            // initializing razorpay account
            val checkout = Checkout()

            // on the below line we have to see our id.
            checkout.setKeyID("Enter your key here")

            // set image
            checkout.setImage(R.drawable.android)

            // initialize json object
            val obj = JSONObject()
            try {
                // to put name
                obj.put("name", "Geeks for Geeks")

                // put description
                obj.put("description", "Test payment")

                // to set theme color
                obj.put("theme.color", "")

                // put the currency
                obj.put("currency", "INR")

                // put amount
                obj.put("amount", amount)

                // put mobile number
                obj.put("prefill.contact", "9284064503")

                // put email
                obj.put("prefill.email", "chaitanyamunje@gmail.com")

                // open razorpay to checkout activity
                checkout.open(this@MainActivity, obj)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPaymentSuccess(s: String?) {
        // this method is called on payment success.
        Toast.makeText(this, "Payment is successful : " + s, Toast.LENGTH_SHORT).show();
    }

    override fun onPaymentError(p0: Int, s: String?) {
        // on payment failed.
        Toast.makeText(this, "Payment Failed due to error : " + s, Toast.LENGTH_SHORT).show();
    }
}
