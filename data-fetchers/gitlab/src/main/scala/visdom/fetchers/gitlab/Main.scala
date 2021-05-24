package visdom.fetchers.gitlab

import io.circe.JsonObject
import org.mongodb.scala.result.InsertManyResult
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConnection.insertData
import visdom.database.mongodb.MongoConstants
import visdom.utils.json.Conversions.jsonObjectsToBson


object Main extends App
{
    private val endSleep: Int = 10000

    def makeRequest(inputRequest: HttpRequest): Option[HttpResponse[String]] = {
        try {
            Some(inputRequest.asString)
        }
        catch {
            case e: java.io.IOException => None
        }
    }

    private val project: String = sys.env.getOrElse(
        GitlabConstants.EnvironmentGitlabProject,
        GitlabConstants.DefaultGitlabProject
    )
    private val reference: String = sys.env.getOrElse(
        GitlabConstants.EnvironmentGitlabReference,
        GitlabConstants.DefaultGitlabReference
    )
    private val databaseName: String = sys.env.getOrElse(
        MongoConstants.MongoTargetDatabase,
        MongoConstants.DefaultMongoTargetDatabase
    )

    private val server: GitlabServer = new GitlabServer(
        hostAddress = sys.env.getOrElse(
            GitlabConstants.EnvironmentGitlabHost,
            GitlabConstants.DefaultGitlabHost
        ),
        apiToken = Some(
            sys.env.getOrElse(
                GitlabConstants.EnvironmentGitlabToken,
                GitlabConstants.DefaultGitlabToken
            )
        ),
        allowUnsafeSSL = Some(
            sys.env.getOrElse(
                GitlabConstants.EnvironmentGitlabInsecure,
                GitlabConstants.DefaultGitlabInsecure
            ).toBoolean
        )
    )

    def getData(
        dataOptions: GitlabFetchOptions,
        commitLinkType: Option[GitlabCommitLinkType]
    ): Either[String, Vector[JsonObject]] = {
        val dataHandlerOption: Option[GitlabDataHandler] = dataOptions match {
            case commitOptions: GitlabCommitOptions => Some(new GitlabCommitHandler(commitOptions))
            case fileOptions: GitlabFileOptions => Some(new GitlabFileHandler(fileOptions))
            case commitLinkOptions: GitlabCommitLinkOptions => commitLinkType match {
                case Some(value) => value match {
                    case GitlabCommitDiff => Some(new GitlabCommitDiffHandler(commitLinkOptions))
                    case GitlabCommitRefs => Some(new GitlabCommitRefsHandler(commitLinkOptions))
                }
                case None => None
            }
        }

        dataHandlerOption match {
            case Some(dataHandler: GitlabDataHandler) => {
                val dataRequest: HttpRequest = dataHandler.getRequest()
                val responses: Vector[HttpResponse[String]] = dataHandler.makeRequests(dataRequest)
                dataHandler.processAllResponses(responses)
            }
            case None => Left("Error: no data fetcher could be created")
        }
    }

    def handleData(
        database: String,
        collection: String,
        rawData: Either[String, Vector[JsonObject]]
    ): Unit = {
        rawData match {
            case Right(jsonData: Vector[JsonObject]) => {
                println(s"Found ${jsonData.length} commits at ${project}")
                insertData(database, collection, jsonData).subscribe(
                    doOnNext = (result: InsertManyResult) =>
                        println(s"${result.getInsertedIds.size()} documents written to the database"),
                    doOnError = (error: Throwable) =>
                        println(s"Database error: ${error.toString()}")
                )
            }
            case Left(errorMessage: String) => println(s"Fetch error: ${errorMessage}")
        }
    }

    val commitFetcherOptions: GitlabCommitOptions = GitlabCommitOptions(
        hostServer = server,
        projectName = project,
        reference = reference,
        startDate = None,
        endDate = None,
        filePath = None,
        includeStatistics = Some(true),
        includeFileLinks = Some(true),
        includeReferenceLinks = Some(true)
    )

    val fileFetcherOptions: GitlabFileOptions = GitlabFileOptions(
        hostServer = server,
        projectName = project,
        reference = reference,
        filePath = None,
        useRecursiveSearch = Some(true),
        includeCommitLinks = Some(true)
    )

    handleData(databaseName, MongoConstants.CollectionCommits, getData(commitFetcherOptions, None))
    handleData(databaseName, MongoConstants.CollectionFiles, getData(fileFetcherOptions, None))

    println("Waiting for 10 seconds.")
    Thread.sleep(10000)
    println("The end.")
}
