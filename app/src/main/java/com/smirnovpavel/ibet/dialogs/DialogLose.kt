package com.smirnovpavel.ibet.dialogs

import android.app.AlertDialog
import com.smirnovpavel.ibet.DisputActivity
import com.smirnovpavel.ibet.databinding.DialogLoseBinding

class DialogLose (activ: DisputActivity){
    private val activ = activ

    fun createLoseDial () {
        val builder = AlertDialog.Builder(activ)
        val bindingDial = DialogLoseBinding.inflate(activ.layoutInflater)
        builder.setView(bindingDial.root)

        val dialog = builder.create()

        bindingDial.buttonOK.setOnClickListener() {
            activ.finish()
            dialog.dismiss()
        }
        dialog.show()
    }
}