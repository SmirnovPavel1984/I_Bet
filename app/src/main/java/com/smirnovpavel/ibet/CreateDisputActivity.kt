package com.smirnovpavel.ibet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.smirnovpavel.ibet.data.Disput
import com.smirnovpavel.ibet.data.User
import com.smirnovpavel.ibet.database.DbManager
import com.smirnovpavel.ibet.databinding.ActivityCreateDisputBinding
import java.lang.System.currentTimeMillis

class CreateDisputActivity : AppCompatActivity() {

    lateinit var binding: ActivityCreateDisputBinding
    private val dbManager = DbManager(null, this)
    var currentUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateDisputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = intent.getSerializableExtra(MainActivity.PROFILE) as User


    }

    fun closeDisputCreate(view: View) {
        finish()
    }


    fun createDisput () : Disput {
        val disput: Disput
        var betType: String? = null

        //Log.d("IBETTEST_User", currentUser?.userEmail ?: "null")
        binding.apply {
            if (rbMoney.isChecked) {betType = "money"}
            else if (rbRatio.isChecked) {betType = "ratio"}

            val name: String?
            if (!currentUser?.userName?.isEmpty()!!) {
                name = currentUser?.userName
            } else {
                name = currentUser?.userEmail
            }

            //Log.d("IBETTEST_User", name ?: "null")
            disput = Disput(
                dbManager.db.push().key,
                etDisputTheme.text.toString(),
                etDisputText.text.toString(),
                currentUser?.userID,
                name,
                null,
                null,
                betType,
                etBet.text.toString().toInt(),
                null,
                null,
                currentTimeMillis(),
                "activ"
            )
        }
        return disput
    }

    fun onClickPublish(view: View) {
        val condition: Boolean = (binding.etDisputTheme.text != null
                && binding.etDisputText.text != null
                && binding.etBet.text != null
                && binding.rbRatio.isChecked)
        if (binding.rbMoney.isChecked) {
            Toast.makeText(this, "Увы, спор на деньги пока не доступен", Toast.LENGTH_LONG).show()
            return
        }

        if (condition) {

            if (currentUser?.userRatio!! >= binding.etBet.text.toString().toInt()) {
                Toast.makeText(this, "Спор успешно создан.\nОбновите ленту, ударив в гонг!", Toast.LENGTH_LONG).show()
                dbManager.publishDisput(createDisput())
                changeUserDataInCreate(binding.etBet.text.toString().toInt())
                finish()
            } else {
                Toast.makeText(this, "Вы не можете ставить больше, чем у вас есть...", Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_LONG).show()
        }
    }

    fun changeUserDataInCreate (bet: Int) {
        var newRatio: Int? = (currentUser?.userRatio)?.minus(bet)
        if (newRatio==0) newRatio=1
        val newCount = (currentUser?.userDisputCount ?: 0) +1
        val newActivCount = (currentUser?.userDisputActive ?: 0) +1


        val tempUser = User(
            currentUser?.userName,
            currentUser?.userID,
            currentUser?.userEmail,
            newCount,
            currentUser?.userDisputWin,
            currentUser?.userDisputLose,
            newActivCount,
            newRatio,
            currentUser?.userMoney
        )
        dbManager.putUserToDatabase(tempUser)
        finish()
    }

}