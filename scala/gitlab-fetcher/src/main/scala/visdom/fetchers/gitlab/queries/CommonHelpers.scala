package visdom.fetchers.gitlab.queries

import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import visdom.fetchers.gitlab.Routes.server


object CommonHelpers {
    def isProjectName(projectName: String): Boolean = {
        // TODO: implement actual check for a proper project name
        projectName != ""
    }

    def isReference(reference: String): Boolean = {
        // TODO: implement actual check for a proper reference name
        reference != ""
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

    def checkProjectAvailability(projectName: String): Int = {
        visdom.http.gitlab.Utils.getProjectQueryStatusCode(server, projectName)
    }
}
