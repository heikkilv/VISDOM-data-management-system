package visdom.fetchers.gitlab

import io.circe.JsonObject
import java.time.temporal.ChronoUnit.SECONDS
import java.time.ZonedDateTime
import java.time.ZoneOffset
import scalaj.http.Http
import scalaj.http.HttpConstants.utf8
import scalaj.http.HttpConstants.urlEncode
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse


class GitlabCommitHandler(options: GitlabCommitOptions)
    extends GitlabDataHandler {

    def getRequest(): HttpRequest = {
        // https://docs.gitlab.com/ee/api/commits.html
        val uri: String = List(
            options.hostServer.baseAddress,
            GitlabConstants.PathProjects,
            urlEncode(options.projectName, utf8),
            GitlabConstants.PathRepository,
            GitlabConstants.PathCommits
        ).mkString("/")

        val commitRequest: HttpRequest = processOptionalParameters(
            Http(uri).param(GitlabConstants.ParamRef, options.reference)
        )
        options.hostServer.modifyRequest(commitRequest)
    }

    override def processResponse(response: HttpResponse[String]): Either[String, Vector[JsonObject]] = {
        val baseCommitResults: Either[String, Vector[JsonObject]] = super.processResponse(response)
        utils.JsonUtils.modifyJsonResult(
            baseCommitResults,
            utils.JsonUtils.addProjectName,
            options.projectName
        )
    }

    private def processOptionalParameters(request: HttpRequest): HttpRequest = {
        @SuppressWarnings(Array("org.wartremover.warts.Var"))
        var paramMap: Seq[(String, String)] = Seq.empty

        options.startDate match {
            case Some(startDate: ZonedDateTime) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamSince,
                    startDate.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(SECONDS).toString()
                ))
            }
            case None =>
        }

        options.endDate match {
            case Some(endDate: ZonedDateTime) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamUntil,
                    endDate.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(SECONDS).toString()
                ))
            }
            case None =>
        }

        options.filePath match {
            case Some(filePath: String) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamPath, filePath
                ))
            }
            case None =>
        }

        options.includeStatistics match {
            case Some(includeStatistics: Boolean) => {
                paramMap = paramMap ++ Seq((
                    GitlabConstants.ParamWithStats, includeStatistics.toString()
                ))
            }
            case None =>
        }

        // includeFileLinks
        // includeReferenceLinks

        request.params(paramMap)
    }
}
