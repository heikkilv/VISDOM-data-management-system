package visdom.utils

import java.math.BigInteger
import java.security.MessageDigest


object GeneralUtils {
    def toInt(stringValue: String): Option[Int] = {
        try {
            Some(stringValue.toInt)
        } catch {
            case _: java.lang.NumberFormatException => None
        }
    }

    val ShaFunction: String = "SHA-512/256"
    val Encoding: String = "UTF-8"
    val MessageDigester: MessageDigest = MessageDigest.getInstance(ShaFunction)

    def getHash(inputString: String): String = {
        val digest = MessageDigester.digest(inputString.getBytes(Encoding))
        String.format(s"%0${digest.length * 2}x", new BigInteger(1, digest))
    }

    def getHash(inputString: String, useHash: Boolean): String = {
        useHash match {
            case true => getHash(inputString)
            case false => inputString
        }
    }

    def getUpperFolder(path: String): String = {
        path.contains(CommonConstants.Slash) match {
            case true => {
                val pathParts: Array[String] = path.split(CommonConstants.Slash)
                pathParts.take(pathParts.size - 1).mkString(CommonConstants.Slash)
            }
            case false => CommonConstants.EmptyString
        }
    }
}
