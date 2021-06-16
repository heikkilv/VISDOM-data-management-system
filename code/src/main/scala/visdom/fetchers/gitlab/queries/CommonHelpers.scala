package visdom.fetchers.gitlab.queries

import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import visdom.fetchers.gitlab.Routes.server
import visdom.fetchers.gitlab.utils.HttpUtils


object CommonHelpers {
    def isProjectName(projectName: String): Boolean = {
        // TODO: implement actual check for a proper project name
        projectName != ""
    }

    def isReference(reference: String): Boolean = {
        // TODO: implement actual check for a proper reference name
        reference != ""
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

    def toFilePath(filePathOption: Option[String]): Option[String] = {
        filePathOption match {
            case Some(filePath: String) => filePath match {
                case "" => None
                // TODO: implement actual check for a proper file path
                case _ => filePathOption
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

    def checkProjectAvailability(projectName: String): Int = {
        HttpUtils.getProjectQueryStatusCode(server, projectName)
    }
}
