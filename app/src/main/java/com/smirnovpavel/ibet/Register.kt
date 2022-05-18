package com.smirnovpavel.ibet

import android.app.AlertDialog
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.smirnovpavel.ibet.databinding.DialogForgetBinding


class Register (activ: StartSignActivity){
    private val activ = activ


    fun signUpWithEmail (email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            activ.authEmail.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                    task ->
                if (task.isSuccessful) {
                    sendEmailVerification(task.result?.user!!)


                } else {
                    Toast.makeText(activ, activ.resources.getString(R.string.register_error), Toast.LENGTH_LONG).show()
                }

            }
        }
    }



    private fun sendEmailVerification (user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener{task ->
            if (task.isSuccessful) {
                Toast.makeText(activ, activ.resources.getString(R.string.send_email), Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(activ, activ.resources.getString(R.string.send_email_error), Toast.LENGTH_LONG).show()
            }
        }
    }


    fun logInWithEmail (email: String, password: String){
        if (email.isNotEmpty() && password.isNotEmpty()) {
            activ.authEmail.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    task ->
                if (!task.isSuccessful) {
                    Toast.makeText(activ, activ.resources.getString(R.string.login_error), Toast.LENGTH_LONG).show()
                } else {
                    val i = Intent (activ, MainActivity::class.java)
                    activ.startActivity(i)
                    activ.finish()
                }
            }
        }
    }

    fun passwordRecovery(bindingDial: DialogForgetBinding, dialog: AlertDialog?) {
        if (bindingDial.etEmailForget.text.isNotEmpty()) {
            activ.authEmail.sendPasswordResetEmail(bindingDial.etEmailForget.text.toString().trim()).addOnCompleteListener{
                task->
                if (task.isSuccessful) {
                    Toast.makeText(activ, activ.resources.getString(R.string.send_password), Toast.LENGTH_LONG).show()
                }
            }
            dialog?.dismiss()
        } else {

        }
    }


}