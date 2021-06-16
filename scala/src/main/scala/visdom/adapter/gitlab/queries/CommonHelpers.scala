package visdom.adapter.gitlab.queries

import java.time.LocalDate
import java.time.DateTimeException
import java.time.ZonedDateTime
import visdom.adapter.gitlab.utils.TimeUtils


object CommonHelpers {
    def isNonEmpty(stringOption: Option[String]): Boolean = {
        // Returns true for either non-empty string or None.
        stringOption match {
            case Some(stringValue: String) => stringValue != ""
            case None => true
        }
    }

    def isProjectName(projectNameOption: Option[String]): Boolean = {
        // TODO: implement actual check for a proper project name
        isNonEmpty(projectNameOption)
    }

    def getCheckedProjectName(projectNameOption: Option[String]): Either[String, Option[String]] = {
        isProjectName(projectNameOption) match {
            case true => Right(projectNameOption)
            case false => Left(s"'${projectNameOption.getOrElse("")}' is not a valid project name")
        }
    }

    def isUserName(userNameOption: Option[String]): Boolean = {
        // TODO: implement actual check for a proper user name
        isNonEmpty(userNameOption)
    }

    def isFilePath(filePathOption: Option[String]): Boolean = {
        // TODO: implement actual check for a proper file path
        isNonEmpty(filePathOption)
    }

    def isFilePaths(filePaths: String): Boolean = {
        val filePathArray: Array[String] = filePaths.split(Constants.Comma)
        filePathArray.size match {
            case 0 => false
            case _ => filePathArray.forall(filePath => isFilePath(Some(filePath)))
        }
    }

    def getCheckedFilePaths(filePaths: String): Either[String, Array[String]] = {
        isFilePaths(filePaths) match {
            case true => Right(filePaths.split(Constants.Comma))
            case false => Left(s"'${filePaths}' is not a valid list of file paths")
        }
    }

    def isDate(dateString: String): Boolean = {
        try {
            val temp: LocalDate = LocalDate.parse(dateString)
            true
        }
        catch {
            case _: DateTimeException => false
        }
    }

    def isDateOption(dateStringOption: Option[String]): Boolean = {
        dateStringOption match {
            case Some(dateString: String) => isDate(dateString)
            case None => true
        }
    }

    def toDateTimeString(dateTimeStringOption: Option[String]): Option[String] = {
        dateTimeStringOption match {
            case Some(dateTimeString: String) => TimeUtils.toUtcString(dateTimeString)
            case None => None
        }
    }

    def getCheckedDateTime(dateTimeStringOption: Option[String]): Either[String, Option[String]] = {
        toDateTimeString(dateTimeStringOption) match {
            case Some(dateTime: String) => Right(Some(dateTime))
            case None => dateTimeStringOption match {
                case None => Right(None)
                case Some(dateTimeString: String) =>
                    Left(s"'${dateTimeString}' is not a valid datetime in ISO 8601 format with timezone")
            }
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

    def getCheckedParameter[T, U](
        parameterValue: T,
        parameterFunction: T => Either[String, U]
    ): Either[String, U] = {
        parameterFunction(parameterValue)
    }
}
