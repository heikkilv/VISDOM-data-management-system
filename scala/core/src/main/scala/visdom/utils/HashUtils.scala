package visdom.utils

import java.math.BigInteger
import java.security.MessageDigest


object HashUtils {
    val ShaFunction: String = "SHA-512/256"
    val Encoding: String = "UTF-8"
    val MessageDigester: MessageDigest = MessageDigest.getInstance(ShaFunction)
    val secretWord: String = EnvironmentVariables.getEnvironmentVariable(EnvironmentVariables.EnvironmentSecretWord)

    def getHash(inputString: String): String = {
        val digest = MessageDigester.digest((secretWord + inputString).getBytes(Encoding))
        String.format(s"%0${digest.length * 2}x", new BigInteger(1, digest))
    }

    def getHash(inputNumber: Int): Int = {
        getHash(inputNumber.toString()).hashCode()
    }

    def getHash(inputString: String, useHash: Boolean): String = {
        useHash match {
            case true => getHash(inputString)
            case false => inputString
        }
    }

    def getHash(inputNumber: Int, useHash: Boolean): Int = {
        getHash(inputNumber.toString(), useHash).hashCode()
    }
}
