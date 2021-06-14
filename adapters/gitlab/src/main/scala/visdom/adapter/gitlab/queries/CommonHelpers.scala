package visdom.adapter.gitlab.queries

import java.time.LocalDate
import java.time.DateTimeException


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

    def isUserName(userNameOption: Option[String]): Boolean = {
        // TODO: implement actual check for a proper user name
        isNonEmpty(userNameOption)
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
}
