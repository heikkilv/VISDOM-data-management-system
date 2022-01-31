package visdom.utils

import java.time.Instant
import java.time.ZonedDateTime
import scala.reflect.runtime.universe.termNames
import scala.reflect.runtime.universe.weakTypeOf
import scala.reflect.runtime.universe.TypeTag
import visdom.http.server.ServerConstants


object GeneralUtils {
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
            case instantValue: Instant => Some(TimeUtils.getMillisString(instantValue))
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
                                    TimeUtils.getMillisString(
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
            case stringValue: String => toInstantOption(TimeUtils.toZonedDateTime(Some(stringValue)))
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

    def seqOfSeqToSeq[A, B](itemSeq: Seq[(A, Seq[B])]): Seq[(A, B)] = {
        itemSeq
            .map({case (a, bSeq) => bSeq.map(b => (a, b))})
            .flatten
    }

    def mapOfSeqToSeq[A, B](itemMap: Map[A, Seq[B]]): Seq[(A, B)] = {
        seqOfSeqToSeq(itemMap.toSeq)
    }

    def mapOfSeqToSeq[A, B](itemMap: scala.collection.mutable.Map[A, Seq[B]]): Seq[(A, B)] = {
        seqOfSeqToSeq(itemMap.toSeq)
    }

    def addItemToMapOfSeq[A, B](itemMap: scala.collection.mutable.Map[A, Seq[B]], key: A, value: B): Unit = {
        itemMap.get(key) match {
            case Some(currentValues: Seq[B]) => currentValues.contains(value) match {
                case true =>
                case false => itemMap.update(key, currentValues ++ Seq(value))
            }
            case None => itemMap.update(key, Seq(value))
        }
    }
}
