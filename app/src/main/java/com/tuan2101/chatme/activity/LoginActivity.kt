package com.tuan2101.chatme.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.tuan2101.chatme.R
import com.tuan2101.chatme.databinding.ActivityLoginBinding
import com.tuan2101.chatme.viewModel.User

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    var currentUser : FirebaseUser? = null
    lateinit var binding: ActivityLoginBinding
    private var firebaseAuth: FirebaseAuth? = null

    lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.createAccount.setOnClickListener(this)
        binding.loginButton.setOnClickListener(this)

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth!!.currentUser

        binding.forgotPassword.setOnClickListener {
            resetPassword()
        }

    }

    override fun onStart() {
        super.onStart()
        if(currentUser != null) {
            navigateToMainActivity()
        }
    }

    fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    override fun onClick(v: View?) {
        if(v == binding.createAccount) {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
        else if (v == binding.loginButton) {
            allowUserLogin()
        }
    }

    private fun resetPassword() {
        if (binding.loginEmail.text.toString().isNotEmpty()) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(binding.loginEmail.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(this@LoginActivity, "We have sent the link to your email for password resetting", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this@LoginActivity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }else {
            Toast.makeText(this@LoginActivity, "Enter your email", Toast.LENGTH_SHORT).show()
        }
    }

    private fun allowUserLogin() {
        var loginEmail = binding.loginEmail.text.toString()
        val loginPassword = binding.loginPassword.text.toString()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Account is signing...")
        progressDialog.setCanceledOnTouchOutside(true)
        progressDialog.show()

        if (loginEmail.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Email can't be empty", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
        else if (loginPassword.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Password can't be empty", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
        else{
            firebaseAuth!!.signInWithEmailAndPassword(loginEmail, loginPassword)
                    .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                        override fun onComplete(p0: Task<AuthResult>) {
                            if(p0.isSuccessful) {
                                navigateToMainActivity()
                                Toast.makeText(this@LoginActivity, "Login Successfully", Toast.LENGTH_SHORT).show()
                                progressDialog.dismiss()
                            }
                            else {
                                Toast.makeText(this@LoginActivity, "Error: " + p0.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                                progressDialog.dismiss()
                            }
                        }

                    })
        }
    }
}