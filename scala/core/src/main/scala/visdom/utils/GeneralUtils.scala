package visdom.utils

import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import scala.reflect.runtime.universe.termNames
import scala.reflect.runtime.universe.weakTypeOf
import scala.reflect.runtime.universe.TypeTag
import visdom.http.server.ServerConstants


object GeneralUtils {
    final val ZeroHourString: String = "T00:00Z"

    def toInt(stringValue: String): Option[Int] = {
        try {
            Some(stringValue.toInt)
        } catch {
            case _: java.lang.NumberFormatException => None
        }
    }

    def toIntWithinInterval(stringValue: String, minValue: Option[Int], maxValue: Option[Int]): Option[Int] = {
        val intValueOption: Option[Int] = toInt(stringValue)

        val insideInterval: Boolean = intValueOption match {
            case Some(intValue: Int) => (
                (minValue.isEmpty || intValue >= minValue.getOrElse(0)) &&
                (maxValue.isEmpty || intValue <= maxValue.getOrElse(0))
            )
            case None => false
        }

        insideInterval match {
            case true => intValueOption
            case false => None
        }
    }

    def toIntOption(value: Any): Option[Int] = {
        value match {
            case intValue: Int => Some(intValue)
            case numberValue: Number => Some(numberValue.intValue())
            case stringValue: String => toInt(stringValue)
            case Some(someValue: Any) => toIntOption(someValue)
            case _ => None
        }
    }

    def toStringOption(value: Any): Option[String] = {
        value match {
            case stringValue: String => Some(stringValue)
            case numberValue: Number => Some(numberValue.toString())
            case instantValue: Instant => Some(getMillisString(instantValue))
            case zonedDateTimeValue: ZonedDateTime => Some(zonedDateTimeValue.toString())
            case Some(someValue: Any) => toStringOption(someValue)
            case _ => None
        }
    }

    def toStringOption(value: Any, transformInstant: Boolean): Option[String] = {
        value match {
            case stringValue: String => {
                (transformInstant && stringValue.contains(CommonConstants.Date)) match {
                    case true => {
                        stringValue.split(CommonConstants.WhiteSpace).lastOption match {
                            case Some(stringPart: String) =>
                                Some(
                                    getMillisString(
                                        Instant.ofEpochMilli(stringPart.substring(0, stringPart.size - 1).toLong)
                                    )
                                )
                            case None => Some(stringValue)
                        }
                    }
                    case false => Some(stringValue)
                }
            }
            case Some(someValue: Any) => toStringOption(someValue, transformInstant)
            case _ => toStringOption(value)
        }
    }

    def toBooleanOption(value: Any): Option[Boolean] = {
        value match {
            case booleanValue: Boolean => Some(booleanValue)
            case stringValue: String => Some(stringValue.toBoolean)
            case Some(someValue: Any) => toBooleanOption(someValue)
            case _ => None
        }
    }

    def toInstantOption(value: Any): Option[Instant] = {
        value match {
            case instantValue: Instant => Some(instantValue)
            case zonedDateTimeValue: ZonedDateTime => Some(zonedDateTimeValue.toInstant())
            case stringValue: String => toInstantOption(toZonedDateTime(Some(stringValue)))
            case Some(someValue: Any) => toInstantOption(someValue)
            case _ => None
        }
    }

    def getMillisString(timeInstant: Instant): String = {
        // Returns the given time as ISO 8601 formatted string in millisecond precision in UTC time zone.
        val timeInMillis: Instant = timeInstant.truncatedTo(ChronoUnit.MILLIS)
        val timeInMillisString: String = timeInMillis.toString()
        timeInMillis.getNano() match {
            case 0 => {
                // When converting to String, Instant class leaves the milliseconds out when they are 0
                timeInMillisString.lastOption match {
                    case Some(timeZoneChar: Char) => (
                        timeInMillisString.dropRight(1) +
                        CommonConstants.Dot +
                        CommonConstants.ZeroChar.toString() * 3 +
                        timeZoneChar.toString()
                    )
                    case None => timeInMillisString  // this should never be reached
                }
            }
            case _ => timeInMillisString
        }
    }

