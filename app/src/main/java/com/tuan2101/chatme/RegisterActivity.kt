package com.tuan2101.chatme

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tuan2101.chatme.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var progressDialog: ProgressDialog
    lateinit var databaseReference: DatabaseReference
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference


        binding.registerButton.setOnClickListener(this)

        binding.hadAccount.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if (v == binding.hadAccount) {
            navigateToMainActivity()
        }
        else if(v == binding.registerButton) {
            createNewAccount()
        }
    }

    fun navigateToMainActivity() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun createNewAccount() {
        var email = binding.registerEmail.text.toString()
        var password = binding.registerPassword.text.toString()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Account is creating...")
        progressDialog.setCanceledOnTouchOutside(true)
        progressDialog.show()

        if (email.isEmpty())
        {
            Toast.makeText(this@RegisterActivity,"Email cannot be empty",Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
        else if (password.isEmpty())
        {
            Toast.makeText(this@RegisterActivity,"Password cannot be empty",Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
        else {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(object: OnCompleteListener<AuthResult> {
                        override fun onComplete(p0: Task<AuthResult>) {
                            if(p0.isSuccessful) {
                                var currentUId = firebaseAuth.currentUser.uid
//                                databaseReference.child("User").child(currentUId).child("name").setValue(binding.registerUserName.text)
//                                databaseReference.child("User").child(currentUId).child("uid").setValue(currentUId)
//                                databaseReference.child("User").child(currentUId).child("status").setValue("off")
//                                databaseReference.child("User").child(currentUId).child("avatar").setValue("")
//                                databaseReference.child("User").child(currentUId).child("search").setValue(binding.registerUserName.text.toString().toLowerCase())


                                val detailsUser = HashMap<String, Any>()
                                detailsUser["name"] = binding.registerUserName.text.toString()
                                detailsUser["iud"] = currentUId
                                detailsUser["status"] = "offline"
                                detailsUser["avatar"] = "https://firebasestorage.googleapis.com/v0/b/chat-app-1ef01.appspot.com/o/2020-04-19.png?alt=media&token=74a156c4-fbbe-43c6-80f8-e2f8cfd0ec5d"
                                detailsUser["search"] = binding.registerUserName.text.toString().toLowerCase()

                                databaseReference.child("User").child(currentUId).updateChildren(detailsUser)
                                    .addOnCompleteListener {task ->
                                        if (task.isSuccessful) {
                                            navigateToMainActivity()
                                            Toast.makeText(this@RegisterActivity, " Account create successfully", Toast.LENGTH_SHORT).show()
                                            progressDialog.dismiss()
                                        }
                                    }
                            }
                            else {
                                Toast.makeText(this@RegisterActivity,"Error: " + p0.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                                progressDialog.dismiss()
                            }
                        }

                    })
        }

    }
}
