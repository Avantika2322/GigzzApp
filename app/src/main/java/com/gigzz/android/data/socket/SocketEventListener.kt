package com.gigzz.android.data.socket

import com.gigzz.android.domain.res.GetChatMessage

interface SocketEventListener {
    fun getMessageData(message: GetChatMessage)

}