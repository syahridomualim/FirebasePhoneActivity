package com.example.firebasephoneactivity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasephoneactivity.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private var verificationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        val phoneNumber = binding.inputEdtPhoneNumber.text.toString()
        val otpCode = binding.inputEdtCodeOtp.text.toString()
        binding.buttonSendSms.setOnClickListener {
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Please fill phone number", Toast.LENGTH_SHORT).show()
            } else {
                sendVerificationCode(phoneNumber)
            }
        }

        binding.verifyOtp.setOnClickListener {
            if (TextUtils.isEmpty(otpCode)) {
                Toast.makeText(this, "Please fill the otp code", Toast.LENGTH_SHORT).show()
            } else {
                verifyCode(otpCode)
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            val code = credential.smsCode
            if (code != null) {
                verifyCode(code)
            }
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            Toast.makeText(this@MainActivity, "Failed to verify", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(s: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(s, token)
            verificationId = s
        }
    }

    private fun sendVerificationCode(
        phoneNumber: String
    ) {

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInByCredential(credential)
    }

    private fun signInByCredential(credential: PhoneAuthCredential) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Login successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}