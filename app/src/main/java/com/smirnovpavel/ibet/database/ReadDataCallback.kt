package com.smirnovpavel.ibet.database

import com.smirnovpavel.ibet.data.Disput
import com.smirnovpavel.ibet.data.User

interface ReadDataCallback {
    fun readData (list: List <Disput>)
    fun readUser (user: User?)
}