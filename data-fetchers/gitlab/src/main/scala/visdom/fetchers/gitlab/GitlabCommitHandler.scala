package visdom.fetchers.gitlab

import io.circe.Json
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
        // https://docs.gitlab.com/ee/api/commits.html#list-repository-commits
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
        val modifiedCommitResults: Either[String, Vector[JsonObject]] = utils.JsonUtils.modifyJsonResult(
            baseCommitResults,
            utils.JsonUtils.addProjectName,
            options.projectName
        )

        val resultsAfterLinks: Either[String, Vector[JsonObject]] = modifiedCommitResults match {
            case Right(commitResults: Vector[JsonObject]) => {
                options.includeFileLinks match {
                    case Some(includeFileLinks: Boolean) if includeFileLinks => {
                        Right(fetchAllDiffData(commitResults))
                    }
                    case _ => modifiedCommitResults
                }
            }
            case Left(_) => modifiedCommitResults
        }
        resultsAfterLinks
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
                    GitlabConstants.ParamPath, urlEncode(filePath, utf8)
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

        // includeReferenceLinks

        request.params(paramMap)
    }

    private def fetchDiffData(commitId: String): Either[String, Vector[JsonObject]] = {
        val commitDiffOptions: GitlabCommitDiffOptions = GitlabCommitDiffOptions(
            options.hostServer,
            options.projectName,
            commitId
        )
        val commitDiffFetcher: GitlabCommitDiffHandler = new GitlabCommitDiffHandler(commitDiffOptions)
        val commitDiffRequest: HttpRequest = commitDiffFetcher.getRequest()
        val commitDiffResponses: Vector[HttpResponse[String]] = commitDiffFetcher.makeRequests(commitDiffRequest)
        commitDiffFetcher.processAllResponses(commitDiffResponses)
    }

    private def fetchAllDiffData(commitData: Vector[JsonObject]): Vector[JsonObject] = {
        commitData.map(
            commitObject => {
                commitObject.apply(GitlabConstants.attributeId) match {
                    case Some(commitIdJson: Json) => commitIdJson.asString match {
                        case Some(commitId: String) => fetchDiffData(commitId) match {
                            case Right(diffData: Vector[JsonObject]) => {
                                val diffJson: Json = Json.fromValues(
                                    diffData.map(diffObject => Json.fromJsonObject(diffObject))
                                )
                                utils.JsonUtils.addSubAttribute(
                                    commitObject,
                                    GitlabConstants.attributeLinks,
                                    GitlabConstants.attributeFiles,
                                    diffJson
                                )
                            }
                            case Left(errorMessage: String) => commitObject
                        }
                        case None => commitObject
                    }
                    case None => commitObject
                }
            }
        )
    }
}
