package visdom.utils

import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

object GeneralUtils {
    def toInt(stringValue: String): Option[Int] = {
        try {
            Some(stringValue.toInt)
        } catch {
            case _: java.lang.NumberFormatException => None
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
            case instantValue: Instant => Some(instantValue.toString())
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
                                    Instant.ofEpochMilli(
                                        stringPart.substring(0, stringPart.size - 1).toLong
                                    ).toString()
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

    def isIdNumber(idNumberString: String): Boolean = {
        val idNumber: Int =
            try {
                idNumberString.toInt
            }
            catch {
                case _: NumberFormatException => -1
            }

        idNumber > 0
    }

    def isIdNumber(idNumberOption: Option[String]): Boolean = {
        idNumberOption match {
            case Some(idString: String) => isIdNumber(idString)
            case None => true
        }
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

    implicit class EnrichedWithToTuple[A](elements: Seq[A]) {
        def toTuple1: Tuple1[A] = elements match {case Seq(a) => Tuple1(a)}
        def toTuple2: (A, A) = elements match {case Seq(a, b) => (a, b)}
        def toTuple3: (A, A, A) = elements match {case Seq(a, b, c) => (a, b, c)}
        def toTuple4: (A, A, A, A) = elements match {case Seq(a, b, c, d) => (a, b, c, d)}
        def toTuple5: (A, A, A, A, A) = elements match {case Seq(a, b, c, d, e) => (a, b, c, d, e)}
        def toTuple6: (A, A, A, A, A, A) = elements match {case Seq(a, b, c, d, e, f) => (a, b, c, d, e, f)}
    }

    def toOption[A, B](
        values: (Any, Any),
        transformations: ((Any) => Option[A], (Any) => Option[B])
    ): Option[(A, B)] = {
        transformations._1(values._1) match {
            case Some(value1) => transformations._2(values._2) match {
                case Some(value2) => Some(value1, value2)
                case None => None
            }
            case None => None
        }
    }

    def toOption[A, B, C](
        values: (Any, Any, Any),
        transformations: ((Any) => Option[A], (Any) => Option[B], (Any) => Option[C])
    ): Option[(A, B, C)] = {
        transformations._1(values._1) match {
            case Some(value1) => transformations._2(values._2) match {
                case Some(value2) => transformations._3(values._3) match {
                    case Some(value3) => Some(value1, value2, value3)
                    case None => None
                }
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D](
        values: (Any, Any, Any, Any),
        transformations: ((Any) => Option[A], (Any) => Option[B], (Any) => Option[C], (Any) => Option[D])
    ): Option[(A, B, C, D)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (values._2, values._3, values._4),
                (transformations._2, transformations._3, transformations._4)
            ) match {
                case Some((value2, value3, value4)) =>
                    Some(value1, value2, value3, value4)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E](
        values: (Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A],
            (Any) => Option[B],
            (Any) => Option[C],
            (Any) => Option[D],
            (Any) => Option[E]
        )
    ): Option[(A, B, C, D, E)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (values._2, values._3, values._4, values._5),
                (transformations._2, transformations._3, transformations._4, transformations._5)
            ) match {
                case Some((value2, value3, value4, value5)) =>
                    Some(value1, value2, value3, value4, value5)
                case None => None
            }
            case None => None
        }
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toOption[A, B, C, D, E, F](
        values: (Any, Any, Any, Any, Any, Any),
        transformations: (
            (Any) => Option[A],
            (Any) => Option[B],
            (Any) => Option[C],
            (Any) => Option[D],
            (Any) => Option[E],
            (Any) => Option[F]
            )
    ): Option[(A, B, C, D, E, F)] = {
        transformations._1(values._1) match {
            case Some(value1) => toOption(
                (values._2, values._3, values._4, values._5, values._6),
                (transformations._2, transformations._3, transformations._4, transformations._5, transformations._6)
            ) match {
                case Some((value2, value3, value4, value5, value6)) =>
                    Some(value1, value2, value3, value4, value5, value6)
                case None => None
            }
            case None => None
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
