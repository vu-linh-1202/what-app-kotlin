package com.nac.whatappkotlin.registerlogin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.nac.whatappkotlin.R
import com.nac.whatappkotlin.message.LatesMessageAct
import kotlinx.android.synthetic.main.activity_login.*

class LoginAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener {
            performLogin()
        }
        tv_back_to_register.setOnClickListener {
            finish()
        }
    }
    private fun performLogin() {

        val email = edt_email.text.toString()
        val password = edt_password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out email/password.", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                val intent = Intent(this, LatesMessageAct::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to login in: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}