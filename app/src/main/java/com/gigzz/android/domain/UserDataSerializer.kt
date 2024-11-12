package com.gigzz.android.domain

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.gigzz.android.domain.res.SignInData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object UserDataSerializer : Serializer<SignInData> {

    override val defaultValue: SignInData
        get() = SignInData.default

    override suspend fun readFrom(input: InputStream): SignInData {
        try {
            return Json.decodeFromString(
                deserializer = SignInData.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Error occurred during decoding the storage", exception)
        }
    }

    override suspend fun writeTo(t: SignInData, output: OutputStream) =
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(serializer = SignInData.serializer(), value = t).encodeToByteArray()
            )
        }
}