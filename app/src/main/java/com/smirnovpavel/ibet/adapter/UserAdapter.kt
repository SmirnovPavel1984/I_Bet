package com.smirnovpavel.ibet.adapter

import com.smirnovpavel.ibet.data.User

class UserAdapter {
    var currentUser = User()
    var opponent = User()

    fun updateAdapter (user: User?)  {
        if (user != null)
            currentUser = user

        //Log.d("finish_try", currentUser.userRatio.toString() ?: "null")

    }

    fun updateAdapterToFindOpponent (user: User?) {
        if (user != null )
            opponent = user
        //Log.d("Testing", "параметры user внутри адаптера = ${user!!.userEmail} ${user.userID}")
        //Log.d("Testing", "параметры opponent внутри адаптера = ${opponent.userEmail} ${opponent.userID}")
    }


}