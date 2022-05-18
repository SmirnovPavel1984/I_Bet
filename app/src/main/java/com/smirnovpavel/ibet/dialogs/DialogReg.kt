package com.smirnovpavel.ibet.dialogs

import android.app.AlertDialog
import com.smirnovpavel.ibet.Register
import com.smirnovpavel.ibet.StartSignActivity
import com.smirnovpavel.ibet.databinding.DialogRegisterBinding

class DialogReg (activ: StartSignActivity) {
    private val activ = activ
    private val register = Register(activ)


    fun createRegDial () {
        val builder = AlertDialog.Builder(activ)
        val bindingDial = DialogRegisterBinding.inflate(activ.layoutInflater)
        builder.setView(bindingDial.root)

        val dialog = builder.create()

        bindingDial.buttonRegister.setOnClickListener() {
            register.signUpWithEmail(bindingDial.etLogin.text.toString().trim(), bindingDial.etPassword.text.toString())
            dialog.dismiss()
        }
        dialog.show()
    }
}