package com.xaluoqone.test

import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object Sha256Util {
    fun encryption(str: String): String {
        return encryption(str.toByteArray(StandardCharsets.UTF_8))
    }

    private fun encryption(buf: ByteArray?): String {
        val messageDigest: MessageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(buf)
        return messageDigest.digest().toHex()
    }

    fun sha256HMAC(content: String, secret: String?): String {
        var sha256HMAC: Mac? = null
        try {
            sha256HMAC = Mac.getInstance("HmacSHA256")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        val secretKey: SecretKey = SecretKeySpec(secret!!.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        try {
            sha256HMAC!!.init(secretKey)
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        val digest = sha256HMAC!!.doFinal(content.toByteArray(StandardCharsets.UTF_8))
        return digest.toHex().uppercase(Locale.getDefault())
    }
}

fun ByteArray.toHex(): String = joinToString("") { eachByte -> "%02x".format(eachByte) }