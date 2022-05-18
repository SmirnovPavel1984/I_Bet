package com.smirnovpavel.ibet.data

import java.io.Serializable

data class User (
    var userName: String? = null,
    var userID: String? = null,
    var userEmail: String? = null,
    var userDisputCount: Int? = 0,
    var userDisputWin: Int? = 0,
    var userDisputLose: Int? = 0,
    var userDisputActive: Int? = 0,
    var userRatio: Int? = 100,
    var userMoney: Int? = 0
) : Serializable