    def getCurrentTimeString(): String = {
        // Returns the current time as ISO 8601 formatted string in millisecond precision in UTC time zone.
        getMillisString(Instant.now())
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toStringSeqOption(value: Any): Option[Seq[String]] = {
        toSeqOption(value, toStringOption(_))
    }

    def toSeqOption[T](value: Any, transformation: (Any) => Option[T]): Option[Seq[T]] = {
        value match {
            case sequence: Seq[_] => Some(sequence.map(singleValue => transformation(singleValue)).flatten)
            case array: Array[_] => toSeqOption(array.toSeq, transformation)
            case Some(someValue: Any) => toSeqOption(someValue, transformation)
            case _ => None
        }
    }

    def isPositiveInteger(integerCandidateString: String): Boolean = {
        val integerCandidate: Int =
            try {
                integerCandidateString.toInt
            }
            catch {
                case _: NumberFormatException => -1
            }

        integerCandidate > 0
    }

    def isPositiveInteger(integerCandidateStringOption: Option[String]): Boolean = {
        integerCandidateStringOption match {
            case Some(integerCandidateString: String) => isPositiveInteger(integerCandidateString)
            case None => true
        }
    }

    def isIdNumber(idNumberString: String): Boolean = {
        isPositiveInteger(idNumberString)
    }

    def isIdNumber(idNumberOption: Option[String]): Boolean = {
        isPositiveInteger(idNumberOption)
    }

    def toZonedDateTime(dateTimeStringOption: Option[String]): Option[ZonedDateTime] = {
        dateTimeStringOption match {
            case Some(dateTimeString: String) =>
                try {
                    Some(ZonedDateTime.parse(dateTimeString))
                }
                catch {
                    case error: DateTimeParseException => None
                }
            case None => None
        }
    }

    def toZonedDateTimeFromDate(dateString: Option[String]): Option[ZonedDateTime] = {
        toZonedDateTime(dateString.map(value => value + ZeroHourString))
    }

    def lessOrEqual(dateTimeA: Option[ZonedDateTime], dateTimeB: Option[ZonedDateTime]): Boolean = {
        dateTimeA match {
            case Some(valueA: ZonedDateTime) => dateTimeB match {
                case Some(valueB: ZonedDateTime) => valueA.compareTo(valueB) <= 0
                case None => false
            }
            case None => false
        }
    }

    def zonedDateTimeToString(dateTimeOption: Option[ZonedDateTime]): String = {
        dateTimeOption match {
            case Some(dateTime: ZonedDateTime) => dateTime.toString()
            case None => CommonConstants.EmptyString
        }
    }

    def getLaterInstant(time1: Instant, time2: Instant): Instant = {
        if (time1.compareTo(time2) > 0) {
            time1
        }
        else {
            time2
        }
    }

    def isBooleanString(inputString: String): Boolean = {
        ServerConstants.BooleanStrings.contains(inputString)
    }

    def findFirstMissing(values: Seq[Int]): Int = {
        // Returns the first positive integer that is not included in the given sequence.

        def findFirstMissingInternal(remainingValues: Seq[Int], value: Int): Int = {
            remainingValues.headOption match {
                case Some(headValue: Int) => headValue match {
                    case n: Int if n < value => findFirstMissingInternal(remainingValues.drop(1), value)
                    case n: Int if n == value => findFirstMissingInternal(remainingValues.drop(1), value + 1)
                    case _ => value
                }
                case None => value
            }
        }

        findFirstMissingInternal(values.filter(integer => integer > 0).sorted, 1)
    }

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

    def getUpperFolder(path: String): String = {
        path.contains(CommonConstants.Slash) match {
            case true => {
                val pathParts: Array[String] = path.split(CommonConstants.Slash)
                pathParts.take(pathParts.size - 1).mkString(CommonConstants.Slash)
            }
            case false => CommonConstants.EmptyString
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsNonUnitStatements, WartRemoverConstants.WartsListSize))
    def getAttributeCount[T]()(implicit tag: TypeTag[T]): Int = {
        try {
            weakTypeOf[T].decl(termNames.CONSTRUCTOR).asMethod.paramLists.headOption match {
                case Some(paramList: List[_]) => paramList.size
                case None => 0
            }
        }
        catch {
            case reflectionException: ScalaReflectionException => {
                println(s"${reflectionException}")
                0
            }
        }
    }

    def getUuid(inputString: String): String = {
        memeid4s.UUID.V5(memeid4s.UUID.Nil, inputString).toString()
    }

    def getUuid(inputString: String, otherInputs: String*): String = {
        getUuid((Seq(inputString) ++ otherInputs).mkString(CommonConstants.DoubleDot))
    }
}
