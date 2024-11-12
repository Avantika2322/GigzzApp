package com.gigzz.android.data.socket

import android.util.Log
import com.gigzz.android.domain.res.GetChatMessage
import com.gigzz.android.utils.AppConstants
import com.gigzz.android.utils.getTimeOffset

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.json.JSONObject
import io.socket.client.Socket
import io.socket.client.IO
import java.net.URISyntaxException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Module
@InstallIn(SingletonComponent::class)
object SocketHelper{
    private lateinit var mSocket: Socket
    private var eventListener: SocketEventListener? = null
    var chatId: Int? = null
    var chatType: Int? = null
    var myUserId: Int? = null  //my id
    var otherUserId: Int? = null //other Id

    fun initEventListener(listener: SocketEventListener?){
        eventListener = listener
    }


    fun init() {
        try {
            val options = IO.Options()
            options.forceNew = true
            options.reconnection = true //reconnection
            //options.timeout = 20000

            //creating socket instance
            mSocket = IO.socket(AppConstants.SOCKET_URL, options)

        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }



    fun turnOnSocket() {
        connectSocket()
        mSocket.on("receive_message"){ args ->
            //{"chat_id":44,"message":"Bello","sender_id":"390","receiver_id":"391","message_send_by":"390","created_at":"2023-10-23 09:51:58"}
            val data = args[0] as JSONObject
            val tt = addTime(data["created_at"] as String)
            val getChatMessage = GetChatMessage(
                chatId = data["chat_id"] as Int,
                createdAt = tt /*data["created_at"] as String*/,
                mediaUrl = "",
                message = data["message"] as String,
                messageId = 0,
                messageSendBy = data["message_send_by"] as String,
                msgType = "",
                receiverId = data["receiver_id"] as String,
                receiverReadFlag = 0,
                senderId = data["sender_id"] as String,
                senderReadFlag = 0,
                profileImageUrl = "",
                postedBy = ""
            )
            eventListener?.getMessageData(getChatMessage)

        }



        mSocket.on(Socket.EVENT_CONNECT){
            Log.d("socket", "joined")
            chatId?.let { it1 -> joinSocket(it1) }
        }
        mSocket.on(Socket.EVENT_DISCONNECT){
            Log.d("socket", "Disconnected")
        }

        mSocket.on(Socket.EVENT_CONNECT_ERROR){
            Log.d("socket", "Connect error")
            connectSocket()
        }
    }

    private fun connectSocket(){
        if(!mSocket.connected()) {
            mSocket.connect()
        }
    }

    fun joinSocket(chatId: Int){
        val jsonObject = JSONObject()
        jsonObject.put("chat_id", chatId)
        mSocket.emit(AppConstants.JOIN_SOCKET, jsonObject)
    }

    fun sentMessageEvent(message: String) {
        val sendJsonObject = JSONObject()
        sendJsonObject.put("chat_id", chatId)
        sendJsonObject.put("sender_id", myUserId.toString())
        sendJsonObject.put("receiver_id", otherUserId.toString())
        sendJsonObject.put("message", message)
        sendJsonObject.put("chat_type", chatType)
        sendJsonObject.put("created_at", getTimeOffset())
        if (mSocket.isActive)
            mSocket.emit("send_message", sendJsonObject)
    }

    fun blockUserEvent(){
        val blockJsonObject = JSONObject()
        blockJsonObject.put("chat_id", chatId)
        blockJsonObject.put("user_id", myUserId.toString())
        blockJsonObject.put("other_user_id", otherUserId.toString())
        mSocket.emit("block-user", blockJsonObject)
    }

    fun addTime(time: String): String {
        // Parse the input date and time
        val inputDateTimeString = time
        val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
        val inputDateTime = LocalDateTime.parse(inputDateTimeString, formatter)

        // Add 5 hours and 30 minutes
        val resultDateTime = inputDateTime.plusHours(5).plusMinutes(30)

        // Format the result
        val resultDateTimeString = resultDateTime.format(formatter)
        println("Original DateTime: $inputDateTimeString")
        println("Result DateTime: $resultDateTimeString")
        return resultDateTimeString
    }

    fun turnOffSocket(){
        mSocket.off()
        chatId = 0
    }

    fun disconnectSocket(){
        mSocket.disconnect()
        chatId = 0
    }

}