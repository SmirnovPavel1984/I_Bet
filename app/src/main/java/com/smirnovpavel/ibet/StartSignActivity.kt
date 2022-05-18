package com.smirnovpavel.ibet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.smirnovpavel.ibet.data.User
import com.smirnovpavel.ibet.database.DbManager
import com.smirnovpavel.ibet.dialogs.DialogForget
import com.smirnovpavel.ibet.dialogs.DialogReg
import com.smirnovpavel.ibet.databinding.ActivityStartSignBinding

class StartSignActivity : AppCompatActivity() {

    private lateinit var binding : ActivityStartSignBinding
    private val dialogReg = DialogReg(this)
    private val dialogForget = DialogForget (this)

    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth
    var authEmail = FirebaseAuth.getInstance()
    private val register = Register(this)

    private val dbManager = DbManager(null, this)


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityStartSignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        auth.currentUser

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account!=null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                //Log.d ("I_BET_MESSAGE", "Api Exception")
            }
        }

        binding.buttonSignGoogle.setOnClickListener {
            logInWithGoogle()

        }

        checkAuth()

        binding.tvRegisterLink.setOnClickListener {
            dialogReg.createRegDial()
        }

//----------------------------------------------------------------TEST!!!----------------------------
        binding.buttonExit.setOnClickListener{
            authEmail.signOut()
        }

        binding.buttonLogin.setOnClickListener{
            register.logInWithEmail(binding.etEmail.text.toString().trim(),
                binding.etPassword.text.toString())
        }

        binding.buttonForget.setOnClickListener{
            dialogForget.createForgetDial()
        }

    }

    private fun getClient(): GoogleSignInClient {
        // Configure Google Sign In
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) //Это какой-то баг. Всё исправно работает!
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }

    private fun logInWithGoogle () {
        val signInClient = getClient()
        launcher.launch(signInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle (idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                //Log.d ("I_BET_MESSAGE", "Successful login via Google account")
                checkAuth()
                finish()
            } else {
                Toast.makeText(this, this.resources.getString(R.string.login_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkAuth () {
        if (auth.currentUser!=null) {
            val i = Intent (this, MainActivity::class.java)
            startActivity(i)
        }
    }



}