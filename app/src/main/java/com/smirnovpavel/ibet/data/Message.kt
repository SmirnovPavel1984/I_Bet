package com.smirnovpavel.ibet.data

import java.io.Serializable

data class Message(
    val messageAuthor: String? = null,
    val messageText: String? = null
) : Serializable
