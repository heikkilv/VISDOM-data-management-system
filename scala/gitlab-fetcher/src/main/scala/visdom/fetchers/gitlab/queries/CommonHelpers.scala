package visdom.fetchers.gitlab.queries

import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import visdom.fetchers.gitlab.Routes.server
import visdom.utils.CommonConstants


object CommonHelpers {
    def isProjectName(projectName: String): Boolean = {
        // TODO: implement actual check for a proper project name
        projectName != ""
    }

    def isProjectNameSequence(projectNames: String): Boolean = {
        !(
            projectNames
                .split(CommonConstants.Comma)
                .map(projectName => isProjectName(projectName))
                .contains(false)
        )
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

    def checkProjectsAvailability(projectNames: Seq[String]): Map[Int, Seq[String]] = {
        projectNames.map(
            projectName => (
                visdom.http.gitlab.Utils.getProjectQueryStatusCode(server, projectName),
                projectName
            )
        )
            .groupBy(codeWithProject => codeWithProject._1)
            .mapValues(codeWithProjectSequence => codeWithProjectSequence.map(codeWithProject => codeWithProject._2))
    }
}
