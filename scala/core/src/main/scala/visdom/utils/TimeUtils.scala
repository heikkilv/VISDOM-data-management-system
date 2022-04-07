package visdom.utils

import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit


object TimeUtils {
    final val DefaultYear: Int = 1970
    final val DefaultMonth: Int = 1
    final val DefaultDay: Int = 1
    final val DefaultTime: ZonedDateTime =
        ZonedDateTime.of(DefaultYear, DefaultMonth, DefaultDay, 0, 0, 0, 0, ZoneId.of(CommonConstants.UTC))

    final val ZeroHourString: String = "T00:00Z"

    def toZonedDateTime(dateTimeString: String): Option[ZonedDateTime] = {
        try {
            Some(ZonedDateTime.parse(dateTimeString))
        }
        catch {
            case error: DateTimeParseException => None
        }
    }

    def toZonedDateTimeWithDefault(dateTimeString: String): ZonedDateTime = {
        toZonedDateTime(dateTimeString) match {
            case Some(dateTimeValue: ZonedDateTime) => dateTimeValue
            case None => DefaultTime
        }
    }

    def toZonedDateTime(dateTimeStringOption: Option[String]): Option[ZonedDateTime] = {
        dateTimeStringOption match {
            case Some(dateTimeString: String) => toZonedDateTime(dateTimeString)
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

    def getDifference(timeA: ZonedDateTime, timeB: ZonedDateTime): Double = {
         timeA.toEpochSecond() - timeB.toEpochSecond() +
         timeA.getNano() / CommonConstants.Billion - timeB.getNano() / CommonConstants.Billion
    }

    def fromEpochMilliToString(epochTime: Long): String = {
        getMillisString(Instant.ofEpochMilli(epochTime))
    }
}
