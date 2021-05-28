package visdom.fetchers.gitlab

import io.circe.JsonObject
import org.mongodb.scala.result.InsertManyResult
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConnection.insertData
import visdom.database.mongodb.MongoConnection.mongoClient
import visdom.database.mongodb.MongoConstants
import visdom.utils.json.Conversions.jsonObjectsToBson
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.Document


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
    private val database: MongoDatabase = mongoClient.getDatabase(databaseName)

    def handleData(
        fetchOptions: GitlabFetchOptions
    ): Int = {
        val dataHandlerOption: Option[GitlabDataHandler] = fetchOptions match {
            case commitFetchOption: GitlabCommitOptions => Some(new GitlabCommitHandler(commitFetchOption))
            case fileFetcherOptions: GitlabFileOptions => Some(new GitlabFileHandler(fileFetcherOptions))
            case _ => None
        }
        val documentsOption: Option[Array[Document]] = dataHandlerOption match {
            case Some(dataHandler: GitlabDataHandler) => dataHandler.process()
            case None => None
        }
        documentsOption match {
            case Some(documents: Array[Document]) => documents.size
            case None => 0
        }
    }

    val commitFetcherOptions: GitlabCommitOptions = GitlabCommitOptions(
        hostServer = server,
        mongoDatabase = Some(database),
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
        mongoDatabase = Some(database),
        projectName = project,
        reference = reference,
        filePath = None,
        useRecursiveSearch = Some(true),
        includeCommitLinks = Some(true)
    )

    val commits: Int = handleData(commitFetcherOptions)
    val files: Int = handleData(fileFetcherOptions)
    println(s"Found ${commits} commits.")
    println(s"Found ${files} files.")

    println("Waiting for 10 seconds.")
    Thread.sleep(endSleep)
    println("The end.")
}
