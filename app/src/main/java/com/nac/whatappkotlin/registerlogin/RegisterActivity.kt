package com.nac.whatappkotlin.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.nac.whatappkotlin.R
import com.nac.whatappkotlin.message.LatesMessageAct
import com.nac.whatappkotlin.model.User
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    companion object{
        val TAG="RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_signIn.setOnClickListener {
            performRegister()
        }
        tv_already_account.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login activity")

            //launch the login activity somehow
            val intent = Intent(this, LoginAct::class.java)
            startActivity(intent)
        }

        btn_select_photo.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterLogin", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            circleImageView.setImageBitmap(bitmap)
            btn_select_photo.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            btn_select_photo.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister() {
        val email = edt_email.text.toString()
        val password = edt_password.text.toString()

        //yêu cầu nhập email & password của bạn để đăng nhập
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_LONG).show()
            return
        }

        Log.d(TAG,"Attempting to create user with email:$email")

        //Firebase Authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                //else if successful
                Log.d(TAG, "Successful create user with uid: ${it.result?.user?.uid}")

                upLoadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun upLoadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/image/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully upload image: ${it.metadata?.path}")

                //get link image
                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to uplaod image to storage:${it.message}")
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/user/$uid")

        val users = User(uid, edt_name.text.toString(), profileImageUrl)
        ref.setValue(users)
            .addOnSuccessListener {
                Log.d(TAG, " Finally we saved the user to Firebase Database")

                val intent = Intent(this, LatesMessageAct::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d(TAG, "Failed to set value to database: ${it.message}")
            }
    }
}