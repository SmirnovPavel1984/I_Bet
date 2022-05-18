package com.smirnovpavel.ibet.dialogs

import android.app.AlertDialog
import com.smirnovpavel.ibet.Register
import com.smirnovpavel.ibet.StartSignActivity
import com.smirnovpavel.ibet.databinding.DialogForgetBinding

class DialogForget (activ: StartSignActivity) {
    private val activ = activ
    private val register = Register(activ)


    fun createForgetDial () {
        val builder = AlertDialog.Builder(activ)
        val bindingDial = DialogForgetBinding.inflate(activ.layoutInflater)
        builder.setView(bindingDial.root)

        val dialog = builder.create()

        bindingDial.buttonRemind.setOnClickListener() {
            register.passwordRecovery(bindingDial, dialog)
            dialog.dismiss()
        }
        dialog.show()
    }
}