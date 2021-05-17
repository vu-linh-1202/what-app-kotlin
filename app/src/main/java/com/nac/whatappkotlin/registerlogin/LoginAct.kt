package com.nac.whatappkotlin.registerlogin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.nac.whatappkotlin.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener {
            val email = edt_email.text.toString()
            val password = edt_password.text.toString()

            Log.d("Login", "Attemt login with email/pass: $email/****")

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {  }

        }
        tv_back_to_register.setOnClickListener {
            finish()
        }
    }
}